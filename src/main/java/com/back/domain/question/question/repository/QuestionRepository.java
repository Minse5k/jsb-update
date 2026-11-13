package com.back.domain.question.question.repository;

import com.back.domain.question.question.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;

// JpaRepository<엔티티, id 타입> 을 상속하면 save, findAll, findById, count, delete 등이 자동으로 생긴다.
public interface QuestionRepository extends JpaRepository<Question, Integer> {
}
