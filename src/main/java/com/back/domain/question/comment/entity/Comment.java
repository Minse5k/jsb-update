package com.back.domain.question.comment.entity;

import com.back.domain.question.answer.entity.Answer;
import com.back.domain.question.question.entity.Question;
import com.back.domain.user.user.entity.SiteUser;
import com.back.global.jpa.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static jakarta.persistence.FetchType.LAZY;

// 댓글. 질문에 다는 댓글이면 question, 답변에 다는 댓글이면 answer 중 하나만 채워진다.
@Entity
@Getter
@NoArgsConstructor
public class Comment extends BaseEntity {
    @ManyToOne(fetch = LAZY)
    private Question question; // 질문 댓글일 때

    @ManyToOne(fetch = LAZY)
    private Answer answer; // 답변 댓글일 때

    @ManyToOne(fetch = LAZY)
    private SiteUser author;

    @Column(length = 500)
    private String content;

    private Comment(Question question, Answer answer, SiteUser author, String content) {
        this.question = question;
        this.answer = answer;
        this.author = author;
        this.content = content;
    }

    public static Comment ofQuestion(Question question, SiteUser author, String content) {
        return new Comment(question, null, author, content);
    }

    public static Comment ofAnswer(Answer answer, SiteUser author, String content) {
        return new Comment(null, answer, author, content);
    }

    // 이 댓글이 결국 어느 질문 화면에 보이는지 (답변 댓글이면 그 답변의 질문)
    public Question getTargetQuestion() {
        return question != null ? question : answer.getQuestion();
    }

    // 이 댓글을 화면에서 볼 수 있는 주소 : 질문 댓글이면 질문의 댓글 영역, 답변 댓글이면 답변 하나의 주소(030-01)
    public String getLink() {
        if (question != null) return "/questions/%d#question_comments".formatted(question.getId());
        return "/questions/%d/answers/%d".formatted(answer.getQuestion().getId(), answer.getId());
    }

    public void modify(String content) {
        this.content = content;
    }
}
