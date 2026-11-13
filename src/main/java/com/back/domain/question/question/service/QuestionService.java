package com.back.domain.question.question.service;

import com.back.domain.question.question.entity.Question;
import com.back.domain.question.question.repository.QuestionRepository;
import com.back.global.exception.DataNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service // 스프링이 이 클래스를 서비스 빈으로 등록한다.
@RequiredArgsConstructor
public class QuestionService {
    private final QuestionRepository questionRepository;

    public List<Question> findAll() {
        return questionRepository.findAll();
    }

    public Question getQuestion(int id) {
        return questionRepository
                .findById(id)
                .orElseThrow(() -> new DataNotFoundException("question not found")); // 없으면 404
    }
}
