package com.back.global.jpa.entity;

import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

import static jakarta.persistence.GenerationType.IDENTITY;

@MappedSuperclass // 이 클래스 자체는 테이블이 되지 않고, 상속받은 엔티티의 테이블에 아래 칼럼들이 들어간다.
@EntityListeners(AuditingEntityListener.class) // 저장/수정 시점에 @CreatedDate, @LastModifiedDate 값을 자동으로 채운다.
@Getter
public abstract class BaseEntity {
    @Id
    @GeneratedValue(strategy = IDENTITY) // DB의 auto increment로 번호를 매긴다.
    private int id;

    @CreatedDate
    private LocalDateTime createDate;

    @LastModifiedDate
    private LocalDateTime modifyDate;
}
