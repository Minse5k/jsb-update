package com.back.domain.question.question.repository;

import com.back.domain.question.question.entity.Question;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

// JpaRepository<엔티티, id 타입> 을 상속하면 save, findAll, findById, count, delete 등이 자동으로 생긴다.
public interface QuestionRepository extends JpaRepository<Question, Integer> {
    // 메서드 이름만 규칙대로 지으면 JPA가 SQL을 만들어 준다.
    Optional<Question> findBySubject(String subject); // WHERE subject = ?

    Optional<Question> findBySubjectAndContent(String subject, String content); // WHERE subject = ? AND content = ?

    List<Question> findBySubjectLike(String subjectLike); // WHERE subject LIKE ?  (결과가 여러 개일 수 있어서 List)

    // 검색 : 질문 제목·내용·글쓴이, 답변 내용·글쓴이 중 하나라도 kw 를 포함하면 결과에 넣는다.
    // 메서드 이름 규칙으로는 표현하기 어려워서 JPQL(엔티티 기준 SQL)을 직접 쓴다.
    // left join : 답변·글쓴이가 없는 질문도 빠지지 않게
    // distinct : 답변이 여러 개 걸리면 같은 질문이 여러 줄 나오므로 하나로
    @Query("""
            select distinct q
            from Question q
            left join q.author u1
            left join q.answers a
            left join a.author u2
            where q.subject like %:kw%
               or q.content like %:kw%
               or u1.username like %:kw%
               or a.content like %:kw%
               or u2.username like %:kw%
            """)
    Page<Question> findAllByKeyword(@Param("kw") String kw, Pageable pageable);

    // 조회 수 +1 을 DB 에서 한 번에 : UPDATE question SET view_count = view_count + 1 WHERE id = ?
    // 읽고(10) → 더하고(11) → 저장하는 방식은 두 사람이 동시에 열면 둘 다 11 을 저장해 하나가 사라진다.
    // clearAutomatically : 실행 후 영속성 컨텍스트를 비워서, 이어서 조회하는 질문이 바뀐 값을 읽게 한다.
    @Modifying(clearAutomatically = true)
    @Query("update Question q set q.viewCount = q.viewCount + 1 where q.id = :id")
    int increaseViewCount(@Param("id") int id);
}
