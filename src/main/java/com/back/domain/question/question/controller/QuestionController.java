package com.back.domain.question.question.controller;

import com.back.domain.question.question.entity.Question;
import com.back.domain.question.question.repository.QuestionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor // final 필드를 받는 생성자를 만들어 준다 → 스프링이 그 생성자로 QuestionRepository 를 넣어 준다.
public class QuestionController {
    private final QuestionRepository questionRepository;

    @GetMapping("/question/list")
    @ResponseBody
    public String list() {
        List<Question> questions = questionRepository.findAll();

        // 질문 하나당 <li> 하나
        String questionLiHtml = questions
                .stream()
                .map(q -> "<li>%d / %s</li>".formatted(q.getId(), q.getSubject()))
                .collect(Collectors.joining("\n"));

        // 자바 코드 안에서 HTML 을 직접 조립한다. (다음 단계에서 템플릿으로 옮긴다.)
        return """
                <!DOCTYPE html>
                <html lang="ko">
                <head>
                    <meta charset="UTF-8">
                    <title>질문 목록</title>
                </head>
                <body>
                    <h1>질문 목록</h1>
                    <ul>
                %s
                    </ul>
                </body>
                </html>
                """.formatted(questionLiHtml);
    }
}
