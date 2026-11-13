package com.back.domain.user.user.controller;

import com.back.domain.user.user.service.UserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    public record JoinForm(
            @NotBlank(message = "01-username-아이디를 입력해주세요.")
            @Size(min = 3, max = 25, message = "02-username-아이디는 3자 이상, 25자 이하로 입력해주세요.")
            String username,
            @NotBlank(message = "04-password1-비밀번호를 입력해주세요.")
            String password1,
            @NotBlank(message = "05-password2-비밀번호 확인을 입력해주세요.")
            String password2,
            @NotBlank(message = "07-email-이메일을 입력해주세요.")
            @Email(message = "08-email-이메일 형식이 올바르지 않습니다.")
            String email
    ) {
    }

    @GetMapping("/join") // GET /users/join : 회원가입 화면
    public String showJoin(@ModelAttribute("form") JoinForm form) {
        return "user/user/join";
    }

    @PostMapping("/join") // POST /users/join : 회원가입 처리
    @Transactional
    public String join(
            @ModelAttribute("form") @Valid JoinForm form,
            BindingResult bindingResult
    ) {
        if (bindingResult.hasErrors()) return "user/user/join";

        // 어노테이션으로는 검사할 수 없는 규칙은 직접 검사해서 에러를 추가한다. (메시지 형식은 form_errors 와 같게)
        if (!form.password1().equals(form.password2())) {
            bindingResult.rejectValue("password2", "passwordInCorrect", "06-password2-2개의 비밀번호가 일치하지 않습니다.");
        }
        if (userService.findByUsername(form.username()).isPresent()) {
            bindingResult.rejectValue("username", "duplicated", "03-username-이미 사용 중인 아이디입니다.");
        }
        if (userService.findByEmail(form.email()).isPresent()) {
            bindingResult.rejectValue("email", "duplicated", "09-email-이미 사용 중인 이메일입니다.");
        }
        if (bindingResult.hasErrors()) return "user/user/join";

        userService.join(form.username(), form.password1(), form.email());

        return "redirect:/";
    }

    @GetMapping("/login") // GET /users/login : 로그인 화면만 보여 준다. 처리(POST)는 시큐리티가 한다.
    public String showLogin() {
        return "user/user/login";
    }
}
