package com.back.domain.question.answer.entity;

import com.back.domain.question.comment.entity.Comment;
import com.back.domain.question.question.entity.Question;
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

    @ManyToMany // 추천한 회원들 → 중간 테이블 answer_voters
    private final Set<SiteUser> voters = new HashSet<>();

    // 이 답변에 달린 댓글들. 넣으면 저장, 빼면 삭제 (004-04 의 답변과 같은 방식)
    @OneToMany(mappedBy = "answer", cascade = {PERSIST, REMOVE}, orphanRemoval = true)
    @OrderBy("id asc") // 댓글은 오래된 것부터
    private final List<Comment> comments = new ArrayList<>();

    public Answer(Question question, SiteUser author, String content) {
        this.question = question;
        this.author = author;
        this.content = content;
    }

    public void modify(String content) {
        this.content = content;
    }

    public void vote(SiteUser voter) {
        voters.add(voter);
    }

    public Comment addComment(SiteUser author, String content) {
        Comment comment = Comment.ofAnswer(this, author, content);
        comments.add(comment);
        return comment;
    }

    public Optional<Comment> findCommentById(int id) {
        return comments.stream().filter(c -> c.getId() == id).findFirst();
    }

    public void deleteComment(Comment comment) {
        comments.remove(comment);
    }
}
