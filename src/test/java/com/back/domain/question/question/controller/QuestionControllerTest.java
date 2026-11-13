package com.back.domain.question.question.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;
import com.back.global.exception.DataNotFoundException;
import com.back.domain.question.question.entity.Question;
import com.back.domain.question.question.repository.QuestionRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class QuestionControllerTest {
    @Autowired
    private MockMvc mvc;
    @Autowired
    private QuestionRepository questionRepository;

    @Test
    @DisplayName("GET /questions : 질문 목록에 샘플 질문 제목들이 보인다.")
    void t1() throws Exception {
        ResultActions resultActions = mvc
                .perform(get("/questions"))
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(QuestionController.class))
                .andExpect(handler().methodName("list"))
                .andExpect(status().isOk())
                .andExpect(view().name("question/question/list")) // 어떤 템플릿으로 그렸는지
                .andExpect(content().string(containsString("sbb가 무엇인가요?")))
                .andExpect(content().string(containsString("스프링부트 모델 질문입니다.")))
                .andExpect(content().string(containsString("href=\"/questions/1\""))); // 제목이 상세 페이지 링크인지
    }

    @Test
    @DisplayName("GET /questions/1 : 1번 질문의 제목과 내용이 보인다.")
    void t2() throws Exception {
        ResultActions resultActions = mvc
                .perform(get("/questions/1"))
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(QuestionController.class))
                .andExpect(handler().methodName("detail"))
                .andExpect(status().isOk())
                .andExpect(view().name("question/question/detail"))
                .andExpect(content().string(containsString("sbb가 무엇인가요?")))
                .andExpect(content().string(containsString("sbb에 대해서 알고 싶습니다.")));
    }

    @Test
    @DisplayName("GET /questions/999 : 없는 질문이면 404 로 응답한다.")
    void t3() throws Exception {
        ResultActions resultActions = mvc
                .perform(get("/questions/999"))
                .andDo(print());

        resultActions
                .andExpect(handler().methodName("detail"))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertThat(result.getResolvedException()).isInstanceOf(DataNotFoundException.class));
    }

    @Test
    @DisplayName("GET /questions/2 : 질문에 달린 답변 개수와 답변 내용이 보인다.")
    void t4() throws Exception {
        ResultActions resultActions = mvc
                .perform(get("/questions/2"))
                .andDo(print());

        resultActions
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("2개의 답변이 있습니다.")))
                .andExpect(content().string(containsString("네 자동으로 생성됩니다.")))
                .andExpect(content().string(containsString("맞습니다!")));
    }

    @Test
    @DisplayName("GET /questions/write : 질문 등록 폼이 보인다.")
    void t5() throws Exception {
        ResultActions resultActions = mvc
                .perform(get("/questions/write"))
                .andDo(print());

        resultActions
                .andExpect(handler().methodName("showWrite"))
                .andExpect(status().isOk())
                .andExpect(view().name("question/question/write"))
                .andExpect(content().string(containsString("name=\"subject\"")))
                .andExpect(content().string(containsString("name=\"content\"")));
    }

    @Test
    @DisplayName("POST /questions/write : 질문이 등록되고, 등록된 질문의 상세 페이지로 리다이렉트된다.")
    void t6() throws Exception {
        ResultActions resultActions = mvc
                .perform(
                        post("/questions/write")
                                .param("subject", "새 질문 제목")
                                .param("content", "새 질문 내용")
                )
                .andDo(print());

        resultActions
                .andExpect(handler().methodName("write"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/questions/*"));

        // 번호는 다른 테스트 실행 순서에 따라 달라질 수 있어서(롤백돼도 auto increment 는 되돌아가지 않음) 제목으로 찾는다.
        Question question = questionRepository.findBySubject("새 질문 제목").get();
        assertThat(question.getContent()).isEqualTo("새 질문 내용");
        resultActions.andExpect(redirectedUrl("/questions/" + question.getId())); // 그 질문의 상세 페이지로 갔는지
    }

    @Test
    @DisplayName("POST /questions/write : 제목이 비어 있으면 저장하지 않고 폼을 다시 보여 주며, 입력한 내용은 유지된다.")
    void t7() throws Exception {
        long beforeCount = questionRepository.count();

        ResultActions resultActions = mvc
                .perform(
                        post("/questions/write")
                                .param("subject", "   ") // 공백만
                                .param("content", "내용은 입력함")
                )
                .andDo(print());

        resultActions
                .andExpect(handler().methodName("write"))
                .andExpect(status().isOk()) // 리다이렉트가 아니라 폼을 다시 그림
                .andExpect(view().name("question/question/write"))
                .andExpect(model().attributeHasFieldErrors("form", "subject"))
                .andExpect(content().string(containsString("제목을 입력해주세요.")))
                .andExpect(content().string(containsString("data-error-field-name=\"subject\""))) // 포커스할 필드 정보
                .andExpect(content().string(containsString("내용은 입력함"))); // 입력값 유지

        assertThat(questionRepository.count()).isEqualTo(beforeCount); // 저장되지 않음
    }
}
