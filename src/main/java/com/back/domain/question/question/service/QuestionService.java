package com.back.domain.question.question.service;

import com.back.domain.question.category.entity.Category;
import com.back.domain.question.category.service.CategoryService;
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

import java.util.Optional;


@Service // 스프링이 이 클래스를 서비스 빈으로 등록한다.
@RequiredArgsConstructor
public class QuestionService {
    private final QuestionRepository questionRepository;
    private final CategoryService categoryService;

    public long count() {
        return questionRepository.count();
    }

    public Page<Question> findPage(int page, String kw) {
        // page 번째 페이지(0부터 시작), 한 페이지에 10개, 최신 글(번호가 큰 글)부터
        Pageable pageable = PageRequest.of(page, 10, Sort.by(Sort.Order.desc("id")));

        // kw 가 빈 문자열이면 like '%%' 라서 모든 질문이 나온다. (= 검색하지 않은 목록)
        return questionRepository.findAllByKeyword(kw, pageable);
        // + 전체 페이지 수를 알기 위한 count 쿼리도 함께 실행된다.
    }

    public Optional<Question> findBySubject(String subject) {
        return questionRepository.findBySubject(subject);
    }

    public void increaseViewCount(int id) {
        questionRepository.increaseViewCount(id);
    }

    public Question getQuestion(int id) {
        return questionRepository
                .findById(id)
                .orElseThrow(() -> new DataNotFoundException("question not found")); // 없으면 404
    }

    public Question write(SiteUser author, Category category, String subject, String content) {
        Question question = new Question(author, category, subject, content);

        return questionRepository.save(question); // INSERT 후 id 가 채워진 질문을 돌려준다.
    }

    // 카테고리를 고르지 않으면 기본 카테고리(질문답변)
    public Question write(SiteUser author, String subject, String content) {
        return write(author, categoryService.getDefault(), subject, content);
    }

    // 카테고리가 생기기 전에 만든 질문들을 기본 카테고리로 (이미 데이터가 있는 DB 용)
    public int assignDefaultCategory() {
        return questionRepository.assignCategoryWhereNull(categoryService.getDefault());
    }

    public void flush() {
        questionRepository.flush(); // 쌓여 있는 INSERT/UPDATE 를 지금 바로 DB 에 보낸다.
    }

    public void delete(Question question) {
        questionRepository.delete(question); // cascade REMOVE 로 답변들도 함께 삭제 (005-06)
    }

    public void modify(Question question, String subject, String content) {
        question.modify(subject, content); // 트랜잭션이 끝날 때 더티체킹으로 UPDATE (005-05), modifyDate 는 Auditing 이 갱신
    }
}
