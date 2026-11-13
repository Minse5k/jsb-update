package com.back.domain.home.home.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test") // application-test.yaml(메모리 DB) 사용
@SpringBootTest // 실제 앱과 똑같이 스프링 전체를 띄운다.
@AutoConfigureMockMvc // 진짜 서버(톰캣) 없이 요청을 흉내 내는 MockMvc를 준비한다.
public class HomeControllerTest {
    @Autowired
    private MockMvc mvc;

    @Test
    @DisplayName("GET /hello 요청 시 HomeController.hello()가 Hello World를 응답한다.")
    void t1() throws Exception {
        // when : 브라우저가 GET /hello 를 보낸 것처럼 요청
        ResultActions resultActions = mvc
                .perform(get("/hello"))
                .andDo(print()); // 요청/응답 내용을 콘솔에 출력

        // then : 어떤 메서드가 처리했고, 무엇을 응답했는지 확인
        resultActions
                .andExpect(handler().handlerType(HomeController.class))
                .andExpect(handler().methodName("hello"))
                .andExpect(status().isOk())
                .andExpect(content().string("Hello World"));
    }

    @Test
    @DisplayName("GET / 요청 시 질문 목록(/questions)으로 리다이렉트한다.")
    void t2() throws Exception {
        ResultActions resultActions = mvc
                .perform(get("/"))
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(HomeController.class))
                .andExpect(handler().methodName("root"))
                .andExpect(status().is3xxRedirection()) // 302
                .andExpect(redirectedUrl("/questions"));
    }
}
