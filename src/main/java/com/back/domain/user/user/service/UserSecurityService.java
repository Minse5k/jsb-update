package com.back.domain.user.user.service;

import com.back.domain.user.user.entity.SiteUser;
import com.back.domain.user.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

// 시큐리티가 로그인을 처리할 때 "이 아이디의 회원 정보를 달라"고 부르는 서비스
// 이 빈이 있으면 시큐리티는 기본 회원(user / 콘솔 비밀번호) 대신 우리 DB의 회원으로 로그인을 처리한다.
@Service
@RequiredArgsConstructor
public class UserSecurityService implements UserDetailsService {
    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        SiteUser siteUser = userRepository
                .findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다."));

        // 권한 : 아이디가 admin 이면 관리자, 나머지는 일반 회원
        String role = "admin".equals(username) ? "ROLE_ADMIN" : "ROLE_USER";

        // 시큐리티의 User(UserDetails) 로 바꿔서 돌려준다.
        // 시큐리티가 이 안의 암호화된 비밀번호와 로그인 폼에 입력한 비밀번호를 PasswordEncoder.matches 로 비교한다.
        return new User(
                siteUser.getUsername(),
                siteUser.getPassword(),
                List.of(new SimpleGrantedAuthority(role))
        );
    }
}
