package com.back.global.initData;

import com.back.domain.question.question.service.QuestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.transaction.annotation.Transactional;

@Profile("dev") // 개발 환경에서만 동작한다. (테스트는 BaseInitData 의 질문 2개만으로 결과가 항상 같아야 하므로)
@Configuration
@RequiredArgsConstructor
public class DevInitData {
    @Autowired
    @Lazy
    private DevInitData self;
    private final QuestionService questionService;

    @Bean
    @Order(2) // BaseInitData(샘플 질문 2개) 다음에 실행
    ApplicationRunner devInitDataApplicationRunner() {
        return args -> {
            self.work1();
        };
    }

    @Transactional
    public void work1() {
        if (questionService.count() >= 300) return; // 이미 충분히 있으면 건너뜀

        // 페이징을 확인할 수 있도록 질문 300개
        for (int i = 1; i <= 300; i++) {
            questionService.write("테스트 데이터입니다:[%03d]".formatted(i), "내용무");
        }
    }
}
