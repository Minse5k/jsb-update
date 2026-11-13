package com.back.domain.question.answer.controller;

import com.back.domain.question.question.entity.Question;
import com.back.domain.question.question.service.QuestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/questions/{questionId}/answers") // "몇 번 질문의 답변들" 이라는 자원
@RequiredArgsConstructor
public class AnswerController {
    private final QuestionService questionService; // 답변은 질문을 통해서 만든다. (AnswerService, AnswerRepository 가 필요 없다.)

    @PostMapping // POST /questions/{questionId}/answers : 답변 생성
    @Transactional // 이 메서드가 끝날 때 커밋 → 그때 addAnswer 로 추가한 답변이 INSERT 된다.
    public String write(
            @PathVariable int questionId,
            @RequestParam("content") String content // 폼의 <textarea name="content"> 값
    ) {
        Question question = questionService.getQuestion(questionId);

        question.addAnswer(content);

        return "redirect:/questions/" + questionId; // 등록 후 상세 페이지로 이동 (새로고침해도 다시 등록되지 않게)
    }
}
