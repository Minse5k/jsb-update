package com.back.domain.question.category.service;

import com.back.domain.question.category.entity.Category;
import com.back.domain.question.category.repository.CategoryRepository;
import com.back.global.exception.DataNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {
    public static final String DEFAULT_CODE = "qna";

    private final CategoryRepository categoryRepository;

    // 기본 카테고리 3개를 없을 때만 만든다. (앱 시작 시 BaseInitData 가 호출)
    public void ensureDefaults() {
        create("qna", "질문답변");
        create("lecture", "강좌");
        create("free", "자유게시판");
    }

    private void create(String code, String name) {
        if (categoryRepository.findByCode(code).isEmpty()) {
            categoryRepository.save(new Category(code, name));
        }
    }

    public List<Category> findAll() {
        return categoryRepository.findAll(Sort.by("id")); // 만든 순서대로
    }

    public Category getByCode(String code) {
        return categoryRepository
                .findByCode(code)
                .orElseThrow(() -> new DataNotFoundException("category not found"));
    }

    public Category getDefault() {
        return getByCode(DEFAULT_CODE);
    }
}
