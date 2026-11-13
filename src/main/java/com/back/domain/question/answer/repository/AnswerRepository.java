package com.back.domain.question.answer.repository;

import com.back.domain.question.answer.entity.Answer;
import com.back.domain.question.question.entity.Question;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

// 답변을 "페이지 단위로 읽을 때"만 쓴다. 생성·수정·삭제는 지금처럼 질문(Question)을 통해서 한다.
public interface AnswerRepository extends JpaRepository<Answer, Integer> {
    // WHERE question_id = ? + 정렬·LIMIT·OFFSET 은 pageable 이 붙인다.
    Page<Answer> findByQuestion(Question question, Pageable pageable);

    // 추천순 : 추천한 회원 수(중간 테이블 answer_voters 의 행 수)가 많은 답변부터, 같으면 최신순
    @Query(
            value = """
                    select a
                    from Answer a
                    where a.question = :question
                    order by size(a.voters) desc, a.id desc
                    """,
            countQuery = "select count(a) from Answer a where a.question = :question"
    )
    Page<Answer> findByQuestionOrderByVotes(@Param("question") Question question, Pageable pageable);
}
