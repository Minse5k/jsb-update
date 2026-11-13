package com.back.domain.question.question.controller;

import com.back.domain.question.question.entity.Question;
import com.back.domain.question.question.service.QuestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

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
    public String detail(@PathVariable int id, Model model) {
        Question question = questionService.getQuestion(id);

        model.addAttribute("question", question);

        return "question/question/detail";
    }
}
