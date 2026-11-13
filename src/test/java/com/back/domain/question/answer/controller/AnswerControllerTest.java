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

import static org.assertj.core.api.Assertions.assertThat;
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
}
