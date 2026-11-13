package com.back.domain.home.home.controller;

import com.back.domain.question.answer.service.AnswerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller // 스프링이 이 클래스를 컨트롤러 빈으로 등록하고, 안의 매핑 정보를 읽어 둔다.
@RequiredArgsConstructor
public class HomeController {
    private final AnswerService answerService;

    @GetMapping("/hello") // GET /hello 요청이 오면 이 메서드가 실행된다.
    @ResponseBody // 리턴값을 템플릿 이름이 아니라 응답 본문(body) 그대로 보낸다.
    public String hello() {
        return "Hello World";
    }

    @GetMapping("/")
    public String root() {
        // "redirect:주소" 를 리턴하면 브라우저에게 "그 주소로 다시 요청하라"(302)고 응답한다.
        return "redirect:/questions";
    }

    @GetMapping("/recent") // GET /recent : 사이트 전체의 최근 답변 (게시판을 가리지 않는 사이트 단위 화면이라 홈에 둔다)
    @Transactional(readOnly = true)
    public String recent(Model model) {
        model.addAttribute("recentAnswers", answerService.findRecent());

        return "home/home/recent";
    }
}
