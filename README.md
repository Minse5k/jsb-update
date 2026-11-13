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

### 005-07 리포지터리, 답변 생성과 조회를 질문을 통해서
- [링크](https://github.com/Minse5k/jsb-update/tree/005-07)
- 작업: 답변 생성 테스트(t8, `question.addAnswer()`), 답변 조회 테스트(t9, `question.getAnswers()`)
- 이유:
  - 교재(2-05): `AnswerRepository`를 만들고 `answerRepository.save(answer)`로 생성, `answerRepository.findById()`로 조회
  - 강의(14163 9·21강, 레포 t9·t11): `@OneToMany` 필드로 생성·조회하는 객체지향적인 방법을 함께 보여 주고, 실제 답변 등록 기능(14163 37강 "강사는 oneToMany 필드를 통해서 답변을 생성")도 이 방식으로 만듦 → 강의 방식 채택
  - 그래서 `AnswerRepository`는 만들지 않음. 답변은 항상 질문을 통해서 만들고 찾기 때문에 필요가 없음(14163 9강 "answerRepository를 주입받아야 한다. 귀찮다")
  - t8의 `id == 0` → `flush()` → `id > 0`: INSERT는 트랜잭션 끝(또는 flush)에 일어나고, 그때 DB가 번호를 매긴다는 흐름을 보여 줌(14163 21강)
- 결과: 테스트 11개 통과

### 006-01 질문 목록, 타임리프 없이 HTML 문자열로 목록 출력
- [링크](https://github.com/Minse5k/jsb-update/tree/006-01)
- 작업: `QuestionController`의 `GET /question/list` — 리포지터리로 질문을 가져와 자바 코드 안에서 HTML 문자열을 직접 만들어 응답, MockMvc 테스트
- 이유:
  - 교재(2-07)는 처음부터 타임리프 템플릿으로 목록을 만듦
  - 강의(14163 27강 "타임리프 없이 /question/list 구현")는 먼저 자바 문자열로 HTML을 만들고, 다음 강의에서 템플릿으로 옮김 → 강의 순서를 따름
  - 강의 순서가 더 좋은 이유: 템플릿 엔진이 "HTML 문자열 만들기"를 대신해 주는 도구라는 걸 직접 비교할 수 있음. 다음 커밋(006-02)과 diff를 보면 무엇이 컨트롤러에서 템플릿으로 옮겨 가는지 보임
  - `@RequiredArgsConstructor` + `final` 필드: 생성자 주입(교재·강의 공통)
- 결과: 테스트 12개 통과, 브라우저에서 `localhost:8080/question/list`에 `1 / sbb가 무엇인가요?`, `2 / 스프링부트 모델 질문입니다.` 출력 확인

### 006-02 질문 목록, 타임리프 템플릿으로 전환
- [링크](https://github.com/Minse5k/jsb-update/tree/006-02)
- 작업: 타임리프 도입 — 컨트롤러는 `Model`에 `questions`를 담고 템플릿 이름(`question/question/list`)을 리턴, HTML은 `templates/question/question/list.html`이 `th:each`로 만듦. dev 프로필에서 템플릿 캐시 끄기
- 이유:
  - 006-01과 비교: HTML 조립 코드가 컨트롤러에서 사라지고 템플릿으로 옮겨 감. 컨트롤러는 "데이터 준비", 템플릿은 "화면 그리기"로 역할이 나뉨(14163 28강 "컨트롤러가 아니라 템플릿에서 응답 내용을 생성")
  - `@ResponseBody`가 빠졌으므로 리턴값 `"question/question/list"`는 응답 본문이 아니라 템플릿 이름으로 해석됨(002-01에서 설명한 차이)
  - 템플릿 위치: 교재는 `templates/question_list.html`, 강의(14168)는 `templates/post/post/list.html`처럼 패키지 구조와 같은 폴더 → 강의 방식 채택. 화면이 많아져도 도메인별로 모여 있음
  - 변수 이름: 교재는 `questionList`, 여기서는 `questions`(14163 레포 엔티티 필드 `answers`처럼 복수형)
  - `thymeleaf.cache: false`: 교재에는 없음. 강의(14163 28·29강 "타임리프 캐시는 오직 개발환경에서만 꺼야 합니다")처럼 dev에서만 끔
  - `thymeleaf.prefix: file:src/main/resources/templates/`: 강의(14168 레포 `application-dev.yaml`) 설정. 빌드 없이 소스 폴더의 템플릿을 바로 읽어서 HTML을 저장하고 새로고침만 하면 반영됨
  - 테스트에 `view().name(...)` 추가: 어떤 템플릿으로 그렸는지까지 확인
- 결과: 테스트 12개 통과, 브라우저 출력이 006-01과 똑같음(같은 결과를 템플릿으로 만든 것)

### 006-03 질문 목록, 테이블로 출력
- [링크](https://github.com/Minse5k/jsb-update/tree/006-03)
- 작업: 질문 목록을 `<table>`(번호·제목·작성일시)로 출력, 작성일시는 `#temporals.format`
- 이유:
  - 교재(2-07)·강의(14163 32강 "질문 목록을 테이블로 출력")와 같은 화면 구성
  - `loop.count`: `th:each`의 반복 상태 변수로 1부터 세는 순번(14163 레포 `question_list.html`과 같음). 진짜 게시물 번호 계산은 3-03(페이징 후)에서
  - 작성일시는 004-01의 JPA Auditing이 자동으로 채운 값 → 교재처럼 서비스에서 `setCreateDate` 하지 않았는데도 날짜가 나옴
- 결과: 테스트 통과. 서버 재시작 없이 템플릿 저장 후 새로고침만으로 반영됨(006-02의 `prefix` 설정 효과), 작성일시 `2026-11-13 12:23` 표시 확인

### 007-01 루트 URL, / 접속 시 질문 목록으로 리다이렉트
- [링크](https://github.com/Minse5k/jsb-update/tree/007-01)
- 작업: `HomeController`에 `GET /` → `redirect:/question/list` 추가, 리다이렉트 테스트
- 이유:
  - 교재(2-08)는 `MainController`를 새로 만들어 `/`를 처리함. 002-01에서 정한 대로 사이트 홈 역할인 `HomeController`에 넣음(강의 14163 레포도 `MainController` 하나에 `/` 처리)
  - `redirect:`는 서버가 직접 목록 화면을 그리는 게 아니라, 브라우저에게 "`/question/list`로 다시 요청해"라고 302 응답을 보내는 것. 그래서 주소창도 `/question/list`로 바뀜(14168 28강 "액션메서드에서 바로 302로 응답")
- 결과: 테스트 13개 통과(302, redirectedUrl 확인), 브라우저에서 `localhost:8080/` 접속 시 주소가 `/question/list`로 바뀌는 것 확인

### 008-01 서비스, QuestionService 도입
- [링크](https://github.com/Minse5k/jsb-update/tree/008-01)
- 작업: `QuestionService` 생성(`findAll`), `QuestionController`가 리포지터리 대신 서비스를 사용
- 이유:
  - 교재(2-09)·강의(14163 34강)와 같은 구조: 컨트롤러 → 서비스 → 리포지터리. 컨트롤러는 요청/응답만, 서비스는 기능(비즈니스 로직)을 맡음
  - 메서드 이름: 교재는 `getList`, 강의(14168 `PostService.findAll`)는 리포지터리와 같은 이름 → 강의 방식 채택. 서비스 메서드가 어떤 리포지터리 메서드로 이어지는지 이름만 봐도 연결됨
  - 지금은 서비스가 리포지터리를 그대로 부르기만 하지만, 이후 등록·수정·삭제·검색 로직이 서비스에 쌓이고 여러 컨트롤러가 같은 기능을 재사용함
- 결과: 테스트 13개 통과(목록 테스트가 그대로 통과 = 동작은 같고 구조만 바뀜)

### 008-02 서비스, 조회 액션에 읽기 전용 트랜잭션
- [링크](https://github.com/Minse5k/jsb-update/tree/008-02)
- 작업: 목록 액션 메서드에 `@Transactional(readOnly = true)`
- 이유:
  - 교재에는 `@Transactional`이 거의 없음(스프링부트 기본값인 open-in-view에 기대어 화면 렌더링까지 DB 연결을 유지)
  - 강의(14168 31강 `PostController.showList`)는 조회 액션에 `@Transactional(readOnly = true)`, 변경 액션에 `@Transactional`을 붙여 트랜잭션 범위를 코드에 드러냄 → 강의 방식 채택
  - 강의 방식이 더 좋은 이유: "이 요청은 조회만 한다"가 코드에 보이고, `readOnly`면 Hibernate가 변경 감지(더티체킹)를 하지 않아 불필요한 작업이 줄어듦. 003-02에서 켠 트랜잭션 로그로 언제 시작·끝나는지 확인 가능
  - open-in-view(기본값 켜짐)는 강의 설정과 같이 그대로 둠 — 템플릿에서 LAZY 필드(`answers`)를 읽을 때 필요
- 결과: 테스트 13개 통과

### 009-01 상세 페이지, 질문을 조회해 상세 화면에 표시
- [링크](https://github.com/Minse5k/jsb-update/tree/009-01)
- 작업: `GET /question/detail/{id}` 상세 페이지 — `@PathVariable`로 id를 받아 `QuestionService.getQuestion(id)`로 조회, `detail.html`에 제목·내용 출력
- 이유:
  - 교재(2-10)·강의(14163 035-1 "질문을 조회해 상세 화면에 표시")와 같은 흐름. 강의처럼 상세 페이지를 3단계(표시 → 목록 링크 → 404)로 나눔
  - URL의 `{id}` 부분이 `@PathVariable int id`로 들어옴: `/question/detail/2` → id = 2
  - 이 단계에서는 강의 035-1과 같이 `findById(id).get()` — 없는 id면 500 에러가 남(009-03에서 처리)
  - 내용에 `white-space: pre-line`: 교재에는 없음. 강의(14168 29강 "글 본문 보여줄 때 style=white-space: pre-line 적용하여 줄바꿈 표시")처럼 사용자가 친 줄바꿈이 화면에도 보이게
  - 조회 액션이라 `@Transactional(readOnly = true)`(008-02)
- 결과: 테스트 14개 통과, 브라우저에서 `/question/detail/2`에 제목·내용 출력 확인

### 009-02 상세 페이지, 목록의 제목을 상세 페이지에 연결
- [링크](https://github.com/Minse5k/jsb-update/tree/009-02)
- 작업: 목록의 제목을 상세 페이지 링크(`th:href="@{|/question/detail/${question.id}|}"`)로 변경
- 이유:
  - 교재(2-10)·강의(14163 035-2 "목록의 제목을 상세 페이지에 연결하기")와 같음
  - `@{...}`는 URL을 만드는 문법(앱이 `/myapp` 같은 경로 아래에 배포돼도 자동으로 붙여 줌), `|...|`는 문자열 안에 변수를 끼워 넣는 문법(14163 35강 표 "@{ ... } 와 | ... | 비교")
  - 테스트에 `href="/question/detail/1"`이 있는지 검증 추가
- 결과: 테스트 통과, 브라우저에서 목록의 제목을 클릭하면 `/question/detail/1`로 이동해 상세 내용이 보임

### 009-03 상세 페이지, 없는 질문에 404 응답
- [링크](https://github.com/Minse5k/jsb-update/tree/009-03)
- 작업: 없는 질문이면 404로 응답 — `DataNotFoundException`(`@ResponseStatus(NOT_FOUND)`) 생성, `getQuestion`에서 `orElseThrow`
- 이유:
  - 교재(2-10)·강의(14163 035-3 "없는 질문에 404 응답하기")와 같은 커스텀 예외 방식. 009-01 상태에서는 `No value present`(500 서버 오류)가 나와서 원인을 알기 어려움. 커스텀 예외는 "질문이 없음"을 정확히 알려 줌(14163 35강 "친절한 예외 메시지")
  - 예외 위치: 교재·강의(14163)는 `com.mysite.sbb` 루트에 둠. 질문뿐 아니라 답변·회원에서도 쓸 공통 예외라 `global.exception`에 둠(14168의 `global` 패키지 구분 기준 적용)
  - 코드 모양: 강의(14163)는 `if (isPresent()) ... else throw`, 여기서는 같은 동작을 한 줄로 하는 `Optional.orElseThrow`
- 결과: 테스트 15개 통과(404, 발생한 예외 타입 확인), 브라우저에서 `/question/detail/999` 접속 시 404 확인

### 010-01 URL 프리픽스, @RequestMapping("/question")
- [링크](https://github.com/Minse5k/jsb-update/tree/010-01)
- 작업: `QuestionController`에 `@RequestMapping("/question")`, 메서드 매핑을 `/list`, `/detail/{id}`로 줄임
- 이유:
  - 교재(2-11)·강의(14163 036)와 같음. 클래스에 붙인 주소가 모든 메서드 주소 앞에 붙음 → 중복 제거
  - 실제 URL은 바뀌지 않았으므로 기존 테스트가 그대로 통과해야 함(리팩터링 확인)
- 결과: 테스트 15개 모두 그대로 통과

### 010-02 URL 프리픽스, 질문 URL을 /questions, /questions/{id}로
- [링크](https://github.com/Minse5k/jsb-update/tree/010-02)
- 작업: 질문 URL 변경 — 목록 `GET /question/list` → `GET /questions`, 상세 `GET /question/detail/{id}` → `GET /questions/{id}`. 링크·리다이렉트·테스트도 함께 수정
- 이유:
  - 교재(2-11)와 강의 14163 코드는 `/question/list`, `/question/detail/5`를 그대로 씀
  - 그런데 강의(14163 37강 "HTTP URL의 좋은 예와 나쁜 예시")는 `GET /questions`(목록은 복수형 명사), 상세는 `detail` 같은 동사 없이 `/명사/번호`가 좋은 예라고 설명함. 14168 강의 코드도 `/posts`, `/posts/{id}` 방식 → 강의가 "좋은 예"로 든 방식 채택
  - 더 좋은 이유: URL은 "무엇(자원)"만 나타내고 "무엇을 할지"는 HTTP 메서드(GET/POST/PUT/DELETE)로 구분하는 규칙이 앞으로 등록·수정·삭제 URL에도 일관되게 적용됨
  - 이 변경 때문에 앞으로의 URL은 교재와 다름: 교재 `/question/create` → 이 프로젝트 `/questions/write` 처럼(각 커밋에서 대응 관계를 적음)
- 결과: 테스트 15개 통과, 브라우저에서 `/` → `/questions`, 제목 링크가 `/questions/2`로 바뀐 것 확인

### 011-01 답변 기능, 상세 페이지에 답변 목록 표시
- [링크](https://github.com/Minse5k/jsb-update/tree/011-01)
- 작업: 상세 페이지에 답변 개수(`#lists.size(question.answers)`)와 답변 목록(내용·작성일시) 표시
- 이유:
  - 교재(2-12)·강의(14163 037)와 같은 화면. 교재는 `question.answerList`, 여기서는 004-04에서 만든 `question.answers`
  - 컨트롤러는 바뀌지 않음: 질문만 모델에 넣으면 템플릿이 `question.answers`로 답변까지 꺼내 씀(@OneToMany의 장점, 14163 9강 "부모에서 자식들을 바로 조회 가능")
  - `answers`는 LAZY라서 템플릿에서 처음 읽는 순간 답변 조회 SQL이 실행됨
- 결과: 테스트 16개 통과, 브라우저 `/questions/2`에 "2개의 답변이 있습니다."와 답변 2개 출력 확인

### 011-02 답변 기능, 답변 등록(POST /questions/{id}/answers)
- [링크](https://github.com/Minse5k/jsb-update/tree/011-02)
- 작업: 답변 등록 — `AnswerController`의 `POST /questions/{questionId}/answers`, 상세 페이지에 답변 작성 폼, 테스트
- 이유:
  - URL: 교재(2-12)·강의 14163 코드는 `POST /answer/create/{id}`. 강의(14163 37강 URL 표)는 이를 나쁜 예로, `POST /questions/5/answers`를 좋은 예로 듦 → 강의가 권장한 방식 채택(010-02와 같은 규칙: 자원은 URL, 행위는 POST)
  - 저장 방식: 교재는 `AnswerService.create()`에서 `answerRepository.save()`. 강의(14163 37강 "강사는 oneToMany 필드를 통해서 답변을 생성했습니다")는 `question.addAnswer(content)`만 호출 → 강의 방식 채택. 그래서 `AnswerService`·`AnswerRepository`가 없음
  - `@Transactional`(14163 레포 `AnswerController`, 14168 레포와 같음): 메서드가 끝날 때 커밋되면서 PERSIST로 답변 INSERT
  - 등록 후 `redirect:/questions/{id}`: 결과 화면을 직접 그리지 않고 상세 페이지로 다시 요청하게 함. 새로고침해도 POST가 다시 전송되지 않음(14168 28강 302)
  - 테스트의 `em.flush(); em.clear();`: 테스트 전체가 한 트랜잭션이라, 그냥 조회하면 메모리에 있는 객체를 돌려줌. DB에 진짜 INSERT 됐는지 보려면 비우고 다시 읽어야 함
  - 이 단계에서는 빈 답변도 등록됨 → 013(질문 등록·폼 검증)에서 처리
- 결과: 테스트 17개 통과(테스트 로그에 answer INSERT 확인), 브라우저에서 1번 질문에 답변을 입력·등록하면 "1개의 답변이 있습니다."로 바뀜

### 012-01 디자인, 정적 CSS와 프리텐다드 폰트
- [링크](https://github.com/Minse5k/jsb-update/tree/012-01)
- 작업: 정적 CSS `static/resource/style.css`(프리텐다드 폰트, textarea 폭 100%, 제출 버튼 여백) 생성, 목록·상세 템플릿에 연결
- 이유:
  - 교재(2-13)는 `static/style.css`에 textarea·버튼 스타일만 둠. 그 규칙은 그대로 가져옴
  - 폰트: 강의(14163 39강 "강사는 프리텐다드 폰트를 사용")처럼 CDN의 프리텐다드 woff2를 `@font-face`로 불러옴. 파일을 내려받지 않아도 됨
  - 위치: 교재는 `static/style.css`, 강의(14168 46강)는 `static/resource/` 아래로 모음 → 강의 방식 채택. 시큐리티 도입(016) 후 `/resource/**` 하나만 허용하면 모든 정적 파일이 열리도록 미리 정리(14168 46강 "정적인 콘텐츠를 static/resource 폴더로 옮기는 이유")
  - `static` 폴더 아래 경로가 그대로 URL이 됨: `static/resource/style.css` → `/resource/style.css`
- 결과: 테스트 통과, 브라우저에서 `/resource/style.css` 200 응답, 본문 폰트 `Pretendard`, textarea가 화면 폭 전체로 늘어난 것 확인
