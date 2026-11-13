package com.back.domain.question.question.service;

import com.back.domain.question.question.entity.Question;
import com.back.domain.question.question.repository.QuestionRepository;
import com.back.domain.user.user.entity.SiteUser;
import com.back.global.exception.DataNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;


@Service // 스프링이 이 클래스를 서비스 빈으로 등록한다.
@RequiredArgsConstructor
public class QuestionService {
    private final QuestionRepository questionRepository;

    public long count() {
        return questionRepository.count();
    }

    public Page<Question> findPage(int page) {
        // page 번째 페이지(0부터 시작), 한 페이지에 10개, 최신 글(번호가 큰 글)부터
        Pageable pageable = PageRequest.of(page, 10, Sort.by(Sort.Order.desc("id")));

        return questionRepository.findAll(pageable);
        // SELECT ... FROM question ORDER BY id DESC LIMIT 10 OFFSET (page * 10)
        // + 전체 페이지 수를 알기 위한 SELECT count(*) FROM question
    }

    public Question getQuestion(int id) {
        return questionRepository
                .findById(id)
                .orElseThrow(() -> new DataNotFoundException("question not found")); // 없으면 404
    }

    public Question write(SiteUser author, String subject, String content) {
        Question question = new Question(author, subject, content);

        return questionRepository.save(question); // INSERT 후 id 가 채워진 질문을 돌려준다.
    }

    public void delete(Question question) {
        questionRepository.delete(question); // cascade REMOVE 로 답변들도 함께 삭제 (005-06)
    }

    public void modify(Question question, String subject, String content) {
        question.modify(subject, content); // 트랜잭션이 끝날 때 더티체킹으로 UPDATE (005-05), modifyDate 는 Auditing 이 갱신
    }
}
