package com.back.domain.question.question.controller;

import com.back.domain.question.answer.controller.AnswerController;
import com.back.domain.question.question.entity.Question;
import com.back.domain.question.question.service.QuestionService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.List;

@Controller
@RequestMapping("/questions") // 이 컨트롤러의 모든 URL 앞에 /questions 가 붙는다.
@RequiredArgsConstructor // final 필드를 받는 생성자를 만들어 준다 → 스프링이 그 생성자로 QuestionService 를 넣어 준다.
public class QuestionController {
    private final QuestionService questionService; // 컨트롤러는 리포지터리를 직접 쓰지 않고 서비스를 통한다.

    @GetMapping // → GET /questions (질문 "목록" = 질문들)
    @Transactional(readOnly = true) // 조회만 하는 요청: 이 메서드가 끝날 때까지 하나의 트랜잭션(DB 연결) 안에서 처리
    public String list(Model model) { // 스프링이 Model 객체를 만들어서 넣어 준다.
        List<Question> questions = questionService.findAll();

        model.addAttribute("questions", questions); // 템플릿에서 ${questions} 로 꺼내 쓸 수 있게 등록

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
    public String showWrite(@ModelAttribute("form") WriteForm form) { // 빈 form 을 모델에 "form" 으로 넣는다 → 템플릿의 th:object="${form}"
        return "question/question/write";
    }

    @PostMapping("/write") // POST /questions/write : 질문 등록 "처리" (URL 은 화면과 같고 메서드가 다르다)
    @Transactional
    public String write(
            @ModelAttribute("form") @Valid WriteForm form, // 요청 파라미터(subject, content)를 WriteForm 에 담고 검증한다.
            BindingResult bindingResult // 검증 결과. 반드시 @Valid 바로 다음 매개변수여야 한다.
    ) {
        // 검증 실패 → 저장하지 않고 폼을 다시 보여 준다. (form 은 모델에 남아 있어서 입력값이 유지된다.)
        if (bindingResult.hasErrors()) return "question/question/write";

        Question question = questionService.write(form.subject(), form.content());

        return "redirect:/questions/" + question.getId(); // 방금 등록한 질문의 상세 페이지로 이동
    }
}
