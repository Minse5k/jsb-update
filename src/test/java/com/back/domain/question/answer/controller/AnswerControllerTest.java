package com.back.domain.question.answer.controller;

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
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;
import com.back.domain.question.answer.entity.Answer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class AnswerControllerTest {
    @Autowired
    private MockMvc mvc;
    @Autowired
    private QuestionRepository questionRepository;
    @Autowired
    private EntityManager em;

    @Test
    @DisplayName("POST /questions/1/answers : 1번 질문에 답변이 등록되고 상세 페이지로 리다이렉트된다.")
    void t1() throws Exception {
        ResultActions resultActions = mvc
                .perform(
                        post("/questions/1/answers")
                                .with(csrf()) // 시큐리티 테스트 도구 : 진짜 폼처럼 CSRF 토큰을 붙여서 보낸다.
                                .with(user("user1")) // user1(샘플 회원)로 로그인한 상태
                                .param("content", "답변 내용입니다.") // 폼에서 content 를 보낸 것과 같다.
                )
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(AnswerController.class))
                .andExpect(handler().methodName("write"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/questions/1"));

        // 테스트 전체가 하나의 트랜잭션이라 아직 커밋 전이다.
        // flush : 지금까지의 변경(INSERT)을 DB에 실제로 보낸다. / clear : 메모리에 있던 엔티티를 비운다.
        // → 아래 findById 는 메모리가 아니라 DB에서 다시 읽어 온다. (진짜 저장됐는지 확인)
        em.flush();
        em.clear();

        Question question = questionRepository.findById(1).get();
        assertThat(question.getAnswers()).hasSize(1);
        assertThat(question.getAnswers().getLast().getContent()).isEqualTo("답변 내용입니다.");
    }

    @Test
    @DisplayName("POST /questions/1/answers : 내용이 비어 있으면 등록하지 않고 상세 페이지를 에러와 함께 다시 보여 준다.")
    void t2() throws Exception {
        ResultActions resultActions = mvc
                .perform(
                        post("/questions/1/answers")
                                .with(csrf()) // 시큐리티 테스트 도구 : 진짜 폼처럼 CSRF 토큰을 붙여서 보낸다.
                                .with(user("user1")) // user1(샘플 회원)로 로그인한 상태
                                .param("content", "")
                )
                .andDo(print());

        resultActions
                .andExpect(handler().methodName("write"))
                .andExpect(status().isOk())
                .andExpect(view().name("question/question/detail"))
                .andExpect(model().attributeHasFieldErrors("answerForm", "content"))
                .andExpect(content().string(containsString("답변 내용을 입력해주세요.")));

        em.flush();
        em.clear();
        assertThat(questionRepository.findById(1).get().getAnswers()).isEmpty();
    }

    @Test
    @DisplayName("답변 수정 : 글쓴이(user2)는 수정 폼과 수정 처리가 되고, 다른 회원(user1)은 403")
    void t3() throws Exception {
        Question question = questionRepository.findById(2).get();
        int answerId = question.getAnswers().getFirst().getId(); // user2 가 쓴 첫 번째 답변

        mvc
                .perform(get("/questions/2/answers/%d/modify".formatted(answerId)).with(user("user2")))
                .andExpect(status().isOk())
                .andExpect(view().name("question/answer/modify"))
                .andExpect(content().string(containsString("네 자동으로 생성됩니다.")));

        mvc
                .perform(get("/questions/2/answers/%d/modify".formatted(answerId)).with(user("user1")))
                .andExpect(status().isForbidden());

        mvc
                .perform(
                        post("/questions/2/answers/%d/modify".formatted(answerId))
                                .param("_method", "PUT")
                                .with(csrf())
                                .with(user("user2"))
                                .param("content", "수정된 답변")
                )
                .andExpect(handler().methodName("modify"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/questions/2"));

        em.flush();
        em.clear();
        assertThat(questionRepository.findById(2).get().getAnswers().getFirst().getContent()).isEqualTo("수정된 답변");
    }

    @Test
    @DisplayName("답변 수정 : 다른 질문의 답변 번호로 접근하면 404")
    void t4() throws Exception {
        int answerIdOfQuestion2 = questionRepository.findById(2).get().getAnswers().getFirst().getId();

        mvc
                .perform(get("/questions/1/answers/%d/modify".formatted(answerIdOfQuestion2)).with(user("user2")))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("답변 삭제 : 다른 회원은 403, 글쓴이가 삭제하면 질문의 답변 목록에서 빠지고(orphanRemoval) DB 에서도 삭제된다.")
    void t5() throws Exception {
        int answerId = questionRepository.findById(2).get().getAnswers().getFirst().getId();

        mvc
                .perform(delete("/questions/2/answers/%d".formatted(answerId)).with(csrf()).with(user("user1")))
                .andExpect(status().isForbidden());

        mvc
                .perform(
                        post("/questions/2/answers/%d".formatted(answerId))
                                .param("_method", "DELETE")
                                .with(csrf())
                                .with(user("user2"))
                )
                .andExpect(handler().methodName("delete"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/questions/2"));

        em.flush();
        em.clear();
        assertThat(questionRepository.findById(2).get().getAnswers()).hasSize(1);
        assertThat(em.find(Answer.class, answerId)).isNull(); // answer 테이블에서도 사라짐
    }
}
