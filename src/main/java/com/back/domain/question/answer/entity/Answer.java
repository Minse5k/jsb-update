package com.back.domain.question.answer.entity;

import com.back.domain.question.question.entity.Question;
import com.back.domain.user.user.entity.SiteUser;
import com.back.global.jpa.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static jakarta.persistence.FetchType.LAZY;

@Entity
@Getter
@NoArgsConstructor
public class Answer extends BaseEntity {
    // 답변 N개 : 질문 1개 → answer 테이블에 question_id 외래키 칼럼이 생긴다.
    // LAZY : 답변을 조회할 때 질문은 바로 가져오지 않고, 실제로 사용할 때 가져온다.
    @ManyToOne(fetch = LAZY)
    private Question question;

    @ManyToOne(fetch = LAZY) // 글쓴이 → answer 테이블에 author_id 외래키
    private SiteUser author;

    @Column(columnDefinition = "TEXT")
    private String content;

    public Answer(Question question, SiteUser author, String content) {
        this.question = question;
        this.author = author;
        this.content = content;
    }

    public void modify(String content) {
        this.content = content;
    }
}
