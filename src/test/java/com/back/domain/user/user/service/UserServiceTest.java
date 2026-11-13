package com.back.domain.user.user.service;

import com.back.domain.user.user.entity.SiteUser;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ActiveProfiles("test")
@SpringBootTest
@Transactional
public class UserServiceTest {
    @Autowired
    private UserService userService;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private UserSecurityService userSecurityService;

    @Test
    @DisplayName("join : 비밀번호는 암호화되어 저장되고, 원래 비밀번호와 matches 로만 비교할 수 있다.")
    void t1() {
        SiteUser user = userService.join("joinUser1", "1234", "joinUser1@test.com");

        assertThat(user.getId()).isGreaterThan(0);
        assertThat(user.getPassword()).isNotEqualTo("1234"); // 그대로 저장되지 않음
        assertThat(user.getPassword()).startsWith("$2"); // BCrypt 해시 형식
        assertThat(passwordEncoder.matches("1234", user.getPassword())).isTrue();
        assertThat(userService.findByUsername("joinUser1")).isPresent();
    }

    @Test
    @DisplayName("loadUserByUsername : 시큐리티가 로그인할 때 쓰는 회원 정보(UserDetails)를 만든다.")
    void t2() {
        userService.join("joinUser2", "1234", "joinUser2@test.com");

        UserDetails userDetails = userSecurityService.loadUserByUsername("joinUser2");

        assertThat(userDetails.getUsername()).isEqualTo("joinUser2");
        assertThat(passwordEncoder.matches("1234", userDetails.getPassword())).isTrue();
        assertThat(userDetails.getAuthorities()).extracting("authority").containsExactly("ROLE_USER");
        assertThatThrownBy(() -> userSecurityService.loadUserByUsername("없는회원"))
                .isInstanceOf(UsernameNotFoundException.class);
    }
}
