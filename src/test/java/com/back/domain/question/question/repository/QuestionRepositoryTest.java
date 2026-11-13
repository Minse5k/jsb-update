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

    @Test
    @DisplayName("findBySubject : 제목으로 질문을 조회한다.")
    void t3() {
        Question question = questionRepository.findBySubject("sbb가 무엇인가요?").get();
        // SELECT * FROM question WHERE subject = 'sbb가 무엇인가요?'

        assertThat(question.getId()).isEqualTo(1);
    }

    @Test
    @DisplayName("findBySubjectAndContent : 제목과 내용이 모두 일치하는 질문을 조회한다.")
    void t4() {
        Question question = questionRepository.findBySubjectAndContent("sbb가 무엇인가요?", "sbb에 대해서 알고 싶습니다.").get();
        // SELECT * FROM question WHERE subject = 'sbb가 무엇인가요?' AND content = 'sbb에 대해서 알고 싶습니다.'

        assertThat(question.getId()).isEqualTo(1);
    }

    @Test
    @DisplayName("findBySubjectLike : 제목이 sbb 로 시작하는 질문들을 조회한다.")
    void t5() {
        List<Question> questions = questionRepository.findBySubjectLike("sbb%");
        // SELECT * FROM question WHERE subject LIKE 'sbb%'
        // sbb% : sbb 로 시작 / %sbb : sbb 로 끝남 / %sbb% : sbb 를 포함

        assertThat(questions).hasSize(1);
        assertThat(questions.get(0).getSubject()).isEqualTo("sbb가 무엇인가요?");
    }

    @Test
    @DisplayName("수정 : 엔티티의 값만 바꾸면 트랜잭션 안에서 자동으로 UPDATE 된다.(더티체킹)")
    void t6() {
        Question question = questionRepository.findById(1).get();

        question.modify("수정된 제목", "수정된 내용");
        // questionRepository.save(question) 를 호출하지 않아도 된다.
        // JPA가 조회 시점의 값과 지금 값을 비교해서 바뀐 것이 있으면 UPDATE 한다.

        Question foundQuestion = questionRepository.findBySubject("수정된 제목").get();
        // 조회 쿼리를 날리기 전에 JPA가 먼저 UPDATE 를 실행(flush)하므로 바뀐 제목으로 찾을 수 있다.

        assertThat(foundQuestion.getId()).isEqualTo(1);
    }
}
