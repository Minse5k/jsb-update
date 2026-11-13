package com.back.domain.question.question.repository;

import com.back.domain.question.question.entity.Question;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@SpringBootTest
@Transactional // 각 테스트가 끝나면 롤백 → 테스트끼리 서로 영향을 주지 않는다.
public class QuestionRepositoryTest {
    @Autowired
    private QuestionRepository questionRepository;

    @Test
    @DisplayName("findAll : BaseInitData 가 만든 샘플 질문 2개를 조회한다.")
    void t1() {
        List<Question> questions = questionRepository.findAll();
        // SELECT * FROM question

        assertThat(questions).hasSize(2);

        Question question = questions.get(0);
        assertThat(question.getSubject()).isEqualTo("sbb가 무엇인가요?");
    }

    @Test
    @DisplayName("findById : id 로 질문 1개를 조회한다.")
    void t2() {
        Question question = questionRepository.findById(1).get();
        // SELECT * FROM question WHERE id = 1
        // findById 는 Optional 을 리턴한다. 없을 수도 있기 때문. 여기서는 있다고 확신하므로 get()

        assertThat(question.getSubject()).isEqualTo("sbb가 무엇인가요?");
    }
}
