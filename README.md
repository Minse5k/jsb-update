# jsb-update

『점프 투 스프링부트』의 기능을 구현하면서, 장희성 강사의 14163·14168 강의 내용을 적용해 개선하는 프로젝트입니다.

## 환경
- Spring Boot 4.1.2
- Java 25
- Gradle (Kotlin DSL)
- 의존성: Spring Web MVC, Thymeleaf, Spring Data JPA, H2, Lombok, DevTools, Validation

## 참고 자료
- [점프 투 스프링부트 교재](https://wikidocs.net/book/7601)
- [14163 강의 — 점프 투 스프링부트 개선](https://www.slog.gg/p/14163/raw)
- [14168 강의 — JPA, 타임리프 개선](https://www.slog.gg/p/14168/raw)

## 작업내역

### 001-01 프로젝트 세팅, 스프링부트 프로젝트 생성
- [링크](https://github.com/Minse5k/jsb-update/tree/001-01)
- 작업: Spring Initializr로 프로젝트 생성(Boot 4.1.2, Java 25, Gradle Kotlin DSL), H2 DB 파일을 `.gitignore`에 추가, README 작성
- 이유: 강사 실습 환경과 동일한 기반 마련
- 결과: `./gradlew test` 통과, `./gradlew bootRun`으로 정상 실행, `localhost:8080` 접속 시 아직 매핑이 없어 Whitelabel 404 응답(서버 정상 동작 확인)

### 002-01 Hello 페이지, HomeController에 GET /hello 추가
- [링크](https://github.com/Minse5k/jsb-update/tree/002-01)
- 작업: `HomeController`에 `GET /hello` → `Hello World` 응답 메서드 추가
- 이유:
  - 교재(1-03, 2-02)의 `@Controller` + `@GetMapping` + `@ResponseBody` 방식은 그대로 사용 — URL 하나와 메서드 하나가 연결되는 원리를 가장 단순하게 보여 주기 때문
  - 위치는 강의 방식 채택: 교재는 `com.mysite.sbb.HelloController`(루트 패키지)지만, 강의의 `domain.{도메인}.{세부도메인}.controller` 구조가 기능이 늘어도 파일 위치를 예측하기 쉬워서 `com.back.domain.home.home.controller.HomeController`에 둠
  - 이름은 `HelloController` 대신 `HomeController`: 교재 2-08의 루트 URL(`/`) 처리도 이 클래스에 넣을 예정이라 사이트 홈 역할의 이름이 맞음
- 결과: 브라우저에서 `localhost:8080/hello` 접속 시 `Hello World` 출력 확인

### 002-02 Hello 페이지, GET /hello MockMvc 테스트 추가
- [링크](https://github.com/Minse5k/jsb-update/tree/002-02)
- 작업: `HomeControllerTest` 작성 — `GET /hello` 요청을 보내고 처리한 컨트롤러·메서드, 응답 코드 200, 본문 `Hello World`를 검증
- 이유:
  - MockMvc 컨트롤러 테스트는 교재에도 강의(14163·14168)에도 없음. 47강 미션 규칙 7("가능한 테스트를 실행하고 결과를 확인")에 따라 추가함
    - 교재(2-05)와 14163 강의는 JUnit을 리포지터리(DB) 확인에만 사용. 컨트롤러는 요청을 보내 응답을 봐야 검증되므로 MockMvc가 필요함
    - 기능을 고친 뒤 브라우저를 일일이 열지 않아도 깨졌는지 바로 알 수 있음
  - 강의 레포(p-14163-2 `QuestionRepositoryTest`, p-14168-2 `BackApplicationTests`)에서 확인한 테스트 작성 방식은 따름: `@SpringBootTest`, `@DisplayName`, 메서드 이름 `t1`
  - `@WebMvcTest`(웹 계층만 띄움, 더 가벼움) 대신 `@SpringBootTest` + `@AutoConfigureMockMvc` — 강의 테스트와 같은 `@SpringBootTest` 기반이고, 앞으로 서비스·DB까지 이어지는 요청 전체 흐름을 같은 방식으로 테스트할 수 있음
  - `handler().handlerType()` / `handler().methodName()`으로 "어떤 컨트롤러의 어떤 메서드가 요청을 처리했는지"까지 확인 — 요청 흐름을 테스트 코드로 드러냄
- 결과: 테스트 통과, 기대값을 일부러 `Hello`로 바꾸면 실패하는 것도 확인(테스트가 실제로 검증하고 있음). DevTools 자동 재시작 확인(문자열 변경 후 컴파일 → 서버가 0.27초 만에 재시작되어 브라우저에 반영, 확인 후 원복)

### 003-01 DB 설정, yaml 전환과 dev/test 프로필 분리
- [링크](https://github.com/Minse5k/jsb-update/tree/003-01)
- 작업: `application.properties` → `application.yaml` 전환, dev/test 프로필 분리(dev: H2 파일 DB `./db_dev`, test: H2 메모리 DB), 테스트 클래스에 `@ActiveProfiles("test")`
- 이유:
  - 교재(2-03)는 `application.properties` 한 파일에 `jdbc:h2:~/local`(홈 폴더의 DB 파일)을 씀. 강의(14163 5강, 14168 레포)는 `application.yaml` + `application-dev.yaml` / `application-test.yaml`로 나눔
  - 강의 방식이 더 좋은 이유: 개발용 데이터와 테스트용 데이터가 섞이지 않음. 테스트는 매번 빈 메모리 DB에서 시작해 결과가 항상 같음
  - DB 파일을 홈 폴더가 아닌 프로젝트 폴더(`./db_dev`)에 둬서 어떤 DB를 쓰는지 바로 보임(14168 강의 환경 `jdbc:h2:./db_dev;MODE=MySQL`). `.gitignore`에는 001-01에서 이미 추가
  - H2 콘솔: 교재는 `spring.h2.console.enabled=true`를 직접 설정하지만, 강의 레포는 설정하지 않음 — DevTools가 개발 실행 시 자동으로 켜 주기 때문. 강의 방식을 따름
- 결과: 테스트 통과(test 프로필), bootRun 로그에 `profile is active: "dev"` 확인, `db_dev.mv.db` 생성 확인, `localhost:8080/h2-console` 접속 확인

### 003-02 DB 설정, JPA와 SQL 로그 설정
- [링크](https://github.com/Minse5k/jsb-update/tree/003-02)
- 작업: JPA 설정(`ddl-auto: update`)과 SQL 로그 설정(SQL 출력·정렬·색상·바인딩 값·트랜잭션 로그), `hikari.auto-commit: false`
- 이유:
  - 교재(2-03): `ddl-auto=update`, `format_sql`, `show_sql`까지만 설정
  - 강의(14163 6강 "SQL 로그 출력 설정", 14168 레포 `application.yaml`): 여기에 `highlight_sql`, `use_sql_comments`, `jdbc.bind`/`jdbc.extract` TRACE, 트랜잭션 TRACE 로그를 추가 → 강의 방식 채택
  - 강의 방식이 더 좋은 이유: SQL의 `?`에 실제로 어떤 값이 들어갔는지, 트랜잭션이 언제 시작·커밋되는지까지 로그로 보여서 "내 코드가 DB에 무엇을 시키는지" 흐름을 눈으로 따라갈 수 있음
  - `hikari.auto-commit: false`도 14168 레포 설정을 따름 — 커밋은 `@Transactional`이 끝날 때 한 번만 일어나도록
  - 교재의 `hibernate.dialect=H2Dialect`는 넣지 않음: Hibernate가 접속한 DB를 보고 자동으로 정하기 때문(강의 레포도 미설정)
- 결과: 테스트 통과. 엔티티가 아직 없어서 SQL 로그는 004에서 확인

### 004-01 질문·답변 엔티티, 공통 부모 BaseEntity와 JPA Auditing
- [링크](https://github.com/Minse5k/jsb-update/tree/004-01)
- 작업: 모든 엔티티의 공통 부모 `BaseEntity`(id, createDate, modifyDate) 생성, `BackApplication`에 `@EnableJpaAuditing`
- 이유:
  - 교재(2-04)는 `Question`, `Answer`마다 `id`, `createDate`를 각각 선언하고, 작성일시는 서비스에서 `setCreateDate(LocalDateTime.now())`로 직접 넣음. 수정일시(`modifyDate`)는 3-09에서 또 따로 추가함
  - 강의(14168 2강, 레포 `global/jpa/entity/BaseEntity`)는 공통 필드를 `@MappedSuperclass` 부모 클래스로 모으고, `@CreatedDate`/`@LastModifiedDate` + JPA Auditing으로 날짜를 자동으로 채움 → 강의 방식 채택
  - 강의 방식이 더 좋은 이유: 엔티티가 늘어나도 공통 필드를 반복해서 쓰지 않음. 날짜를 깜빡하고 안 넣는 실수가 사라짐(교재 3-09의 `setModifyDate` 같은 코드가 필요 없음)
  - id 타입은 강의처럼 `int`(교재는 `Integer`). 저장 전에는 0, 저장 후 DB가 번호를 매김
- 결과: 테스트 통과. 아직 상속받는 엔티티가 없어서 테이블은 004-02에서 확인

### 004-02 질문·답변 엔티티, Question 엔티티
- [링크](https://github.com/Minse5k/jsb-update/tree/004-02)
- 작업: `Question` 엔티티(subject 200자, content TEXT) 생성, `BaseEntity` 상속
- 이유:
  - 패키지는 강의(14168)의 `domain.{도메인}.{세부도메인}.entity` 구조를 따라 `com.back.domain.question.question.entity`. 교재(2-04, 2-06)는 `com.mysite.sbb.question` 한 폴더에 엔티티·컨트롤러·서비스를 모두 둠. 강의 방식이 계층(entity/repository/service/controller)별로 파일 위치가 정해져서 찾기 쉬움
  - 교재 2-06(도메인별로 분류하기)은 처음부터 이 구조로 만들어서 따로 하지 않음
  - 교재는 `@Setter`를 열어 두고 밖에서 값을 바꾸지만, 강의(14168 `Post`)는 `@Setter` 없이 생성자로 값을 받음 → 강의 방식 채택. 아무 데서나 값을 바꿀 수 없어서 "이 값은 어디서 바뀌지?"를 찾기 쉬움
  - `@NoArgsConstructor`: JPA가 DB에서 읽은 값으로 객체를 만들 때 빈 생성자가 필요함(교재·강의 공통)
- 결과: 테스트 로그에서 `create table question (id, create_date, modify_date, content TEXT, subject varchar(200))` 확인 — BaseEntity 칼럼이 question 테이블에 들어감

### 004-03 질문·답변 엔티티, Answer 엔티티와 @ManyToOne
- [링크](https://github.com/Minse5k/jsb-update/tree/004-03)
- 작업: `Answer` 엔티티 생성, `@ManyToOne(fetch = LAZY) Question question`
- 이유:
  - 답변은 질문의 하위 개념이라 강의(14168의 `post.postComment`)처럼 `domain.question.answer` 아래에 둠. 교재(2-06)는 `answer`를 `question`과 같은 높이의 별도 패키지로 둠
  - `@ManyToOne`은 교재와 같음: answer 테이블에 `question_id` 외래키가 생김(14163 7강 "@ManyToOne은 외래키 제약을 건다")
  - `fetch = LAZY`는 강의 방식(14163 25강 "@ManyToOne에는 꼭 fetch=LAZY", 14168 `PostComment`). 교재는 기본값(EAGER)을 그대로 씀. EAGER는 답변만 필요할 때도 질문을 매번 같이 조회해서, 강의 설명대로 필요한 때만 가져오는 LAZY가 더 합리적임
  - 강의처럼 `@Setter` 없이 생성자 `Answer(question, content)`로 생성
- 결과: 테스트 로그에서 `create table answer (..., question_id integer)`와 `foreign key (question_id) references question` 확인

### 004-04 질문·답변 엔티티, Question.answers @OneToMany와 addAnswer
- [링크](https://github.com/Minse5k/jsb-update/tree/004-04)
- 작업: `Question.answers`(`@OneToMany(mappedBy = "question", cascade = {PERSIST, REMOVE}, orphanRemoval = true)`)와 `addAnswer(content)` 메서드 추가
- 이유:
  - 교재(2-04): `@OneToMany(mappedBy = "question", cascade = CascadeType.REMOVE) List<Answer> answerList` — 초기화 없음(null), 답변 저장은 `answerRepository.save()`로 따로 함
  - 강의(14163 21·23강, 14168 37강 `Post.comments`): `cascade = {PERSIST, REMOVE}`, `orphanRemoval = true`, `= new ArrayList<>()`로 초기화, `question.addAnswer("내용")`처럼 질문을 통해 답변을 만듦 → 강의 방식 채택
  - 강의 방식이 더 좋은 이유
    - `PERSIST`: 리스트에 추가만 하면 저장됨 → 답변을 만들 때 AnswerRepository가 필요 없음(객체지향적인 방식)
    - `orphanRemoval`: 리스트에서 빼면 삭제됨 → 답변 삭제(3-09)도 질문을 통해 처리 가능
    - `new ArrayList<>()`: 새 질문에 바로 `add` 해도 NullPointerException이 나지 않음
  - `mappedBy`는 교재·강의 공통: 이게 없으면 JPA가 중간 테이블을 따로 만듦(14163 11·12강). 있으면 answer.question_id를 그대로 이용
- 결과: 테스트 통과, 테스트 로그에 테이블이 answer·question 2개만 생성됨(중간 테이블 없음 = mappedBy 동작). 실제 저장 동작은 005 리포지터리 테스트에서 확인

### 005-01 리포지터리, QuestionRepository와 샘플 데이터(BaseInitData), findAll 테스트
- [링크](https://github.com/Minse5k/jsb-update/tree/005-01)
- 작업: `QuestionRepository` 생성, 앱 시작 시 샘플 질문 2개(두 번째 질문에 답변 2개)를 만드는 `BaseInitData`, `QuestionRepositoryTest.t1`(findAll)
- 이유:
  - 리포지터리 자체는 교재(2-05)와 같음: `JpaRepository<Question, Integer>`만 상속하면 save/findAll/findById/count/delete가 생김
  - 교재(2-05)는 테스트 메서드 안에서 `questionRepository.save()`로 데이터를 넣고, 이 데이터가 개발 DB에 계속 쌓임
  - 강의(14163 14강 `TestInitData`, 14168 2강 `BaseInitData`)는 `ApplicationRunner` 빈으로 앱이 시작될 때 샘플 데이터를 만들고, `count() > 0`이면 건너뜀 → 강의 방식 채택. 서버를 켜기만 하면 화면에서 볼 데이터가 생기고, 테스트도 같은 샘플 데이터를 기준으로 검증할 수 있음
  - `self.work1()`: 강의(14168 33강)의 self 주입. `this.work1()`로 부르면 프록시를 거치지 않아 `@Transactional`이 무시됨. 자기 자신의 프록시(`@Lazy self`)를 통해 불러야 트랜잭션이 적용됨
  - 위치는 강의처럼 `global.initData` — 특정 도메인이 아니라 앱 전체의 초기 데이터라서
  - 답변은 `addAnswer`로 만들고 `save(question2)` 한 번으로 답변까지 저장(004-04의 PERSIST)
  - 테스트 클래스의 `@Transactional`: 각 테스트가 끝나면 롤백되어 테스트끼리 영향을 주지 않음(14163 18강)
- 결과: 테스트 3개 통과. bootRun 로그에 `Getting transaction for [...BaseInitData$$SpringCGLIB$$0.work1]` — 프록시(CGLIB)를 거쳐 트랜잭션이 시작됨을 확인

### 005-02 리포지터리, findById 테스트
- [링크](https://github.com/Minse5k/jsb-update/tree/005-02)
- 작업: `QuestionRepositoryTest.t2` — `findById(1)`로 질문 1개 조회
- 이유:
  - 교재(2-05)와 같은 기능. 교재는 `Optional`을 `isPresent()`로 확인한 뒤 `get()`, 강의(14163 15강 `t2`)는 샘플 데이터가 반드시 있으므로 바로 `.get()` → 테스트에서는 강의처럼 간단하게
  - `findById`가 `Optional`을 돌려주는 이유: 해당 id의 행이 없을 수 있기 때문. 실제 화면 코드에서는 없을 때 404로 처리함(010-03)
- 결과: 테스트 4개 통과

### 005-03 리포지터리, findBySubject와 findBySubjectAndContent
- [링크](https://github.com/Minse5k/jsb-update/tree/005-03)
- 작업: `findBySubject`, `findBySubjectAndContent` 메서드 추가와 테스트(t3, t4)
- 이유:
  - 메서드 이름 규칙(findBy + 칼럼명 + And + 칼럼명)으로 JPA가 WHERE 조건을 만들어 줌 — 교재(2-05)와 같은 원리
  - 리턴 타입: 교재는 `Question`(없으면 null), 강의(14163 레포 `QuestionRepository`)는 `Optional<Question>` → 강의 방식 채택. 결과가 없을 수 있다는 사실이 타입에 드러나서 null 확인을 깜빡하는 실수를 막음
- 결과: 테스트 6개 통과

### 005-04 리포지터리, findBySubjectLike
- [링크](https://github.com/Minse5k/jsb-update/tree/005-04)
- 작업: `findBySubjectLike` 추가와 테스트(t5)
- 이유:
  - 교재(2-05)·강의(14163 16·17강)와 같음. `%`의 위치로 "시작/끝/포함"을 구분
  - 결과가 여러 개일 수 있어서 리턴 타입은 `List`
  - 이 메서드는 3-13 검색 기능의 기초 개념(LIKE)이 됨
- 결과: 테스트 7개 통과

### 005-05 리포지터리, 질문 수정(더티체킹)
- [링크](https://github.com/Minse5k/jsb-update/tree/005-05)
- 작업: `Question.modify(subject, content)` 메서드 추가, 수정 테스트(t6) — `save()` 없이 값만 바꿔서 UPDATE 확인
- 이유:
  - 교재(2-05): `q.setSubject("수정된 제목"); questionRepository.save(q);` — setter로 바꾸고 save를 다시 호출
  - 강의(14168 `Post.modify`, 14163 21강 "더티체킹 == 자동 UPDATE"): 엔티티의 수정 메서드로 값을 바꾸면 트랜잭션 안에서 JPA가 변경을 감지해 자동으로 UPDATE → 강의 방식 채택
  - 강의 방식이 더 좋은 이유: `@Setter`가 없어서 값은 `modify`를 통해서만 바뀜(수정 지점이 한 곳). `save`를 깜빡해도 반영되고, "JPA는 객체를 바꾸면 DB가 따라온다"는 흐름을 그대로 보여 줌
- 결과: 테스트 8개 통과. 테스트 로그에 `update question set content=?, ... where id=?`와 바인딩 값 `[수정된 내용]` 확인 — save를 부르지 않았는데 UPDATE가 실행됨

### 005-06 리포지터리, 질문 삭제와 cascade REMOVE
- [링크](https://github.com/Minse5k/jsb-update/tree/005-06)
- 작업: 삭제 테스트(t7) — 답변 2개가 달린 질문을 삭제
- 이유:
  - 교재(2-05)는 답변이 없는 1번 질문을 삭제해서 개수만 확인함
  - 이 테스트는 답변이 달린 2번 질문을 삭제해 `cascade = REMOVE`(004-04)가 실제로 동작하는지까지 확인(14163 9강 "질문이 삭제되기 전에 관련된 답변들이 먼저 삭제된다")
  - `flush()`: 테스트는 끝나면 롤백되므로 DELETE가 실행되지 않고 끝날 수 있음. 로그로 순서를 보기 위해 강제로 실행(14163 21강의 flush 설명)
- 결과: 테스트 9개 통과. 로그 순서 `delete Answer` → `delete Answer` → `delete Question` 확인 (외래키 때문에 답변이 먼저 지워져야 함)
