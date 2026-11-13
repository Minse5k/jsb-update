package com.back.domain.question.question.entity;

import com.back.global.jpa.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity // 이 클래스를 보고 question 테이블을 만든다.
@Getter
@NoArgsConstructor // JPA가 DB에서 읽어 온 값으로 객체를 만들 때 빈 생성자가 필요하다.
public class Question extends BaseEntity { // id, createDate, modifyDate 는 BaseEntity 에서 상속
    @Column(length = 200)
    private String subject;

    @Column(columnDefinition = "TEXT") // 글자 수 제한이 없는 칼럼
    private String content;

    public Question(String subject, String content) {
        this.subject = subject;
        this.content = content;
    }
}
