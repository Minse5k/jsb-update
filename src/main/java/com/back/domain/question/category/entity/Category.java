package com.back.domain.question.category.entity;

import com.back.global.jpa.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 게시판 분류 : 질문답변, 강좌, 자유게시판
@Entity
@Getter
@NoArgsConstructor
public class Category extends BaseEntity {
    @Column(unique = true, length = 30)
    private String code; // 주소에 쓰는 값 : qna, lecture, free

    @Column(length = 30)
    private String name; // 화면에 보이는 이름

    public Category(String code, String name) {
        this.code = code;
        this.name = name;
    }
}
