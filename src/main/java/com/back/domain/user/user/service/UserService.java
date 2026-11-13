package com.back.domain.user.user.service;

import com.back.domain.user.user.entity.SiteUser;
import com.back.domain.user.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder; // SecurityConfig 에 등록한 BCryptPasswordEncoder 가 들어온다.

    public SiteUser join(String username, String password, String email) {
        // 비밀번호는 그대로 저장하지 않고 암호화(해시)해서 저장한다.
        // DB 가 유출되어도 원래 비밀번호를 알 수 없게 하기 위해서
        SiteUser user = new SiteUser(username, passwordEncoder.encode(password), email);

        return userRepository.save(user);
    }

    public Optional<SiteUser> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public Optional<SiteUser> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }
}
