package com.back.global.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableMethodSecurity // 컨트롤러 메서드에 @PreAuthorize 를 붙여 메서드 단위로 권한을 검사할 수 있게 한다.
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
                )
                .formLogin(
                        formLogin -> formLogin
                                .loginPage("/users/login") // 로그인 화면 : GET /users/login (우리가 만든 화면)
                                .loginProcessingUrl("/users/login") // 로그인 처리 : POST /users/login (시큐리티가 처리, 컨트롤러 메서드 없음)
                                .defaultSuccessUrl("/") // 성공하면 메인으로
                )
                .oauth2Login(
                        oauth2Login -> oauth2Login
                                .loginPage("/users/login") // 카카오 로그인 버튼도 우리 로그인 화면에 둔다.
                                .defaultSuccessUrl("/")
                                // 카카오 버튼 : GET /oauth2/authorization/kakao → 카카오 로그인 화면으로 리다이렉트
                                // 카카오가 돌려보내는 주소 : GET /login/oauth2/code/kakao (시큐리티가 처리)
                )
                .logout(
                        logout -> logout
                                .logoutUrl("/users/logout") // 로그아웃 처리 : POST /users/logout (CSRF 때문에 GET 으로는 안 된다)
                                .logoutSuccessUrl("/") // 로그아웃 후 메인으로 (세션은 기본으로 무효화된다)
                );

        return http.build();
    }

    @Bean // 비밀번호 암호화 도구를 빈으로 등록 → UserService 에서 주입받아 사용
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
