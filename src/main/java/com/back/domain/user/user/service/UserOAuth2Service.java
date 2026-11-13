package com.back.domain.user.user.service;

import com.back.domain.user.user.entity.SiteUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

// 카카오 로그인 흐름에서 시큐리티가 "회원 정보를 달라"고 부르는 서비스 (폼 로그인의 UserSecurityService 와 같은 역할)
// 1. super.loadUser : 카카오에 받은 토큰으로 회원 정보(GET https://kapi.kakao.com/v2/user/me)를 받아 온다.
// 2. 카카오 회원 번호로 우리 DB 회원(SiteUser)을 찾고, 없으면 만든다.
// 3. 로그인한 사람의 이름(getName)을 우리 아이디로 맞춘다.
//    → 컨트롤러의 principal.getName() 으로 회원을 찾는 기존 코드(글쓰기, 수정 권한 등)가 그대로 동작한다.
@Service
@RequiredArgsConstructor
public class UserOAuth2Service extends DefaultOAuth2UserService {
    public static final String KAKAO_USERNAME_PREFIX = "kakao_";

    private final UserService userService;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) {
        OAuth2User kakaoUser = super.loadUser(userRequest);

        // user-name-attribute: id 로 설정했으므로 getName() 은 카카오 회원 번호
        return toPrincipal(kakaoUser.getName());
    }

    // 카카오 회원 번호 → 우리 회원 (테스트에서 카카오 요청 없이 이 부분만 확인할 수 있게 나눴다)
    public OAuth2User toPrincipal(String kakaoId) {
        SiteUser user = userService.findOrCreateSocialUser(KAKAO_USERNAME_PREFIX + kakaoId);

        return new DefaultOAuth2User(
                List.of(new SimpleGrantedAuthority("ROLE_USER")),
                Map.of("username", user.getUsername()),
                "username" // getName() 이 이 값을 돌려준다.
        );
    }
}
