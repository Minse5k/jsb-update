package com.back.domain.question.comment.controller;

import com.back.domain.question.comment.entity.Comment;
import com.back.domain.question.question.entity.Question;
import com.back.domain.question.question.repository.QuestionRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class CommentControllerTest {
    @Autowired
    private MockMvc mvc;
    @Autowired
    private QuestionRepository questionRepository;
    @Autowired
    private EntityManager em;

    @Test
    @DisplayName("질문 댓글 : 로그인한 회원이 등록하면 질문 아래에 보이고, 빈 댓글은 저장되지 않는다.")
    void t1() throws Exception {
        mvc
                .perform(post("/questions/1/comments").with(csrf()).with(user("user2")).param("content", "좋은 질문이네요"))
                .andExpect(handler().methodName("writeOnQuestion"))
                .andExpect(redirectedUrl("/questions/1#question_comments"));

        mvc
                .perform(post("/questions/1/comments").with(csrf()).with(user("user2")).param("content", "   "))
                .andExpect(redirectedUrl("/questions/1#question_comments"));

        em.flush();
        em.clear();
        assertThat(questionRepository.findById(1).get().getComments()).extracting("content").containsExactly("좋은 질문이네요");

        mvc
                .perform(get("/questions/1"))
                .andExpect(content().string(containsString("좋은 질문이네요")));
    }

    @Test
    @DisplayName("질문 댓글 삭제 : 다른 회원은 403, 글쓴이는 삭제된다.")
    void t2() throws Exception {
        Question question = questionRepository.findById(1).get();
        Comment comment = question.addComment(question.getAuthor(), "지울 댓글"); // user1 이 쓴 댓글
        em.flush();

        mvc
                .perform(post("/questions/1/comments/%d".formatted(comment.getId())).param("_method", "DELETE").with(csrf()).with(user("user2")))
                .andExpect(status().isForbidden());

        mvc
                .perform(post("/questions/1/comments/%d".formatted(comment.getId())).param("_method", "DELETE").with(csrf()).with(user("user1")))
                .andExpect(handler().methodName("deleteOnQuestion"))
                .andExpect(redirectedUrl("/questions/1#question_comments"));

        em.flush();
        em.clear();
        assertThat(questionRepository.findById(1).get().getComments()).isEmpty();
    }

    @Test
    @DisplayName("답변 댓글 : 답변 아래에 등록·표시되고, 글쓴이만 삭제할 수 있으며, 그 답변 위치로 돌아간다.")
    void t3() throws Exception {
        int answerId = questionRepository.findById(2).get().getAnswers().getFirst().getId();
        String base = "/questions/2/answers/%d/comments".formatted(answerId);

        mvc
                .perform(post(base).with(csrf()).with(user("user1")).param("content", "답변에 단 댓글"))
                .andExpect(handler().methodName("writeOnAnswer"))
                .andExpect(redirectedUrl("/questions/2?ap=0#answer_%d".formatted(answerId)));

        mvc
                .perform(get("/questions/2"))
                .andExpect(content().string(containsString("id=\"answer_comments_%d\"".formatted(answerId))))
                .andExpect(content().string(containsString("답변에 단 댓글")));

        em.flush();
        em.clear();
        Comment comment = questionRepository.findById(2).get().getAnswers().getFirst().getComments().getFirst();

        mvc
                .perform(post(base + "/" + comment.getId()).param("_method", "DELETE").with(csrf()).with(user("user2")))
                .andExpect(status().isForbidden());
        mvc
                .perform(post(base + "/" + comment.getId()).param("_method", "DELETE").with(csrf()).with(user("user1")))
                .andExpect(handler().methodName("deleteOnAnswer"));

        em.flush();
        em.clear();
        assertThat(questionRepository.findById(2).get().getAnswers().getFirst().getComments()).isEmpty();
    }
}
