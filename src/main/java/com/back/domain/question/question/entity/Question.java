package com.back.domain.question.question.entity;

import com.back.domain.question.answer.entity.Answer;
import com.back.domain.question.category.entity.Category;
import com.back.domain.question.comment.entity.Comment;
import com.back.domain.user.user.entity.SiteUser;
import com.back.global.jpa.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

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

    // 게시판 분류. 질문 N개 : 카테고리 1개 → question 테이블에 category_id 외래키
    @ManyToOne(fetch = LAZY)
    private Category category;

    @Column(length = 200)
    private String subject;

    @Column(columnDefinition = "TEXT") // 글자 수 제한이 없는 칼럼
    private String content;

    // 조회 수. 이미 데이터가 있는 테이블에 칼럼을 추가해도 기존 행이 0 으로 채워지도록 DB 기본값을 둔다.
    @Column(nullable = false, columnDefinition = "INT DEFAULT 0")
    private int viewCount;

    // 질문 1개 : 답변 N개. DB 칼럼은 생기지 않고, answer.question_id 를 이용해 답변들을 찾아온다.
    // mappedBy = "question" : 외래키의 주인은 Answer.question 필드라는 뜻 (없으면 JPA가 중간 테이블을 만든다)
    // PERSIST : answers 에 새로 추가한 답변은 자동으로 INSERT
    // REMOVE : 질문을 삭제하면 답변들도 자동으로 DELETE
    // orphanRemoval : answers 에서 빼낸 답변은 자동으로 DELETE
    @OneToMany(mappedBy = "question", cascade = {PERSIST, REMOVE}, orphanRemoval = true)
    private final List<Answer> answers = new ArrayList<>(); // null 이 아니라 빈 리스트로 시작해야 add 할 때 NPE가 나지 않는다.

    // 추천한 회원들. 질문 N개 : 회원 N명 → 두 테이블만으로는 표현할 수 없어서 JPA 가 중간 테이블(question_voters)을 만든다.
    // Set : 같은 회원이 두 번 들어가지 않는다 → 한 사람이 여러 번 추천해도 1번으로 센다.
    @ManyToMany
    private final Set<SiteUser> voters = new HashSet<>();

    // 이 질문에 달린 댓글들. 넣으면 저장, 빼면 삭제 (004-04 의 답변과 같은 방식)
    @OneToMany(mappedBy = "question", cascade = {PERSIST, REMOVE}, orphanRemoval = true)
    @OrderBy("id asc") // 댓글은 오래된 것부터
    private final List<Comment> comments = new ArrayList<>();

    public Question(SiteUser author, Category category, String subject, String content) {
        this.author = author;
        this.category = category;
        this.subject = subject;
        this.content = content;
    }

    public void modify(Category category, String subject, String content) {
        this.category = category;
        this.subject = subject;
        this.content = content;
    }

    public Answer addAnswer(SiteUser author, String content) {
        Answer answer = new Answer(this, author, content);
        answers.add(answer); // 트랜잭션이 끝날 때 PERSIST 에 의해 INSERT 된다.

        return answer;
    }

    // 이 질문에 달린 답변 중 id 번 답변 (다른 질문의 답변은 찾지 않는다)
    public Optional<Answer> findAnswerById(int id) {
        return answers
                .stream()
                .filter(answer -> answer.getId() == id)
                .findFirst();
    }

    public void deleteAnswer(Answer answer) {
        answers.remove(answer); // 리스트에서 빼기만 하면 orphanRemoval 에 의해 DELETE 된다.
    }

    public void vote(SiteUser voter) {
        voters.add(voter); // 중간 테이블에 INSERT (이미 있으면 아무 일도 일어나지 않음)
    }

    public Comment addComment(SiteUser author, String content) {
        Comment comment = Comment.ofQuestion(this, author, content);
        comments.add(comment);
        return comment;
    }

    public Optional<Comment> findCommentById(int id) {
        return comments.stream().filter(c -> c.getId() == id).findFirst();
    }

    public void deleteComment(Comment comment) {
        comments.remove(comment); // orphanRemoval → DELETE
    }
}
