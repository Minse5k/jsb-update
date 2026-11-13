package com.back.domain.question.answer.repository;

import com.back.domain.question.answer.entity.Answer;
import com.back.domain.question.question.entity.Question;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

// 답변을 "페이지 단위로 읽을 때"만 쓴다. 생성·수정·삭제는 지금처럼 질문(Question)을 통해서 한다.
public interface AnswerRepository extends JpaRepository<Answer, Integer> {
    // WHERE question_id = ? + 정렬·LIMIT·OFFSET 은 pageable 이 붙인다.
    Page<Answer> findByQuestion(Question question, Pageable pageable);
}
