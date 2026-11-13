package com.back.domain.question.answer.controller;

import com.back.domain.question.answer.entity.Answer;
import com.back.domain.question.answer.service.AnswerService;
import com.back.domain.question.question.entity.Question;
import com.back.domain.question.question.service.QuestionService;
import com.back.domain.user.user.entity.SiteUser;
import com.back.domain.user.user.service.UserService;
import com.back.global.exception.DataNotFoundException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;

@Controller
@RequestMapping("/questions/{questionId}/answers") // "몇 번 질문의 답변들" 이라는 자원
@RequiredArgsConstructor
public class AnswerController {
    private final QuestionService questionService; // 답변의 생성·수정·삭제는 질문을 통해서 한다.
    private final UserService userService;
    private final AnswerService answerService; // 상세 화면에 답변을 페이지 단위로 읽어 올 때만

    public record WriteForm(
            @NotBlank(message = "01-content-답변 내용을 입력해주세요.")
            @Size(min = 2, message = "02-content-답변 내용은 2자 이상 입력해주세요.")
            String content
    ) {
    }

    @PostMapping // POST /questions/{questionId}/answers : 답변 생성
    @PreAuthorize("isAuthenticated()") // 로그인한 회원만
    @Transactional // 이 메서드가 끝날 때 커밋 → 그때 addAnswer 로 추가한 답변이 INSERT 된다.
    public String write(
            @PathVariable int questionId,
            @ModelAttribute("answerForm") @Valid WriteForm form,
            BindingResult bindingResult,
            Model model,
            Principal principal
    ) {
        Question question = questionService.getQuestion(questionId);

        if (bindingResult.hasErrors()) {
            // 검증 실패 → 상세 페이지를 다시 그린다. 상세 페이지에는 질문이 필요하므로 모델에 넣어 준다.
            model.addAttribute("question", question);
            model.addAttribute("answerPaging", answerService.findPage(question, 0, AnswerService.SortCode.latest));
            model.addAttribute("so", AnswerService.SortCode.latest.name());
            return "question/question/detail";
        }

        SiteUser author = userService.findByUsername(principal.getName()).get();
        Answer answer = question.addAnswer(author, form.content());

        // 답변은 트랜잭션이 끝날 때 INSERT 되므로 지금은 id 가 0 이다. (005-07)
        // flush 로 INSERT 를 지금 실행시키면 DB 가 매긴 번호가 answer.id 에 들어온다. → 앵커 주소를 만들 수 있다.
        questionService.flush();

        // 등록 후 상세 페이지의 새 답변 위치로 이동 (새로고침해도 다시 등록되지 않게)
        return "redirect:/questions/%d#answer_%d".formatted(questionId, answer.getId());
    }

    // questionId 번 질문의 id 번 답변. 없으면 404
    private Answer getAnswer(Question question, int id) {
        return question
                .findAnswerById(id)
                .orElseThrow(() -> new DataNotFoundException("answer not found"));
    }

    // 글쓴이 본인인지 확인. 아니면 403
    private void checkAuthor(Answer answer, Principal principal) {
        if (answer.getAuthor() == null || !answer.getAuthor().getUsername().equals(principal.getName())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "수정/삭제 권한이 없습니다.");
        }
    }

    public record ModifyForm(
            @NotBlank(message = "01-content-답변 내용을 입력해주세요.")
            @Size(min = 2, message = "02-content-답변 내용은 2자 이상 입력해주세요.")
            String content
    ) {
    }

    @GetMapping("/{id}/modify") // GET /questions/{questionId}/answers/{id}/modify : 답변 수정 화면
    @PreAuthorize("isAuthenticated()")
    @Transactional(readOnly = true)
    public String showModify(@PathVariable int questionId, @PathVariable int id, Principal principal, Model model) {
        Question question = questionService.getQuestion(questionId);
        Answer answer = getAnswer(question, id);
        checkAuthor(answer, principal);

        model.addAttribute("answer", answer);
        model.addAttribute("form", new ModifyForm(answer.getContent()));

        return "question/answer/modify";
    }

    @PutMapping("/{id}/modify") // PUT /questions/{questionId}/answers/{id}/modify : 답변 수정 처리
    @PreAuthorize("isAuthenticated()")
    @Transactional
    public String modify(
            @PathVariable int questionId,
            @PathVariable int id,
            @ModelAttribute("form") @Valid ModifyForm form,
            BindingResult bindingResult,
            Principal principal,
            Model model
    ) {
        Question question = questionService.getQuestion(questionId);
        Answer answer = getAnswer(question, id);
        checkAuthor(answer, principal);

        if (bindingResult.hasErrors()) {
            model.addAttribute("answer", answer);
            return "question/answer/modify";
        }

        answer.modify(form.content()); // 더티체킹으로 UPDATE

        return "redirect:/questions/%d?ap=%d#answer_%d".formatted(questionId, answerService.latestPageOf(answer), id); // 수정한 답변 위치로
    }

    @DeleteMapping("/{id}") // DELETE /questions/{questionId}/answers/{id} : 답변 삭제
    @PreAuthorize("isAuthenticated()")
    @Transactional
    public String delete(@PathVariable int questionId, @PathVariable int id, Principal principal) {
        Question question = questionService.getQuestion(questionId);
        Answer answer = getAnswer(question, id);
        checkAuthor(answer, principal);

        question.deleteAnswer(answer);

        return "redirect:/questions/" + questionId;
    }

    @PostMapping("/{id}/vote") // POST /questions/{questionId}/answers/{id}/vote : 답변 추천
    @PreAuthorize("isAuthenticated()")
    @Transactional
    public String vote(@PathVariable int questionId, @PathVariable int id, Principal principal) {
        Question question = questionService.getQuestion(questionId);
        Answer answer = getAnswer(question, id);
        SiteUser voter = userService.findByUsername(principal.getName()).get();

        answer.vote(voter);

        return "redirect:/questions/%d?ap=%d#answer_%d".formatted(questionId, answerService.latestPageOf(answer), id); // 추천한 답변 위치로
    }
}
