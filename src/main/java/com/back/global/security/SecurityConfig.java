package com.back.global.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {
    @Bean // 이 빈이 있으면 시큐리티 기본 설정 대신 아래 규칙을 쓴다.
    public SecurityFilterChain filterChain(HttpSecurity http) {
        http
                .authorizeHttpRequests(
                        auth -> auth
                                .requestMatchers("/favicon.ico").permitAll()
                                .requestMatchers("/resource/**").permitAll() // 정적 파일(css, js) - 012-01 에서 한 폴더로 모아 둔 이유
                                .requestMatchers("/h2-console/**").permitAll()
                                .requestMatchers("/**").permitAll() // 일단 모든 요청 허용 (로그인이 필요한 곳은 이후 단계에서 막는다)
                                .anyRequest().authenticated()
                )
                .headers(
                        headers -> headers
                                .frameOptions(
                                        // H2 콘솔은 화면을 <frame> 으로 나눠 그린다. 같은 사이트 안의 frame 은 허용
                                        HeadersConfigurer.FrameOptionsConfig::sameOrigin
                                )
                )
                .csrf(
                        csrf -> csrf
                                .ignoringRequestMatchers("/h2-console/**") // H2 콘솔 폼에는 CSRF 토큰이 없으므로 검사 제외
                );

        return http.build();
    }
}
