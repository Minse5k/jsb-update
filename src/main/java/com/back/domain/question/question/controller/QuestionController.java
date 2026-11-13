package com.back.domain.question.question.controller;

import com.back.domain.question.answer.controller.AnswerController;
import com.back.domain.question.question.entity.Question;
import com.back.domain.question.question.service.QuestionService;
import com.back.domain.user.user.entity.SiteUser;
import com.back.domain.user.user.service.UserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;
import org.springframework.web.bind.annotation.ModelAttribute;


@Controller
@RequestMapping("/questions") // 이 컨트롤러의 모든 URL 앞에 /questions 가 붙는다.
@RequiredArgsConstructor // final 필드를 받는 생성자를 만들어 준다 → 스프링이 그 생성자로 QuestionService 를 넣어 준다.
public class QuestionController {
    private final QuestionService questionService; // 컨트롤러는 리포지터리를 직접 쓰지 않고 서비스를 통한다.
    private final UserService userService;

    @GetMapping // → GET /questions (질문 "목록" = 질문들)
    @Transactional(readOnly = true) // 조회만 하는 요청: 이 메서드가 끝날 때까지 하나의 트랜잭션(DB 연결) 안에서 처리
    public String list(
            @RequestParam(defaultValue = "0") int page, // /questions?page=2 → page = 2, 없으면 0 (첫 페이지)
            Model model // 스프링이 Model 객체를 만들어서 넣어 준다.
    ) {
        Page<Question> paging = questionService.findPage(page); // 질문 10개 + 전체 개수·페이지 정보

        model.addAttribute("paging", paging); // 템플릿에서 ${paging} 으로 꺼내 쓸 수 있게 등록

        return "question/question/list"; // templates/question/question/list.html 을 렌더링한 결과가 응답이 된다.
    }

    @GetMapping("/{id}") // → GET /questions/{id} (질문들 중 id 번), {id} 자리에 오는 값을 아래 @PathVariable int id 로 받는다.
    @Transactional(readOnly = true)
    public String detail(
            @PathVariable int id,
            @ModelAttribute("answerForm") AnswerController.WriteForm answerForm, // 상세 화면 아래 답변 폼(th:object="${answerForm}")용 빈 폼
            Model model
    ) {
        Question question = questionService.getQuestion(id);

        model.addAttribute("question", question);

        return "question/question/detail";
    }

    // 폼 입력값을 담는 객체(DTO). record 는 생성자·getter 를 자동으로 만들어 준다.
    // 메시지 형식 "순번-필드명-메시지" : form_errors.html 이 순번으로 정렬하고, 필드명으로 포커스할 칸을 찾는다.
    public record WriteForm(
            @NotBlank(message = "01-subject-제목을 입력해주세요.") // null, "", "   " 모두 실패
            @Size(min = 2, max = 200, message = "02-subject-제목은 2자 이상, 200자 이하로 입력해주세요.")
            String subject,
            @NotBlank(message = "03-content-내용을 입력해주세요.")
            @Size(min = 2, message = "04-content-내용은 2자 이상 입력해주세요.")
            String content
    ) {
    }

    @GetMapping("/write") // GET /questions/write : 질문 등록 "화면"
    @PreAuthorize("isAuthenticated()") // 로그인하지 않았으면 이 메서드를 실행하지 않고 로그인 화면으로 보낸다.
    public String showWrite(@ModelAttribute("form") WriteForm form) { // 빈 form 을 모델에 "form" 으로 넣는다 → 템플릿의 th:object="${form}"
        return "question/question/write";
    }

    @PostMapping("/write") // POST /questions/write : 질문 등록 "처리" (URL 은 화면과 같고 메서드가 다르다)
    @PreAuthorize("isAuthenticated()")
    @Transactional
    public String write(
            @ModelAttribute("form") @Valid WriteForm form, // 요청 파라미터(subject, content)를 WriteForm 에 담고 검증한다.
            BindingResult bindingResult, // 검증 결과. 반드시 @Valid 바로 다음 매개변수여야 한다.
            Principal principal // 로그인한 사용자 정보. principal.getName() = 아이디
    ) {
        // 검증 실패 → 저장하지 않고 폼을 다시 보여 준다. (form 은 모델에 남아 있어서 입력값이 유지된다.)
        if (bindingResult.hasErrors()) return "question/question/write";

        SiteUser author = userService.findByUsername(principal.getName()).get(); // 로그인한 회원 = 글쓴이
        Question question = questionService.write(author, form.subject(), form.content());

        return "redirect:/questions/" + question.getId(); // 방금 등록한 질문의 상세 페이지로 이동
    }

    // 글쓴이 본인인지 확인. 아니면 403(권한 없음)
    private void checkAuthor(SiteUser author, Principal principal) {
        if (author == null || !author.getUsername().equals(principal.getName())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "수정/삭제 권한이 없습니다.");
        }
    }

    public record ModifyForm(
            @NotBlank(message = "01-subject-제목을 입력해주세요.")
            @Size(min = 2, max = 200, message = "02-subject-제목은 2자 이상, 200자 이하로 입력해주세요.")
            String subject,
            @NotBlank(message = "03-content-내용을 입력해주세요.")
            @Size(min = 2, message = "04-content-내용은 2자 이상 입력해주세요.")
            String content
    ) {
    }

    @GetMapping("/{id}/modify") // GET /questions/{id}/modify : 수정 화면
    @PreAuthorize("isAuthenticated()")
    @Transactional(readOnly = true)
    public String showModify(@PathVariable int id, Principal principal, Model model) {
        Question question = questionService.getQuestion(id);
        checkAuthor(question.getAuthor(), principal);

        model.addAttribute("question", question);
        model.addAttribute("form", new ModifyForm(question.getSubject(), question.getContent())); // 기존 값을 채운 폼

        return "question/question/modify";
    }

    @PutMapping("/{id}/modify") // PUT /questions/{id}/modify : 수정 처리 (폼의 _method=PUT 으로 들어온다)
    @PreAuthorize("isAuthenticated()")
    @Transactional
    public String modify(
            @PathVariable int id,
            @ModelAttribute("form") @Valid ModifyForm form,
            BindingResult bindingResult,
            Principal principal,
            Model model
    ) {
        Question question = questionService.getQuestion(id);
        checkAuthor(question.getAuthor(), principal);

        if (bindingResult.hasErrors()) {
            model.addAttribute("question", question);
            return "question/question/modify";
        }

        questionService.modify(question, form.subject(), form.content());

        return "redirect:/questions/" + id;
    }
}
