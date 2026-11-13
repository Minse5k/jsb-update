package com.back.domain.user.user.controller;

import com.back.domain.question.answer.service.AnswerService;
import com.back.domain.question.comment.service.CommentService;
import com.back.domain.question.question.service.QuestionService;
import com.back.domain.user.user.service.UserService;
import com.back.global.exception.DataNotFoundException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import com.back.domain.user.user.entity.SiteUser;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;

@Controller
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final QuestionService questionService;
    private final AnswerService answerService;
    private final CommentService commentService;

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

    // application.yaml 의 카카오 client-id (환경 변수 KAKAO_CLIENT_ID 가 없으면 not-set)
    @Value("${spring.security.oauth2.client.registration.kakao.client-id}")
    private String kakaoClientId;

    @GetMapping("/login") // GET /users/login : 로그인 화면만 보여 준다. 처리(POST)는 시큐리티가 한다.
    public String showLogin(Model model) {
        // 키가 없는데 버튼을 누르면 카카오에서 오류 화면이 나오므로, 키가 있을 때만 버튼을 보인다.
        model.addAttribute("kakaoLoginEnabled", !"not-set".equals(kakaoClientId));

        return "user/user/login";
    }

    public record PasswordForm(
            @NotBlank(message = "01-currentPassword-현재 비밀번호를 입력해주세요.")
            String currentPassword,
            @NotBlank(message = "03-newPassword1-새 비밀번호를 입력해주세요.")
            @Size(min = 4, message = "04-newPassword1-새 비밀번호는 4자 이상 입력해주세요.")
            String newPassword1,
            @NotBlank(message = "06-newPassword2-새 비밀번호 확인을 입력해주세요.")
            String newPassword2
    ) {
    }

    @GetMapping("/password") // GET /users/password : 비밀번호 변경 화면 (로그인한 회원)
    @PreAuthorize("isAuthenticated()")
    public String showChangePassword(@ModelAttribute("form") PasswordForm form) {
        return "user/user/password";
    }

    @PutMapping("/password") // PUT /users/password : 비밀번호 변경 처리 (_method=PUT)
    @PreAuthorize("isAuthenticated()")
    @Transactional
    public String changePassword(
            @ModelAttribute("form") @Valid PasswordForm form,
            BindingResult bindingResult,
            Principal principal
    ) {
        if (bindingResult.hasErrors()) return "user/user/password";

        SiteUser user = userService.findByUsername(principal.getName()).get();

        // 지금 비밀번호를 아는 사람만 바꿀 수 있다. (로그인한 채 자리를 비운 사이 남이 바꾸는 것 방지)
        if (!userService.matchesPassword(user, form.currentPassword())) {
            bindingResult.rejectValue("currentPassword", "wrong", "02-currentPassword-현재 비밀번호가 맞지 않습니다.");
        }
        if (!form.newPassword1().equals(form.newPassword2())) {
            bindingResult.rejectValue("newPassword2", "notSame", "07-newPassword2-새 비밀번호 2개가 일치하지 않습니다.");
        }
        if (form.newPassword1().equals(form.currentPassword())) {
            bindingResult.rejectValue("newPassword1", "same", "05-newPassword1-지금과 다른 비밀번호를 입력해주세요.");
        }
        if (bindingResult.hasErrors()) return "user/user/password";

        userService.changePassword(user, form.newPassword1());

        return "redirect:/users/password?changed"; // 새로고침해도 다시 전송되지 않게 (PRG)
    }

    public record ResetForm(
            @NotBlank(message = "01-email-가입할 때 쓴 이메일을 입력해주세요.")
            @Email(message = "02-email-이메일 형식이 올바르지 않습니다.")
            String email
    ) {
    }

    @GetMapping("/password/reset") // GET /users/password/reset : 비밀번호 찾기 화면 (로그인하지 않은 사람)
    public String showResetPassword(@ModelAttribute("form") ResetForm form) {
        return "user/user/passwordReset";
    }

    @PostMapping("/password/reset") // POST /users/password/reset : 임시 비밀번호 발송
    @Transactional
    public String resetPassword(
            @ModelAttribute("form") @Valid ResetForm form,
            BindingResult bindingResult
    ) {
        if (bindingResult.hasErrors()) return "user/user/passwordReset";

        userService.resetPassword(form.email());

        // 가입된 이메일이든 아니든 같은 안내 (가입 여부를 알려 주지 않는다)
        return "redirect:/users/password/reset?sent";
    }

    @GetMapping("/me") // GET /users/me : 내 프로필로 안내 (내비게이션 바의 "아이디님" 링크)
    @PreAuthorize("isAuthenticated()")
    public String myProfile(Principal principal) {
        return "redirect:/users/%s/profile".formatted(principal.getName());
    }

    // GET /users/{username}/profile : 회원 정보와 그 회원이 쓴 최근 질문·답변·댓글 (누구나 볼 수 있다)
    // /users/{username} 이 아닌 이유 : /users/join, /users/login 같은 주소와 겹치는 아이디(join 등)가 생길 수 있어서
    @GetMapping("/{username}/profile")
    @Transactional(readOnly = true)
    public String profile(@PathVariable String username, Model model, Principal principal) {
        SiteUser user = userService.findByUsername(username)
                .orElseThrow(() -> new DataNotFoundException("user not found")); // 없는 회원이면 404

        model.addAttribute("profileUser", user);
        // 이메일은 본인에게만 보여 준다.
        model.addAttribute("isMe", principal != null && principal.getName().equals(username));

        model.addAttribute("questionCount", questionService.countByAuthor(user));
        model.addAttribute("answerCount", answerService.countByAuthor(user));
        model.addAttribute("commentCount", commentService.countByAuthor(user));

        model.addAttribute("questions", questionService.findRecentByAuthor(user));
        model.addAttribute("answers", answerService.findRecentByAuthor(user));
        model.addAttribute("comments", commentService.findRecentByAuthor(user));

        return "user/user/profile";
    }
}
