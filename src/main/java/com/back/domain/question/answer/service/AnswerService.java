package com.back.domain.question.answer.service;

import com.back.domain.question.answer.entity.Answer;
import com.back.domain.question.answer.repository.AnswerRepository;
import com.back.domain.question.question.entity.Question;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AnswerService {
    public static final int PAGE_SIZE = 10;

    private final AnswerRepository answerRepository;

    // 정렬 기준 : 주소의 ?so= 값
    public enum SortCode {
        latest, // 최신순 (기본)
        vote    // 추천순
    }

    // 이 답변이 최신순 몇 번째 페이지에 있는지 (앞에 더 최신 답변이 몇 개 있는지로 계산)
    public int latestPageOf(Answer answer) {
        long newer = answerRepository.countByQuestionAndIdGreaterThan(answer.getQuestion(), answer.getId());
        return (int) (newer / PAGE_SIZE);
    }

    // 질문 하나의 답변 중 page 번째 페이지
    public Page<Answer> findPage(Question question, int page, SortCode sortCode) {
        if (sortCode == SortCode.vote) {
            // 정렬은 쿼리 안의 order by 가 하므로 Pageable 에는 페이지 번호와 크기만
            return answerRepository.findByQuestionOrderByVotes(question, PageRequest.of(page, PAGE_SIZE));
        }

        return answerRepository.findByQuestion(
                question,
                PageRequest.of(page, PAGE_SIZE, Sort.by(Sort.Order.desc("id")))
        );
    }
}
