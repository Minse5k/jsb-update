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
import com.back.domain.question.question.service.QuestionService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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
    @Autowired
    private QuestionService questionService;

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
                .andExpect(content().string(containsString("href=\"/questions/1\""))) // 제목이 상세 페이지 링크인지
                .andExpect(content().string(containsString("class=\"navbar-brand\""))) // 레이아웃의 내비게이션 바
                // 답변 개수 표시 : 샘플 중 2번 질문만 답변(2개)이 있으므로 딱 1군데에 "2" 가 나와야 한다.
                .andExpect(content().string(containsString("<span class=\"text-danger small ms-2\">2</span>")))
                .andExpect(result -> assertThat(result.getResponse().getContentAsString().split("text-danger small ms-2")).hasSize(2));
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
                .andExpect(content().string(containsString("<div class=\"mb-2\">user1</div>"))) // 질문 글쓴이
                .andExpect(content().string(containsString("<div class=\"mb-2\">user2</div>"))) // 답변 글쓴이
                .andExpect(content().string(containsString("<a id=\"answer_1\"></a>"))) // 답변 앵커
                .andExpect(content().string(containsString("네 자동으로 생성됩니다.")))
                .andExpect(content().string(containsString("맞습니다!")));
    }

    @Test
    @DisplayName("GET /questions/write : 질문 등록 폼이 보인다.")
    void t5() throws Exception {
        ResultActions resultActions = mvc
                .perform(get("/questions/write").with(user("user1")))
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
                                .with(csrf()) // 시큐리티 테스트 도구 : 진짜 폼처럼 CSRF 토큰을 붙여서 보낸다.
                                .with(user("user1")) // user1(샘플 회원)로 로그인한 상태
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
                                .with(csrf()) // 시큐리티 테스트 도구 : 진짜 폼처럼 CSRF 토큰을 붙여서 보낸다.
                                .with(user("user1")) // user1(샘플 회원)로 로그인한 상태
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

    @Test
    @DisplayName("GET /questions?page=1 : 한 페이지에 10개씩, 최신 질문부터 보인다.")
    void t8() throws Exception {
        // 샘플 2개 + 25개 = 27개 → 0페이지 10개, 1페이지 10개, 2페이지 7개
        for (int i = 1; i <= 25; i++) {
            questionService.write(null, "페이징 질문 %02d".formatted(i), "내용");
        }

        ResultActions resultActions = mvc
                .perform(get("/questions").param("page", "1"))
                .andDo(print());

        resultActions
                .andExpect(handler().methodName("list"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("paging", hasProperty("number", is(1)))) // 현재 페이지 번호
                .andExpect(model().attribute("paging", hasProperty("totalPages", is(3))))
                .andExpect(content().string(containsString("페이징 질문 15"))) // 1페이지 = 최신순 11~20번째 → 15..06
                .andExpect(content().string(containsString("페이징 질문 06")))
                .andExpect(content().string(not(containsString("페이징 질문 16"))))
                .andExpect(content().string(not(containsString("페이징 질문 05"))))
                .andExpect(content().string(containsString("href=\"/questions?page=0&amp;kw=\""))) // 이전 버튼(0페이지, & 는 HTML 에서 &amp;)
                .andExpect(content().string(containsString("href=\"/questions?page=2&amp;kw=\""))); // 다음 버튼(2페이지)
    }

    @Test
    @DisplayName("POST /questions/write : CSRF 토큰 없이 보내면 시큐리티가 403 으로 막는다.")
    void t9() throws Exception {
        ResultActions resultActions = mvc
                .perform(
                        post("/questions/write") // .with(csrf()) 없음 = 다른 사이트에서 몰래 보낸 요청과 같다.
                                .param("subject", "토큰 없는 질문")
                                .param("content", "토큰 없는 내용")
                )
                .andDo(print());

        resultActions
                .andExpect(status().isForbidden()); // 컨트롤러까지 가지 못하고 필터에서 거절

        assertThat(questionRepository.findBySubject("토큰 없는 질문")).isEmpty();
    }

    @Test
    @DisplayName("GET /questions/write : 로그인하지 않으면 로그인 화면으로 보낸다.")
    void t10() throws Exception {
        mvc
                .perform(get("/questions/write")) // user(...) 없음 = 비로그인
                .andDo(print())
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/users/login"));
    }

    @Test
    @DisplayName("POST /questions/write : 로그인한 회원이 글쓴이로 저장된다.")
    void t11() throws Exception {
        mvc
                .perform(
                        post("/questions/write")
                                .with(csrf())
                                .with(user("user2"))
                                .param("subject", "user2 의 질문")
                                .param("content", "글쓴이 확인")
                )
                .andExpect(status().is3xxRedirection());

        Question question = questionRepository.findBySubject("user2 의 질문").get();
        assertThat(question.getAuthor().getUsername()).isEqualTo("user2");
    }

    @Test
    @DisplayName("GET /questions/1/modify : 글쓴이(user1)는 기존 값이 채워진 수정 폼을, 다른 회원(user2)은 403 을 받는다.")
    void t12() throws Exception {
        mvc
                .perform(get("/questions/1/modify").with(user("user1")))
                .andExpect(status().isOk())
                .andExpect(view().name("question/question/modify"))
                .andExpect(content().string(containsString("value=\"sbb가 무엇인가요?\"")))
                .andExpect(content().string(containsString("name=\"_method\" value=\"PUT\"")));

        mvc
                .perform(get("/questions/1/modify").with(user("user2")))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("PUT /questions/1/modify : 글쓴이가 수정하면 바뀌고 상세로 리다이렉트, 다른 회원은 403")
    void t13() throws Exception {
        mvc
                .perform(
                        put("/questions/1/modify")
                                .with(csrf())
                                .with(user("user2"))
                                .param("subject", "남이 바꾼 제목")
                                .param("content", "남이 바꾼 내용")
                )
                .andExpect(status().isForbidden());

        mvc
                .perform(
                        post("/questions/1/modify") // 브라우저처럼 POST + _method=PUT 으로 보낸다.
                                .param("_method", "PUT")
                                .with(csrf())
                                .with(user("user1"))
                                .param("subject", "수정된 제목")
                                .param("content", "수정된 내용")
                )
                .andExpect(handler().methodName("modify"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/questions/1"));

        Question question = questionRepository.findById(1).get();
        assertThat(question.getSubject()).isEqualTo("수정된 제목");
    }

    @Test
    @DisplayName("DELETE /questions/2 : 다른 회원은 403, 글쓴이가 삭제하면 답변까지 함께 삭제되고 목록으로 간다.")
    void t14() throws Exception {
        mvc
                .perform(delete("/questions/2").with(csrf()).with(user("user2")))
                .andExpect(status().isForbidden());

        mvc
                .perform(
                        post("/questions/2") // 브라우저(global.js)처럼 POST + _method=DELETE
                                .param("_method", "DELETE")
                                .with(csrf())
                                .with(user("user1"))
                )
                .andExpect(handler().methodName("delete"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/questions"));

        assertThat(questionRepository.findById(2)).isEmpty();
    }

    @Test
    @DisplayName("상세 화면의 수정·삭제 버튼은 글쓴이에게만 보인다.")
    void t15() throws Exception {
        mvc
                .perform(get("/questions/1").with(user("user1")))
                .andExpect(content().string(containsString("href=\"/questions/1/modify\"")))
                .andExpect(content().string(containsString("href=\"/questions/1\" method=\"DELETE\""))); // 주석 속 글자와 구분되게 href 까지

        mvc
                .perform(get("/questions/1").with(user("user2")))
                .andExpect(content().string(not(containsString("href=\"/questions/1/modify\""))))
                .andExpect(content().string(not(containsString("href=\"/questions/1\" method=\"DELETE\""))));
    }

    @Test
    @DisplayName("POST /questions/1/vote : 추천하면 추천 수가 1 늘고, 같은 회원이 다시 추천해도 그대로다.")
    void t16() throws Exception {
        for (int i = 0; i < 2; i++) { // user2 가 두 번 추천
            mvc
                    .perform(post("/questions/1/vote").with(csrf()).with(user("user2")))
                    .andExpect(handler().methodName("vote"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/questions/1"));
        }

        Question question = questionRepository.findById(1).get();
        assertThat(question.getVoters()).hasSize(1);
        assertThat(question.getVoters()).extracting("username").containsExactly("user2");
    }

    @Test
    @DisplayName("상세 화면 : 내용의 마크다운이 HTML 로 바뀌어 보인다.")
    void t17() throws Exception {
        Question question = questionService.write(null, "마크다운 질문", "**굵게** 쓴 글");

        mvc
                .perform(get("/questions/" + question.getId()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("<strong>굵게</strong> 쓴 글")));
    }

    @Test
    @DisplayName("GET /questions?kw=맞습니다 : 답변 내용으로 검색하면 그 답변이 달린 질문만 보인다.")
    void t18() throws Exception {
        mvc
                .perform(get("/questions").param("kw", "맞습니다"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(model().attribute("kw", "맞습니다"))
                .andExpect(model().attribute("paging", hasProperty("totalElements", is(1L))))
                .andExpect(content().string(containsString("스프링부트 모델 질문입니다.")))
                .andExpect(content().string(not(containsString("sbb가 무엇인가요?"))));
    }

    @Test
    @DisplayName("검색 화면 : 검색창에 검색어가 남아 있고, 페이지 링크에도 검색어가 붙는다.")
    void t19() throws Exception {
        for (int i = 1; i <= 15; i++) {
            questionService.write(null, "검색용 질문 %02d".formatted(i), "내용");
        }

        mvc
                .perform(get("/questions").param("kw", "검색용"))
                .andExpect(model().attribute("paging", hasProperty("totalElements", is(15L))))
                .andExpect(content().string(containsString("name=\"kw\" value=\"검색용\"")))
                .andExpect(content().string(containsString("href=\"/questions?page=1&amp;kw=%EA%B2%80%EC%83%89%EC%9A%A9\""))); // 한글은 URL 인코딩
    }

    @Test
    @DisplayName("GET /questions/{id}?ap=1 : 답변은 한 페이지에 10개씩, 최신 답변부터 보인다.")
    void t20() throws Exception {
        Question question = questionService.write(null, "답변 많은 질문", "내용");
        for (int i = 1; i <= 25; i++) {
            question.addAnswer(null, "답변 %02d".formatted(i));
        }
        questionRepository.flush();

        mvc
                .perform(get("/questions/" + question.getId()).param("ap", "1"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("answerPaging", hasProperty("number", is(1))))
                .andExpect(model().attribute("answerPaging", hasProperty("totalPages", is(3))))
                .andExpect(content().string(containsString("25개의 답변이 있습니다.")))
                .andExpect(content().string(containsString("답변 15"))) // 최신순 11~20번째 = 15..06
                .andExpect(content().string(containsString("답변 06")))
                .andExpect(content().string(not(containsString("답변 16"))))
                .andExpect(content().string(not(containsString("답변 05"))));
    }
}
