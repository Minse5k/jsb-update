package com.back.domain.user.user.entity;

import com.back.global.jpa.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 이름이 User 가 아닌 이유 : 스프링 시큐리티에 이미 User 클래스가 있어서 헷갈리지 않게 (교재와 같은 이유)
// 테이블 이름도 user 는 일부 DB 에서 예약어라 site_user 가 안전하다.
@Entity
@Getter
@NoArgsConstructor
public class SiteUser extends BaseEntity {
    @Column(unique = true) // 같은 아이디로 두 번 가입할 수 없다. (DB 가 막는다)
    private String username;

    private String password; // 암호화된 비밀번호가 저장된다.

    @Column(unique = true)
    private String email;

    public SiteUser(String username, String password, String email) {
        this.username = username;
        this.password = password;
        this.email = email;
    }

    public void changePassword(String encodedPassword) {
        this.password = encodedPassword; // 암호화된 값만 받는다.
    }
}
