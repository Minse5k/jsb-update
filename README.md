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

### 012-02 디자인, 부트스트랩(cdnjs) 적용
- [링크](https://github.com/Minse5k/jsb-update/tree/012-02)
- 작업: 부트스트랩 5.3.8 적용 — 목록은 `table`/`table-dark`, 상세는 `card`/`badge`, 답변 폼은 `form-control`/`btn btn-primary`
- 이유:
  - 화면 구성(클래스)은 교재(2-14)·강의(14163 039)와 같음
  - 불러오는 방법: 교재는 bootstrap.min.css 파일을 내려받아 `static`에 복사. 강의(14163 39강 "cdnjs.com를 통해서 프론트엔드 라이브러리를 다운로드 하지 않고 사용")는 cdnjs 주소를 `<link>`로 연결 → 강의 방식 채택. 프로젝트에 큰 라이브러리 파일을 커밋하지 않아도 되고 버전 변경이 주소 한 줄로 끝남
  - 아직 두 템플릿에 `<head>`가 똑같이 반복됨 → 다음 커밋(012-03)에서 공통 레이아웃으로 정리
- 결과: 테스트 통과, 브라우저에서 목록 표(검은 헤더)와 상세 카드 화면 확인

### 012-03 디자인, 공통 레이아웃(layout.html)과 페이지별 제목
- [링크](https://github.com/Minse5k/jsb-update/tree/012-03)
- 작업: 공통 레이아웃 — `thymeleaf-layout-dialect` 의존성, `templates/layout.html`(doctype, 메타, 부트스트랩 CSS·JS, style.css), 목록·상세는 `layout:decorate="~{layout}"` + `layout:fragment="content"`만 작성, 페이지 제목은 `layout:title-pattern`
- 이유:
  - 교재(2-15 "표준 HTML 구조로 변경하기")·강의(14168 44강 레이아웃)와 같은 layout dialect 방식. 012-02에서 템플릿마다 반복되던 `<head>`가 layout.html 한 곳으로 모임
  - 페이지 제목: 교재는 layout.html에 `Hello, sbb!` 고정. 강의(14168 45강 "title 엘리먼트의 layout:title-pattern 속성을 통해서 각 페이지 별 고유의 title")는 페이지마다 `<title>`을 쓰면 `질문 목록 | SBB`처럼 합쳐 줌 → 강의 방식 채택. 브라우저 탭만 봐도 어느 화면인지 구분됨
  - 부트스트랩 JS도 layout에서 한 번 불러옴(다음 기능의 내비게이션 바 접기 버튼에 필요). `bootstrap.bundle.min.js`는 드롭다운 등에 필요한 Popper까지 포함한 파일
- 결과: 테스트 통과, 브라우저 탭 제목이 `질문 목록 | SBB`, `질문 상세 | SBB`로 표시, `<!doctype html>`과 CSS 2개 로드 확인

### 013-01 질문 등록, 등록 버튼과 등록 화면
- [링크](https://github.com/Minse5k/jsb-update/tree/013-01)
- 작업: 목록에 "질문 등록하기" 버튼, `GET /questions/write` 질문 등록 화면(`write.html`)
- 이유:
  - 교재(2-16)는 `/question/create`. 강의(14168 26강)는 `GET /posts/write`(폼 화면)와 `POST /posts/write`(처리)처럼 같은 URL을 HTTP 메서드로 구분 → 강의 방식 채택해 `/questions/write`
  - 왜 `/questions`(POST)만 쓰지 않는가: 14168 26강 설명대로 REST API라면 `POST /questions`만 있으면 되지만, 이 프로젝트는 서버가 폼 화면까지 그리는 방식이라 "폼을 보여 주는 주소"가 필요함
  - `<form method="post">`에 action이 없으면 현재 주소로 전송됨(14168 26강 "action이 가리키는 주소가 현재 페이지라면 생략이 가능")
  - 제목 칸에 `autofocus`(14168 10강): 화면이 열리면 바로 입력 가능
  - `GET /questions/write`와 `GET /questions/{id}`가 겹쳐 보이지만, 스프링은 고정된 경로(`write`)를 변수 경로(`{id}`)보다 먼저 고름 — 테스트에서 `showWrite`가 처리하는 것으로 확인
  - 이 단계는 화면만. 저장 버튼을 누르면 아직 처리하는 메서드가 없음(다음 커밋)
- 결과: 테스트 18개 통과, 브라우저에서 버튼 클릭 → 등록 폼 화면 확인

### 013-02 질문 등록, 등록 처리 후 상세 페이지로 이동
- [링크](https://github.com/Minse5k/jsb-update/tree/013-02)
- 작업: `POST /questions/write` 질문 등록 처리 — `@RequestParam`으로 제목·내용을 받아 `QuestionService.write()`로 저장, 등록된 질문의 상세 페이지로 리다이렉트
- 이유:
  - 처리 흐름(폼 → 컨트롤러 → 서비스 → 리포지터리 save)은 교재(2-16)와 같음
  - 등록 후 이동: 교재는 목록(`/question/list`)으로, 강의(14168 29강 "글 작성 완료시 자동으로 생성된 글 상세페이지로 이동")는 방금 쓴 글의 상세로 → 강의 방식 채택. 내가 쓴 글이 제대로 저장됐는지 바로 확인할 수 있음
  - 서비스 메서드 이름: 교재 `create`, 강의(14168 `PostService.write`) `write` → URL(`/write`)과 이름을 맞춰 강의 방식
  - 테스트는 제목으로 질문을 찾음: 테스트는 롤백되지만 auto increment 번호는 되돌아가지 않아서, 실행 순서에 따라 번호가 달라질 수 있기 때문
  - 이 단계에서는 빈 제목도 저장됨 → 다음 커밋(013-03)에서 검증
- 결과: 테스트 19개 통과, 브라우저에서 제목·내용 입력 후 저장 → `/questions/3` 상세 페이지로 이동 확인

### 013-03 질문 등록, record 폼과 @Valid 입력값 검증
- [링크](https://github.com/Minse5k/jsb-update/tree/013-03)
- 작업: 질문 등록 입력값 검증 — 폼 객체 `record WriteForm`(`@NotBlank`, `@Size`), `@ModelAttribute("form") @Valid WriteForm form` + `BindingResult`, 실패 시 폼을 다시 보여 주며 입력값 유지(`th:object`, `th:field`)
- 이유:
  - 검증 흐름(`@Valid` → `BindingResult.hasErrors()` → 폼 다시 보여 주기)은 교재(2-16)와 같음
  - 폼 객체: 교재는 `QuestionForm` 클래스 + `@Getter @Setter` 별도 파일. 강의(14168 40강 "폼 DTO 클래스를 record 문법으로 구현하여 코드량을 줄임")는 컨트롤러 안의 `record` → 강의 방식 채택. 이 폼은 이 컨트롤러에서만 쓰여서 가까이 두는 게 읽기 쉬움
  - `@NotBlank`: 교재의 `@NotEmpty`는 공백(`"   "`)을 통과시킴. 강의(14168 11강)의 `@NotBlank`는 공백만 있어도 실패 → 강의 방식 채택. 길이 2자 이상도 강의(14168 11강 "제목의 유효길이 2자~")를 따름, 최대 200자는 DB 칼럼 길이(004-02)와 맞춤
  - 모델 이름 `form`: 강의(14168 18강 `@ModelAttribute("form")`)처럼 템플릿에서 짧게 `${form}`. GET에서도 빈 `form`을 넣어야 `th:object="${form}"`이 오류 없이 동작함(14168 16강)
  - `th:field`: `id`, `name`, `value`를 한 번에 만들어 줌 → 검증 실패로 폼을 다시 그릴 때 입력값이 그대로 남음(14168 22강)
  - 남은 문제: 에러가 여러 개면 순서가 매번 뒤죽박죽(브라우저 확인 결과) → 다음 커밋에서 정렬
- 결과: 테스트 20개 통과(공백 제목 → 저장 안 됨, 폼 다시 표시, 입력한 내용 유지). 브라우저에서 빈 폼 제출 시 에러 4개 표시 확인 — 순서가 정렬되지 않은 것도 확인

### 013-04 질문 등록, 에러 메시지 정렬과 첫 에러 칸 포커스(form_errors)
- [링크](https://github.com/Minse5k/jsb-update/tree/013-04)
- 작업: 에러 메시지 정렬과 첫 에러 칸 포커스 — 메시지를 `"01-subject-제목을 입력해주세요."` 형식으로 바꾸고, 공통 조각 `templates/form_errors.html`이 순번으로 정렬해 보여 주며 첫 에러의 필드에 포커스
- 이유:
  - 013-03에서 확인한 문제: 에러가 여러 개일 때 순서가 요청마다 달라짐
  - 교재(2-16)의 `form_errors.html`은 `#fields.allErrors()`를 순서 없이 출력만 함
  - 강의(14168 14강 "커스텀 메세지 앞에 숫자를 넣어서 메세지 순서를 정함", 15강 "에러 중 가장 처음 에러의 필드가 focus", 23~25강 `#fields`·`th:with`·프래그먼트)는 `순번-필드명-메시지`로 정렬과 포커스를 해결 → 강의의 `form_errors.html`을 그대로 채택(부트스트랩 `alert` 클래스만 추가)
  - 흐름: `sorted()`로 순번 정렬 → `split('-', 3)`으로 [순번, 필드명, 메시지] 분리 → 메시지만 화면에, 필드명은 `data-error-field-name`에 → 스크립트가 첫 `li`의 필드명으로 `form[필드명].focus()`
  - `setTimeout`: 스크립트가 폼 맨 위에 있어서 실행 시점엔 아래 입력칸이 아직 없음. 한 박자 늦춰 실행(14168 25강 "지연 시간초는 생략해도 된다")
  - 공통 조각으로 만든 이유: 답변·회원가입 폼에서도 똑같이 쓰기 위해(교재·강의 공통)
- 결과: 테스트 20개 통과(`data-error-field-name="subject"` 확인). 브라우저에서 내용만 비우고 제출 → "내용을 입력해주세요.", "내용은 2자 이상…" 순서로 정렬 표시
  - 포커스 확인 주의: 브라우저 패널이 창 포커스를 갖지 않은 상태(`document.hasFocus() == false`)라 페이지를 불러올 때 포커스가 잡히지 않음. 같은 페이지에서 조각의 스크립트를 다시 실행하자 `content` 칸에 포커스가 가는 것을 확인 — 일반 브라우저 창에서 직접 확인 권장

### 013-05 질문 등록, 답변 등록에도 입력값 검증
- [링크](https://github.com/Minse5k/jsb-update/tree/013-05)
- 작업: 답변 등록 입력값 검증 — `AnswerController.WriteForm`(record, `@NotBlank`, `@Size(min = 2)`), 실패 시 질문 상세 화면을 에러와 함께 다시 보여 줌. 상세 화면 답변 폼에 `th:object="${answerForm}"`, `form_errors` 조각, `th:field`
- 이유:
  - 교재(2-16 후반 "답변 등록 기능에 폼 적용하기")와 같은 흐름: 실패하면 `question`을 모델에 넣고 상세 템플릿을 다시 그림. 상세 GET에서도 빈 폼(`answerForm`)을 넣어야 `th:object`가 동작함(교재의 `detail(..., AnswerForm answerForm)`과 같은 이유)
  - 폼 객체는 013-03과 같은 이유로 강의(14168 40강) record, `@NotBlank`·2자 이상(14168 11강), 메시지 형식과 에러 조각은 013-04를 재사용
  - 모델 이름 `answerForm`: 상세 화면에는 질문 데이터(`question`)와 답변 폼이 같이 있어서 `form`보다 구체적인 이름으로 구분
- 결과: 테스트 21개 통과(빈 답변 → 저장 안 됨, 상세 화면에 에러). 브라우저에서 빈 답변 제출 → 에러 2개가 순서대로 표시, 기존 답변 그대로

### 013-06 질문 등록, 샘플 데이터도 서비스로 생성
- [링크](https://github.com/Minse5k/jsb-update/tree/013-06)
- 작업: 샘플 데이터(`BaseInitData`)를 리포지터리 대신 `QuestionService`(`count`, `write`)로 만들도록 변경
- 이유:
  - 강의(14168 레포 `BaseInitData`)는 `postService.count()`, `postService.write()`처럼 서비스를 사용 → 강의 방식 채택
  - 더 좋은 이유: 화면에서 질문을 등록할 때와 똑같은 코드(`write`)로 샘플을 만들어서, 등록 로직이 바뀌면 샘플 데이터도 자동으로 따라감
  - 답변은 `write`가 돌려준 질문에 `addAnswer` — `work1` 트랜잭션이 끝날 때 PERSIST로 INSERT(004-04)
- 결과: 테스트 21개 통과(샘플 데이터 기준 테스트가 그대로 통과 = 같은 데이터가 만들어짐)

### 014-01 내비게이션 바, 공통 navbar 조각
- [링크](https://github.com/Minse5k/jsb-update/tree/014-01)
- 작업: 내비게이션 바 조각 `templates/navbar.html`(로고 `SBB` → `/`, "질문 목록" 링크, 모바일 메뉴 펼치기 버튼), `layout.html`에 `th:replace`로 삽입
- 이유:
  - 교재(3-01)와 같은 부트스트랩 navbar 조각 방식. 레이아웃에 한 번 넣으면 모든 페이지에 나타남
  - 교재 3-01은 이 단계에서 아직 동작하지 않는 "로그인" 링크를 미리 넣지만, 여기서는 지금 동작하는 "질문 목록" 링크만 넣고 로그인·회원가입 링크는 그 기능을 만들 때(017·018) 추가 → 커밋마다 화면의 모든 링크가 실제로 동작하도록
  - 메뉴 펼치기 버튼은 012-03에서 layout에 넣은 부트스트랩 JS(`bootstrap.bundle.min.js`)가 동작시킴
- 결과: 테스트 21개 통과(`navbar-brand` 확인). 브라우저에서 모든 페이지 상단에 바 표시, 모바일 폭(375px)에서 펼치기 버튼 클릭 시 메뉴가 `collapse show`로 펼쳐짐 확인

### 015-01 페이징, 개발용 대량 샘플 데이터
- [링크](https://github.com/Minse5k/jsb-update/tree/015-01)
- 작업: 개발 환경 전용 대량 샘플 데이터 `DevInitData`(`@Profile("dev")`) — 질문 300개 생성. `BaseInitData`는 `@Order(1)`, `DevInitData`는 `@Order(2)`
- 이유:
  - 교재(3-02)는 테스트 메서드에서 질문 300개를 `save`해서 넣음. 교재는 개발·테스트가 같은 DB라 이 방법으로 화면용 데이터가 생김
  - 이 프로젝트는 003-01에서 dev(파일 DB)와 test(메모리 DB)를 나눴기 때문에 테스트로 넣은 데이터는 화면에 보이지 않음. 그래서 앱 시작 시 데이터를 만드는 강의 방식(005-01 `BaseInitData`)을 그대로 확장하되, dev 프로필에서만 동작하게 함
  - `@Profile("dev")`로 나눈 이유: 테스트는 샘플 질문 2개를 기준으로 결과를 검증함. 300개가 섞이면 기존 테스트의 기대값이 모두 흔들림. (강의 14163 27강 커밋에서도 초기 데이터에 `@Profile`을 붙였다 떼는 장면이 있음 — 프로필로 초기 데이터를 나누는 방법)
  - `@Order`: 질문 2개(Base)가 먼저 만들어지고 그 뒤에 300개가 붙도록 실행 순서를 고정
- 결과: 테스트 21개 통과(테스트에는 영향 없음). bootRun 후 목록에 303개(샘플 3 + 300) 표시 확인 — 한 화면에 전부 나와서 다음 커밋에서 페이징

### 015-02 페이징, Page로 10개씩 조회
- [링크](https://github.com/Minse5k/jsb-update/tree/015-02)
- 작업: 질문 목록 페이징 — `QuestionService.findPage(page)`(`PageRequest.of(page, 10, id 내림차순)`), 목록 액션이 `?page=` 파라미터(기본 0)를 받아 `Page<Question>`을 `paging`으로 모델에 담음. 쓰이지 않게 된 `findAll` 정리
- 이유:
  - `Pageable`/`PageRequest`/`Page` 사용은 교재(3-02)와 같음. `Page`는 이번 페이지 질문 10개와 전체 개수·현재 번호·전체 페이지 수를 함께 담고 있어서, 템플릿이 페이지 버튼까지 그릴 수 있음
  - 정렬 기준: 교재는 `createDate` 내림차순. 여기서는 `id` 내림차순 — 015-01처럼 한 번에 300개를 만들면 작성일시가 거의 같아 순서가 흔들릴 수 있지만, id는 저장 순서대로 하나씩 커지는 값이라 항상 같은 순서가 나옴(강의 근거가 아닌 이 프로젝트의 판단)
  - 리포지터리는 그대로: `JpaRepository`가 이미 `findAll(Pageable)`을 갖고 있음(교재는 `Page<Question> findAll(Pageable)`를 직접 선언)
  - 테스트는 질문 25개를 더 만든 뒤 1페이지에 정확히 11~20번째 최신 질문만 있는지 확인
- 결과: 테스트 22개 통과, 브라우저 `/questions?page=1`에 10개(`[290]`~`[281]`) 표시. 페이지 이동 버튼은 다음 커밋

### 015-03 페이징, 페이지 이동 버튼
- [링크](https://github.com/Minse5k/jsb-update/tree/015-03)
- 작업: 페이지 이동 버튼 — 이전/다음(첫·마지막 페이지에서 비활성화), 현재 페이지 앞뒤 5개 번호, 현재 페이지 강조
- 이유:
  - 구성(`pagination`, `hasPrevious`/`hasNext`, `#numbers.sequence`, 앞뒤 5개 제한)은 교재(3-02)와 같음
  - 링크 주소는 `@{/questions(page=${...})}`로 만듦: 타임리프가 `/questions?page=2`처럼 쿼리 파라미터를 붙여 줌. 링크든 폼이든 결국 "URL을 만들어 요청하는 것"(14168 3강 "폼은 URL 생성기")
  - 화면 번호: 교재는 내부 페이지 번호(0부터)를 그대로 보여 줘서 첫 페이지가 "0"으로 보임. 여기서는 URL은 0부터 그대로 두고 화면 글자만 `page + 1`로 1부터 보이게 함(사람에게 익숙한 표기, 이 프로젝트의 판단)
  - 남은 문제: 어느 페이지든 번호 칸이 1~10으로 똑같음 → 다음 커밋에서 게시물 번호 계산
- 결과: 테스트 통과(이전/다음 링크 주소 확인). 브라우저에서 첫 페이지 "이전" 비활성·`1` 강조, `page=10`에서 `6 … 11(현재) … 16` 표시 확인

### 015-04 페이징, 페이지가 바뀌어도 이어지는 게시물 번호
- [링크](https://github.com/Minse5k/jsb-update/tree/015-04)
- 작업: 목록의 번호를 페이지와 상관없이 이어지는 게시물 번호로 — `전체 개수 - (현재 페이지 × 페이지 크기) - 순번(loop.index)`
- 이유:
  - 교재(3-03)와 같은 공식. 015-03까지는 `loop.count`라서 모든 페이지가 1~10으로 보였음
  - 가장 최신 글이 가장 큰 번호를 갖고, 마지막 페이지의 가장 오래된 글이 1번이 됨
  - DB의 id를 그대로 쓰지 않는 이유: 질문이 삭제되면 id에 빈 번호가 생기지만, 이 공식은 항상 빈틈없이 이어진 번호를 보여 줌(교재 3-03의 설명과 같은 이유)
- 결과: 테스트 통과, 브라우저 `page=1` 첫 줄 293 ~ 마지막 284, 마지막 페이지(`page=30`)는 3·2·1 확인

### 015-05 페이징, 목록에 답변 개수 표시
- [링크](https://github.com/Minse5k/jsb-update/tree/015-05)
- 작업: 목록에서 답변이 있는 질문 제목 옆에 답변 개수(빨간 작은 숫자) 표시
- 이유:
  - 교재(3-04)와 같은 방법: `#lists.size(question.answers) > 0`일 때만 `<span>` 출력. 교재는 `answerList`, 여기서는 `answers`
  - 컨트롤러 변경 없이 템플릿에서 `question.answers`(LAZY)를 읽음. 질문마다 답변 조회 SQL이 한 번씩 실행되는 구조(N+1)라는 점은 알고 넘어감 — 한 페이지 10개라 지금은 문제없음(14163 13강 "N이 천을 넘어가는 케이스가 많아진다면 그때는 다른 방법을 고려")
  - 테스트: HTML이 XML 형식이 아니라 xpath 검사가 안 돼서, "답변 개수 칸이 정확히 한 군데에 2로 나오는지"를 문자열로 확인
- 결과: 테스트 22개 통과, 브라우저 마지막 페이지에서 "스프링부트 모델 질문입니다. 2", "sbb가 무엇인가요? 1" 표시(브라우저로 답변 등록한 1번 질문 포함)

### 016-01 시큐리티, 의존성 추가하면 모든 요청이 막힌다(의도한 실패)
- [링크](https://github.com/Minse5k/jsb-update/tree/016-01)
- 작업: Spring Security 의존성 추가(`spring-boot-starter-security`, 테스트용 `spring-boot-starter-security-test`)만 하고 설정은 하지 않음 — **이 커밋은 일부러 실패 상태**
- 이유:
  - 강의(14168 046-1 "시큐리티 도입, 모든 요청이 막힘")와 같은 순서. 001-01에서 "도입하자마자 모든 요청이 막히는 경험을 하려고 Security를 미리 넣지 않았다"고 한 바로 그 단계
  - 시큐리티는 설정이 없으면 기본값으로 동작함(14168 46강): ① 인증되지 않은 요청은 모두 거절 ② POST·PUT·DELETE는 CSRF 토큰 검사
  - 시큐리티는 컨트롤러보다 앞단의 필터에서 동작함(14168 46강 "필터/인터셉터 레이어"). 그래서 요청이 컨트롤러에 도착하지도 못함
  - 교재(3-05)는 의존성을 넣고 바로 `SecurityConfig`를 만들어서 이 상태를 거치지 않음
- 결과(의도한 실패):
  - `./gradlew test` → 컨트롤러 테스트 12개 실패. 메시지가 `No handler` / `401` — 요청이 필터에서 막혀 컨트롤러(handler)까지 가지 못했다는 뜻. 리포지터리 테스트(컨트롤러를 거치지 않음)는 통과
  - 브라우저에서 `/questions` 접속 → 시큐리티 기본 로그인 화면(`/login`, "Please sign in")으로 이동
  - 다음 커밋에서 설정으로 해결

### 016-02 시큐리티, SecurityConfig로 모든 요청 허용(POST는 CSRF로 막힘)
- [링크](https://github.com/Minse5k/jsb-update/tree/016-02)
- 작업: `global.security.SecurityConfig` — 일단 모든 요청 허용(`/**` permitAll), 정적 파일(`/resource/**`)·H2 콘솔 허용, H2 콘솔용 frame 허용과 CSRF 제외 — **POST 테스트 4개는 아직 실패(CSRF, 다음 커밋에서 해결)**
- 이유:
  - 강의(14168 046-2 "모든 요청 허용, 나중을 위해 정적 콘텐츠 폴더에 담기", 레포 `SecurityConfig`)를 그대로 따름. 위치도 강의처럼 `global.security`(교재는 루트 패키지)
  - 교재(3-05)도 `/**` permitAll, H2 콘솔 CSRF 제외, frame SAMEORIGIN 설정. 다만 교재 코드의 `AntPathRequestMatcher`는 지금 버전(Spring Security 7)에서 사라져서 강의처럼 문자열 경로(`requestMatchers("/h2-console/**")`)를 씀
  - `/resource/**`를 따로 허용해 두는 이유: 나중에 `/**` 허용을 좁혀도 CSS·JS는 항상 열려 있어야 함(14168 46강, 012-01에서 정적 파일을 한 폴더로 모은 이유)
  - 시큐리티 기본값 ②(POST·PUT·DELETE는 CSRF 토큰 검사)는 여전히 켜져 있음
- 결과:
  - 테스트: GET 테스트는 모두 통과로 돌아옴. POST 테스트 4개는 실패 — CSRF 토큰이 없어서 필터에서 403으로 막힘(의도한 상태)
  - 브라우저: 목록·상세 정상. 질문 등록 폼 제출 → **403**. 반면 답변 폼에는 `_csrf` 숨은 칸이 이미 들어 있음 — 답변 폼은 `th:action`을 썼고, 질문 등록 폼은 action을 생략했기 때문(다음 커밋의 핵심)

### 016-03 시큐리티, th:action으로 CSRF 토큰 처리
- [링크](https://github.com/Minse5k/jsb-update/tree/016-03)
- 작업: CSRF 처리 — 질문 등록 폼에 값 없는 `th:action`을 붙여 타임리프가 `_csrf` 숨은 칸을 자동으로 넣게 함. POST 테스트는 `.with(csrf())`로 토큰을 붙여 보내고, 토큰 없이 보내면 403인지 확인하는 테스트(t9) 추가
- 이유:
  - CSRF: 다른 사이트가 사용자 몰래 우리 서버로 폼을 전송하는 공격. 시큐리티는 우리 서버가 만들어 준 토큰이 같이 오는 POST만 받아 줌(14168 46강 CSRF 테스트)
  - 교재(3-06 이후)는 폼마다 `<input type="hidden" th:name="${_csrf.parameterName}" th:value="${_csrf.token}">`을 직접 씀. 강의(14168 046-3 → 046-4 "th:action을 통해서 CSRF 토큰 처리")는 직접 쓰던 숨은 칸을 지우고 `th:action`으로 대신함 → 강의 방식 채택. 폼마다 같은 줄을 반복하지 않아도 되고 빠뜨릴 일이 없음
  - 016-02에서 답변 폼이 이미 동작했던 이유가 바로 이것: 답변 폼은 처음부터 `th:action`을 썼음
  - `th:action`에 값이 없으면 action은 현재 주소 그대로(013-01의 "action 생략"과 같은 효과)
- 결과: 테스트 23개 모두 통과로 복구(토큰 없는 POST → 403 테스트 포함). 브라우저에서 질문 등록 폼에 `_csrf` 칸이 생겼고, 등록 → `/questions/304` 상세로 이동 확인

### 017-01 회원가입, SiteUser 엔티티와 UserRepository
- [링크](https://github.com/Minse5k/jsb-update/tree/017-01)
- 작업: 회원 엔티티 `SiteUser`(username·email 유니크, password)와 `UserRepository`(`findByUsername`, `findByEmail`)
- 이유:
  - 엔티티 구성과 이름 `SiteUser`는 교재(3-06)와 같음. `User`라고 하지 않는 이유는 스프링 시큐리티에 이미 `User` 클래스가 있기 때문
  - 강의(14168)처럼 `BaseEntity` 상속(가입일시가 자동 기록됨), `@Setter` 대신 생성자. 교재는 id가 `Long`이고 공통 부모가 없음
  - 패키지는 지금까지와 같은 `domain.{도메인}.{세부도메인}` 구조로 `domain.user.user`
  - 메서드 이름: 교재 코드는 `findByusername`(소문자 u). 메서드 이름 규칙상 `findByUsername`이 정확한 표기라 고쳐서 씀
  - `findByEmail`은 다음 커밋의 이메일 중복 확인에 사용
- 결과: 테스트 통과, 로그에서 `create table site_user`와 `unique (email)`, `unique (username)` 제약 생성 확인

### 017-02 회원가입, UserService와 비밀번호 암호화(BCrypt)
- [링크](https://github.com/Minse5k/jsb-update/tree/017-02)
- 작업: `UserService.join(username, password, email)` — 비밀번호를 `PasswordEncoder`(BCrypt)로 암호화해 저장. `SecurityConfig`에 `PasswordEncoder` 빈 등록. 서비스 테스트
- 이유:
  - 교재(3-06)와 같은 방식: `BCryptPasswordEncoder`를 빈으로 등록해 서비스에서 주입받음. 비밀번호를 그대로 저장하면 DB가 유출됐을 때 그대로 드러나므로 해시로 저장
  - 메서드 이름: 교재 `create` → 여기서는 `join`(회원 "가입"이라는 의미가 이름에 드러나게, 이 프로젝트의 판단)
  - 테스트로 "저장된 값 ≠ 원래 비밀번호, 하지만 `matches`로는 맞음"을 확인 — 로그인(018) 때 시큐리티가 바로 이 `matches`로 비교함
- 결과: 테스트 24개 통과

### 017-03 회원가입, 회원가입 화면과 처리(/users/join)
- [링크](https://github.com/Minse5k/jsb-update/tree/017-03)
- 작업: 회원가입 화면·처리 — `GET/POST /users/join`, `record JoinForm`(아이디 3~25자, 비밀번호, 비밀번호 확인, 이메일 형식), 비밀번호 불일치·아이디 중복·이메일 중복을 `rejectValue`로 필드 에러 추가, 성공하면 `/`로
- 이유:
  - 화면 구성과 "비밀번호 확인이 다르면 `rejectValue`" 방식은 교재(3-06)와 같음
  - URL: 교재 `/user/signup` → 질문과 같은 규칙(복수형 자원 + GET/POST 같은 URL, 013-01)으로 `/users/join`
  - 폼·검증 방식은 질문 등록(013-03·013-04)과 같은 강의(14168) 방식: record + `@NotBlank` + 순번 메시지. 직접 추가하는 에러도 같은 `"06-password2-..."` 형식이라 정렬·포커스가 그대로 동작
  - 중복 가입 처리: 교재는 일단 저장해 보고 DB 유니크 제약 예외(`DataIntegrityViolationException`)를 잡아 "이미 등록된 사용자입니다" 한 가지로 알려 줌. 여기서는 저장 전에 아이디·이메일을 각각 조회해 어느 칸이 문제인지 그 칸에 에러를 붙임(사용자가 무엇을 고쳐야 하는지 바로 보임, 이 프로젝트의 판단). DB 유니크 제약(017-01)은 마지막 안전장치로 그대로 둠
  - 비밀번호 칸(`type=password`)은 `th:field`를 써도 다시 그릴 때 값이 비워짐(브라우저 보안상 일반적인 동작)
- 결과: 테스트 28개 통과(가입 성공, 비밀번호 불일치, 아이디 중복). 브라우저에서 비밀번호를 다르게 입력 → 에러 표시·아이디 유지, 같게 입력 → 가입 후 `/questions`로 이동

### 017-04 회원가입, 내비게이션 바에 회원가입 링크
- [링크](https://github.com/Minse5k/jsb-update/tree/017-04)
- 작업: 내비게이션 바 오른쪽에 "회원가입" 링크(`/users/join`)
- 이유:
  - 교재(3-06)와 같은 위치의 링크. 014-01에서 정한 대로 기능이 생긴 이 시점에 링크를 추가
- 결과: 테스트 통과, 브라우저 내비게이션 바에 `회원가입` 링크(`href="/users/join"`) 확인
