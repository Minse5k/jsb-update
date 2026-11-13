package com.back.domain.question.answer.controller;

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
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/questions/{questionId}/answers") // "몇 번 질문의 답변들" 이라는 자원
@RequiredArgsConstructor
public class AnswerController {
    private final QuestionService questionService; // 답변은 질문을 통해서 만든다. (AnswerService, AnswerRepository 가 필요 없다.)

    public record WriteForm(
            @NotBlank(message = "01-content-답변 내용을 입력해주세요.")
            @Size(min = 2, message = "02-content-답변 내용은 2자 이상 입력해주세요.")
            String content
    ) {
    }

    @PostMapping // POST /questions/{questionId}/answers : 답변 생성
    @Transactional // 이 메서드가 끝날 때 커밋 → 그때 addAnswer 로 추가한 답변이 INSERT 된다.
    public String write(
            @PathVariable int questionId,
            @ModelAttribute("answerForm") @Valid WriteForm form,
            BindingResult bindingResult,
            Model model
    ) {
        Question question = questionService.getQuestion(questionId);

        if (bindingResult.hasErrors()) {
            // 검증 실패 → 상세 페이지를 다시 그린다. 상세 페이지에는 질문이 필요하므로 모델에 넣어 준다.
            model.addAttribute("question", question);
            return "question/question/detail";
        }

        question.addAnswer(null, form.content()); // 글쓴이는 다음 단계(로그인 필수)에서 채운다.

        return "redirect:/questions/" + questionId; // 등록 후 상세 페이지로 이동 (새로고침해도 다시 등록되지 않게)
    }
}
