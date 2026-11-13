package com.back.domain.question.question.repository;

import com.back.domain.question.question.entity.Question;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import com.back.domain.question.answer.entity.Answer;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

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
        assertThat(question.getAuthor().getUsername()).isEqualTo("user1"); // 글쓴이 (LAZY → 이때 회원 조회)
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

        question.modify(question.getCategory(), "수정된 제목", "수정된 내용");
        // questionRepository.save(question) 를 호출하지 않아도 된다.
        // JPA가 조회 시점의 값과 지금 값을 비교해서 바뀐 것이 있으면 UPDATE 한다.

        Question foundQuestion = questionRepository.findBySubject("수정된 제목").get();
        // 조회 쿼리를 날리기 전에 JPA가 먼저 UPDATE 를 실행(flush)하므로 바뀐 제목으로 찾을 수 있다.

        assertThat(foundQuestion.getId()).isEqualTo(1);
    }

    @Test
    @DisplayName("삭제 : 질문을 삭제하면 cascade REMOVE 에 의해 답변들도 함께 삭제된다.")
    void t7() {
        assertThat(questionRepository.count()).isEqualTo(2);

        Question question = questionRepository.findById(2).get(); // 답변이 2개 달린 질문
        questionRepository.delete(question);
        questionRepository.flush(); // DELETE 쿼리를 지금 바로 실행 (로그에서 확인용)
        // 실행 순서 : DELETE FROM answer ... (2번) → DELETE FROM question WHERE id = 2

        assertThat(questionRepository.count()).isEqualTo(1);
    }

    @Test
    @DisplayName("답변 생성 : question.addAnswer() 만 호출하면 PERSIST 에 의해 INSERT 된다.")
    void t8() {
        Question question = questionRepository.findById(2).get();
        int beforeCount = question.getAnswers().size();

        Answer answer = question.addAnswer(question.getAuthor(), "네 자동으로 생성됩니다.");
        // AnswerRepository.save() 가 필요 없다.

        assertThat(answer.getId()).isEqualTo(0); // 아직 INSERT 전이라 id 가 없다.(int 기본값 0)

        questionRepository.flush(); // 지금 INSERT 실행

        assertThat(answer.getId()).isGreaterThan(0); // INSERT 후 DB가 매긴 번호가 들어온다.
        assertThat(question.getAnswers()).hasSize(beforeCount + 1);
    }

    @Test
    @DisplayName("답변 조회 : question.getAnswers() 로 질문에 달린 답변들을 조회한다.")
    void t9() {
        Question question = questionRepository.findById(2).get();

        List<Answer> answers = question.getAnswers();
        // LAZY 이므로 이 시점에 SELECT * FROM answer WHERE question_id = 2 가 실행된다.

        assertThat(answers).hasSize(2);
        assertThat(answers.get(0).getContent()).isEqualTo("네 자동으로 생성됩니다.");
    }

    @Test
    @DisplayName("수정일시 : 처음 저장할 때는 작성일시와 같고, 수정하면 작성일시보다 뒤가 된다.(JPA Auditing)")
    void t10() throws Exception {
        Question question = questionRepository.save(new Question(null, null, "날짜 확인", "내용"));
        questionRepository.flush();

        assertThat(question.getModifyDate()).isEqualTo(question.getCreateDate());

        Thread.sleep(10); // 시간이 조금 흐른 뒤에 수정
        question.modify(null, "날짜 확인 (수정)", "내용");
        questionRepository.flush(); // UPDATE 직전에 @LastModifiedDate 가 갱신된다.

        assertThat(question.getModifyDate()).isAfter(question.getCreateDate());
    }

    @Test
    @DisplayName("findAllByKeyword : 질문 제목·내용·글쓴이, 답변 내용·글쓴이로 검색한다.")
    void t11() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Order.desc("id")));

        // 질문 제목
        assertThat(questionRepository.findAllByKeyword("", "sbb가", pageable).getContent())
                .extracting("subject").containsExactly("sbb가 무엇인가요?");
        // 답변 내용 ("맞습니다!" 는 2번 질문의 답변)
        assertThat(questionRepository.findAllByKeyword("", "맞습니다", pageable).getContent())
                .extracting("id").containsExactly(2);
        // 답변 글쓴이 (user2 는 2번 질문에만 답변) - 답변 2개가 걸려도 distinct 로 한 번만
        assertThat(questionRepository.findAllByKeyword("", "user2", pageable).getContent())
                .extracting("id").containsExactly(2);
        // 질문 글쓴이 (user1 은 두 질문 모두)
        assertThat(questionRepository.findAllByKeyword("", "user1", pageable).getTotalElements()).isEqualTo(2);
        // 빈 검색어 = 전체
        assertThat(questionRepository.findAllByKeyword("", "", pageable).getTotalElements()).isEqualTo(2);
    }

    @Test
    @DisplayName("카테고리 : 기본 카테고리 3개가 있고, 샘플 질문은 질문답변 카테고리다.")
    void t12() {
        Question question = questionRepository.findById(1).get();

        assertThat(question.getCategory().getCode()).isEqualTo("qna");
        assertThat(question.getCategory().getName()).isEqualTo("질문답변");
    }
}
