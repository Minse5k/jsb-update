package com.back.domain.home.home.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller // 스프링이 이 클래스를 컨트롤러 빈으로 등록하고, 안의 매핑 정보를 읽어 둔다.
public class HomeController {

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
}
