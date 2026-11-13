package com.back.domain.user.user.service;

import com.back.domain.user.user.entity.SiteUser;
import com.back.domain.user.user.repository.UserRepository;
import com.back.global.mail.MailService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder; // SecurityConfig 에 등록한 BCryptPasswordEncoder 가 들어온다.
    private final MailService mailService;

    public SiteUser join(String username, String password, String email) {
        // 비밀번호는 그대로 저장하지 않고 암호화(해시)해서 저장한다.
        // DB 가 유출되어도 원래 비밀번호를 알 수 없게 하기 위해서
        SiteUser user = new SiteUser(username, passwordEncoder.encode(password), email);

        return userRepository.save(user);
    }

    // 입력한 비밀번호가 저장된 (암호화된) 비밀번호와 맞는지
    public boolean matchesPassword(SiteUser user, String rawPassword) {
        return passwordEncoder.matches(rawPassword, user.getPassword());
    }

    public void changePassword(SiteUser user, String newRawPassword) {
        user.changePassword(passwordEncoder.encode(newRawPassword)); // 더티체킹으로 UPDATE
    }

    // 비밀번호 찾기 : 이 이메일의 회원이 있으면 임시 비밀번호로 바꾸고 메일로 보낸다.
    // 회원이 없어도 아무 일도 하지 않을 뿐 결과를 알려 주지 않는다. (가입 여부를 캐내는 데 쓰이지 않게)
    public void resetPassword(String email) {
        userRepository.findByEmail(email).ifPresent(user -> {
            String tempPassword = generateTempPassword();
            changePassword(user, tempPassword);
            mailService.send(
                    user.getEmail(),
                    "[SBB] 임시 비밀번호 안내",
                    """
                    %s님, 임시 비밀번호를 보내 드립니다.

                    임시 비밀번호 : %s

                    로그인한 뒤 '비밀번호 변경'에서 새 비밀번호로 바꿔 주세요.
                    """.formatted(user.getUsername(), tempPassword)
            );
        });
    }

    private static final String TEMP_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnpqrstuvwxyz23456789"; // 헷갈리는 0·O·1·l·I 제외
    private final SecureRandom random = new SecureRandom(); // 예측할 수 없는 난수 (Math.random 은 예측 가능)

    private String generateTempPassword() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 10; i++) {
            sb.append(TEMP_CHARS.charAt(random.nextInt(TEMP_CHARS.length())));
        }
        return sb.toString();
    }

    public Optional<SiteUser> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public Optional<SiteUser> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }
}
