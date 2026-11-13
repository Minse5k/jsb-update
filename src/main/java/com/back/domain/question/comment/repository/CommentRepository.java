package com.back.domain.question.comment.repository;

import com.back.domain.question.comment.entity.Comment;
import com.back.domain.user.user.entity.SiteUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

// 댓글을 모아서 읽을 때만 쓴다. (생성·삭제는 질문·답변을 통해서)
public interface CommentRepository extends JpaRepository<Comment, Integer> {
    List<Comment> findTop10ByOrderByIdDesc(); // 최근 댓글 10개

    // 프로필 : 이 회원이 쓴 최근 댓글 10개, 쓴 댓글 수
    List<Comment> findTop10ByAuthorOrderByIdDesc(SiteUser author);

    long countByAuthor(SiteUser author);
}
