package com.back.global.mail;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Profile({"dev", "test"}) // 운영에서는 이 화면 자체가 없다.
@Controller
@RequiredArgsConstructor
public class DevMailboxController {
    private final DevMailService devMailService;

    @GetMapping("/dev/mailbox") // 개발용 메일함 : 앱이 보낸 메일을 화면으로 확인
    public String mailbox(Model model) {
        model.addAttribute("mails", devMailService.findAll());
        return "global/mail/mailbox";
    }
}
