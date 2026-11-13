package com.back.global.mail;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
public class DevMailboxControllerTest {
    @Autowired
    private MockMvc mvc;
    @Autowired
    private MailService mailService; // test 프로필이라 DevMailService 가 들어온다.

    @Test
    @DisplayName("개발용 메일함 : 보낸 메일이 /dev/mailbox 에 보인다.")
    void t1() throws Exception {
        mailService.send("someone@test.com", "메일함 확인용 제목", "메일함 확인용 본문");

        mvc
                .perform(get("/dev/mailbox"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("메일함 확인용 제목")))
                .andExpect(content().string(containsString("someone@test.com")))
                .andExpect(content().string(containsString("메일함 확인용 본문")));
    }
}
