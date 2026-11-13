package com.back.domain.user.user.controller;

import com.back.domain.user.user.service.UserService;
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
import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class UserControllerTest {
    @Autowired
    private MockMvc mvc;
    @Autowired
    private UserService userService;

    @Test
    @DisplayName("GET /users/join : 회원가입 폼이 보인다.")
    void t1() throws Exception {
        mvc
                .perform(get("/users/join"))
                .andDo(print())
                .andExpect(handler().handlerType(UserController.class))
                .andExpect(handler().methodName("showJoin"))
                .andExpect(status().isOk())
                .andExpect(view().name("user/user/join"));
    }

    @Test
    @DisplayName("POST /users/join : 회원이 가입되고 메인으로 리다이렉트된다.")
    void t2() throws Exception {
        ResultActions resultActions = mvc
                .perform(
                        post("/users/join")
                                .with(csrf())
                                .param("username", "newUser")
                                .param("password1", "1234")
                                .param("password2", "1234")
                                .param("email", "newUser@test.com")
                )
                .andDo(print());

        resultActions
                .andExpect(handler().methodName("join"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));

        assertThat(userService.findByUsername("newUser")).isPresent();
    }

    @Test
    @DisplayName("POST /users/join : 비밀번호 확인이 다르면 가입되지 않고 에러를 보여 준다.")
    void t3() throws Exception {
        mvc
                .perform(
                        post("/users/join")
                                .with(csrf())
                                .param("username", "newUser")
                                .param("password1", "1234")
                                .param("password2", "5678")
                                .param("email", "newUser@test.com")
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(view().name("user/user/join"))
                .andExpect(model().attributeHasFieldErrors("form", "password2"))
                .andExpect(content().string(containsString("2개의 비밀번호가 일치하지 않습니다.")));

        assertThat(userService.findByUsername("newUser")).isEmpty();
    }

    @Test
    @DisplayName("POST /users/join : 이미 있는 아이디면 가입되지 않고 에러를 보여 준다.")
    void t4() throws Exception {
        userService.join("dupUser", "1234", "dup@test.com");

        mvc
                .perform(
                        post("/users/join")
                                .with(csrf())
                                .param("username", "dupUser")
                                .param("password1", "1234")
                                .param("password2", "1234")
                                .param("email", "other@test.com")
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrors("form", "username"))
                .andExpect(content().string(containsString("이미 사용 중인 아이디입니다.")));
    }
}
