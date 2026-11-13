package com.back.global.initData;

import com.back.domain.question.question.entity.Question;
import com.back.domain.question.question.service.QuestionService;
import com.back.domain.user.user.entity.SiteUser;
import com.back.domain.user.user.service.UserService;
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
    private final UserService userService;

    @Bean
    @Order(2) // BaseInitData(샘플 질문 2개) 다음에 실행
    ApplicationRunner devInitDataApplicationRunner() {
        return args -> {
            self.work1();
            self.work2();
        };
    }

    @Transactional
    public void work1() {
        if (questionService.count() >= 300) return; // 이미 충분히 있으면 건너뜀

        SiteUser user1 = userService.findByUsername("user1").get(); // BaseInitData 가 먼저 만든 회원

        // 페이징을 확인할 수 있도록 질문 300개
        for (int i = 1; i <= 300; i++) {
            questionService.write(user1, "테스트 데이터입니다:[%03d]".formatted(i), "내용무");
        }
    }

    @Transactional
    public void work2() {
        // 답변 페이징을 확인할 수 있도록 답변 25개가 달린 질문 하나
        String subject = "답변이 많은 질문입니다";
        if (questionService.findBySubject(subject).isPresent()) return;

        SiteUser user1 = userService.findByUsername("user1").get();
        SiteUser user2 = userService.findByUsername("user2").get();

        Question question = questionService.write(user1, subject, "답변 페이징 확인용 질문입니다.");
        for (int i = 1; i <= 25; i++) {
            question.addAnswer(user2, "%d번째 답변입니다.".formatted(i));
        }
    }
}
