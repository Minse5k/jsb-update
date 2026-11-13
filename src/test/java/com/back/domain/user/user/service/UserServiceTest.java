package com.back.domain.user.user.service;

import com.back.domain.user.user.entity.SiteUser;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@SpringBootTest
@Transactional
public class UserServiceTest {
    @Autowired
    private UserService userService;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    @DisplayName("join : 비밀번호는 암호화되어 저장되고, 원래 비밀번호와 matches 로만 비교할 수 있다.")
    void t1() {
        SiteUser user = userService.join("user1", "1234", "user1@test.com");

        assertThat(user.getId()).isGreaterThan(0);
        assertThat(user.getPassword()).isNotEqualTo("1234"); // 그대로 저장되지 않음
        assertThat(user.getPassword()).startsWith("$2"); // BCrypt 해시 형식
        assertThat(passwordEncoder.matches("1234", user.getPassword())).isTrue();
        assertThat(userService.findByUsername("user1")).isPresent();
    }
}
