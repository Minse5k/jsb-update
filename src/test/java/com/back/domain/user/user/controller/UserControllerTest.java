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
import com.back.global.mail.DevMailService;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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
    @Autowired
    private DevMailService devMailService;

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

    @Test
    @DisplayName("POST /users/login : 맞는 아이디·비밀번호면 로그인되어 메인으로, 틀리면 /users/login?error 로 간다.")
    void t5() throws Exception {
        userService.join("loginUser", "1234", "loginUser@test.com");

        // formLogin : 로그인 폼 전송(username, password, CSRF 토큰)을 흉내 낸다.
        mvc
                .perform(formLogin("/users/login").user("loginUser").password("1234"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andExpect(authenticated().withUsername("loginUser"));

        mvc
                .perform(formLogin("/users/login").user("loginUser").password("틀린비밀번호"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/users/login?error"))
                .andExpect(unauthenticated());
    }

    @Test
    @DisplayName("내비게이션 바 : 로그인 전에는 로그인·회원가입, 로그인 후에는 아이디가 보인다.")
    void t6() throws Exception {
        mvc
                .perform(get("/users/join"))
                .andExpect(content().string(containsString("href=\"/users/login\"")))
                .andExpect(content().string(not(containsString("님</span>"))));

        mvc
                .perform(get("/users/join").with(user("navUser"))) // user() : navUser 로 로그인한 상태로 요청
                .andExpect(content().string(not(containsString("href=\"/users/login\""))))
                .andExpect(content().string(containsString("<span>navUser</span>님")));
    }

    @Test
    @DisplayName("POST /users/logout : 로그아웃되고 메인으로 리다이렉트된다.")
    void t7() throws Exception {
        mvc
                .perform(post("/users/logout").with(csrf()).with(user("logoutUser")))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andExpect(unauthenticated());
    }

    @Test
    @DisplayName("로그인 상태의 화면에는 로그아웃 링크(method=POST)와 CSRF 메타 태그가 있다.")
    void t8() throws Exception {
        mvc
                .perform(get("/users/join").with(user("navUser")))
                .andExpect(content().string(containsString("method=\"POST\" href=\"/users/logout\"")))
                .andExpect(content().string(containsString("<meta name=\"_csrf\"")));
    }

    @Test
    @DisplayName("PUT /users/password : 현재 비밀번호가 맞으면 바뀌고, 틀리면 바뀌지 않는다.")
    void t9() throws Exception {
        userService.join("pwUser", "1234", "pwUser@test.com");

        mvc
                .perform(put("/users/password").with(csrf()).with(user("pwUser"))
                        .param("currentPassword", "틀림").param("newPassword1", "5678").param("newPassword2", "5678"))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrors("form", "currentPassword"));
        assertThat(userService.matchesPassword(userService.findByUsername("pwUser").get(), "1234")).isTrue();

        mvc
                .perform(put("/users/password").with(csrf()).with(user("pwUser"))
                        .param("currentPassword", "1234").param("newPassword1", "5678").param("newPassword2", "5678"))
                .andExpect(handler().methodName("changePassword"))
                .andExpect(redirectedUrl("/users/password?changed"));
        assertThat(userService.matchesPassword(userService.findByUsername("pwUser").get(), "5678")).isTrue();

        // 바뀐 비밀번호로 로그인된다.
        mvc
                .perform(formLogin("/users/login").user("pwUser").password("5678"))
                .andExpect(authenticated().withUsername("pwUser"));
    }

    @Test
    @DisplayName("GET /users/password : 로그인하지 않으면 로그인 화면으로 보낸다.")
    void t10() throws Exception {
        mvc
                .perform(get("/users/password"))
                .andExpect(redirectedUrl("/users/login"));
    }

    @Test
    @DisplayName("POST /users/password/reset : 가입된 이메일이면 임시 비밀번호가 메일로 가고, 그 비밀번호로 로그인된다.")
    void t11() throws Exception {
        userService.join("resetUser", "1234", "resetUser@test.com");

        mvc
                .perform(post("/users/password/reset").with(csrf()).param("email", "resetUser@test.com"))
                .andExpect(handler().methodName("resetPassword"))
                .andExpect(redirectedUrl("/users/password/reset?sent"));

        DevMailService.SentMail mail = devMailService.findAll().getFirst(); // 가장 최근 메일
        assertThat(mail.to()).isEqualTo("resetUser@test.com");
        Matcher m = Pattern.compile("임시 비밀번호 : (\\S+)").matcher(mail.body());
        assertThat(m.find()).isTrue();
        String tempPassword = m.group(1);

        assertThat(userService.matchesPassword(userService.findByUsername("resetUser").get(), "1234")).isFalse();
        mvc
                .perform(formLogin("/users/login").user("resetUser").password(tempPassword))
                .andExpect(authenticated().withUsername("resetUser"));
    }

    @Test
    @DisplayName("POST /users/password/reset : 없는 이메일이어도 같은 안내를 하고 메일은 보내지 않는다.")
    void t12() throws Exception {
        int before = devMailService.findAll().size();

        mvc
                .perform(post("/users/password/reset").with(csrf()).param("email", "nobody@test.com"))
                .andExpect(redirectedUrl("/users/password/reset?sent"));

        assertThat(devMailService.findAll()).hasSize(before);
    }
}
