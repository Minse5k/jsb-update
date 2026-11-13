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

    // 질문 하나의 답변 중 page 번째 페이지 (최신순)
    public Page<Answer> findPage(Question question, int page) {
        return answerRepository.findByQuestion(
                question,
                PageRequest.of(page, PAGE_SIZE, Sort.by(Sort.Order.desc("id")))
        );
    }
}
