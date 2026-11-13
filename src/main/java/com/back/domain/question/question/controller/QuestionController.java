package com.back.domain.question.question.controller;

import com.back.domain.question.question.entity.Question;
import com.back.domain.question.question.service.QuestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor // final 필드를 받는 생성자를 만들어 준다 → 스프링이 그 생성자로 QuestionService 를 넣어 준다.
public class QuestionController {
    private final QuestionService questionService; // 컨트롤러는 리포지터리를 직접 쓰지 않고 서비스를 통한다.

    @GetMapping("/question/list")
    public String list(Model model) { // 스프링이 Model 객체를 만들어서 넣어 준다.
        List<Question> questions = questionService.findAll();

        model.addAttribute("questions", questions); // 템플릿에서 ${questions} 로 꺼내 쓸 수 있게 등록

        return "question/question/list"; // templates/question/question/list.html 을 렌더링한 결과가 응답이 된다.
    }
}
