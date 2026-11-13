package com.back.domain.question.question.repository;

import com.back.domain.question.question.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

// JpaRepository<엔티티, id 타입> 을 상속하면 save, findAll, findById, count, delete 등이 자동으로 생긴다.
public interface QuestionRepository extends JpaRepository<Question, Integer> {
    // 메서드 이름만 규칙대로 지으면 JPA가 SQL을 만들어 준다.
    Optional<Question> findBySubject(String subject); // WHERE subject = ?

    Optional<Question> findBySubjectAndContent(String subject, String content); // WHERE subject = ? AND content = ?
}
