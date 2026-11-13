package com.back.domain.question.question.entity;

import com.back.domain.question.answer.entity.Answer;
import com.back.domain.user.user.entity.SiteUser;
import com.back.global.jpa.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

import static jakarta.persistence.CascadeType.PERSIST;
import static jakarta.persistence.CascadeType.REMOVE;
import static jakarta.persistence.FetchType.LAZY;

@Entity // 이 클래스를 보고 question 테이블을 만든다.
@Getter
@NoArgsConstructor // JPA가 DB에서 읽어 온 값으로 객체를 만들 때 빈 생성자가 필요하다.
public class Question extends BaseEntity { // id, createDate, modifyDate 는 BaseEntity 에서 상속
    // 글쓴이. 질문 N개 : 회원 1명 → question 테이블에 author_id 외래키
    @ManyToOne(fetch = LAZY)
    private SiteUser author;

    @Column(length = 200)
    private String subject;

    @Column(columnDefinition = "TEXT") // 글자 수 제한이 없는 칼럼
    private String content;

    // 질문 1개 : 답변 N개. DB 칼럼은 생기지 않고, answer.question_id 를 이용해 답변들을 찾아온다.
    // mappedBy = "question" : 외래키의 주인은 Answer.question 필드라는 뜻 (없으면 JPA가 중간 테이블을 만든다)
    // PERSIST : answers 에 새로 추가한 답변은 자동으로 INSERT
    // REMOVE : 질문을 삭제하면 답변들도 자동으로 DELETE
    // orphanRemoval : answers 에서 빼낸 답변은 자동으로 DELETE
    @OneToMany(mappedBy = "question", cascade = {PERSIST, REMOVE}, orphanRemoval = true)
    private final List<Answer> answers = new ArrayList<>(); // null 이 아니라 빈 리스트로 시작해야 add 할 때 NPE가 나지 않는다.

    public Question(SiteUser author, String subject, String content) {
        this.author = author;
        this.subject = subject;
        this.content = content;
    }

    public void modify(String subject, String content) {
        this.subject = subject;
        this.content = content;
    }

    public Answer addAnswer(SiteUser author, String content) {
        Answer answer = new Answer(this, author, content);
        answers.add(answer); // 트랜잭션이 끝날 때 PERSIST 에 의해 INSERT 된다.

        return answer;
    }
}
