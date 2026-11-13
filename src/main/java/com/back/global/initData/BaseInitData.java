package com.back.global.initData;

import com.back.domain.question.question.entity.Question;
import com.back.domain.question.question.service.QuestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.transaction.annotation.Transactional;

@Configuration
@RequiredArgsConstructor
public class BaseInitData {
    // 자기 자신(프록시)을 주입받는다.
    // work1() 을 this.work1() 로 부르면 @Transactional 이 적용되지 않기 때문에 self.work1() 로 부른다.
    @Autowired
    @Lazy
    private BaseInitData self;
    private final QuestionService questionService; // 컨트롤러와 같이 서비스를 통해서 데이터를 만든다.

    @Bean
    ApplicationRunner baseInitDataApplicationRunner() {
        // 스프링부트가 시작을 마치면 자동으로 실행된다.
        return args -> {
            self.work1();
        };
    }

    @Transactional
    public void work1() {
        if (questionService.count() > 0) return; // 이미 데이터가 있으면 다시 만들지 않는다.

        questionService.write("sbb가 무엇인가요?", "sbb에 대해서 알고 싶습니다.");

        Question question2 = questionService.write("스프링부트 모델 질문입니다.", "id는 자동으로 생성되나요?");
        question2.addAnswer("네 자동으로 생성됩니다."); // 트랜잭션(work1)이 끝날 때 PERSIST 에 의해 INSERT
        question2.addAnswer("맞습니다!");
    }
}
