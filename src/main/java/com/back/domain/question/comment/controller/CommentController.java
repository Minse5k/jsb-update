package com.back.domain.question.comment.controller;

import com.back.domain.question.comment.entity.Comment;
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
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;

// 댓글은 질문 아래, 답변 아래 두 곳에 달리므로 주소도 두 갈래
//   질문 댓글 : /questions/{questionId}/comments
//   답변 댓글 : /questions/{questionId}/answers/{answerId}/comments (029-03)
@Controller
@RequiredArgsConstructor
public class CommentController {
    private final QuestionService questionService;
    private final UserService userService;

    public record WriteForm(
            @NotBlank
            @Size(max = 500)
            String content
    ) {
    }

    private void checkAuthor(Comment comment, Principal principal) {
        if (comment.getAuthor() == null || !comment.getAuthor().getUsername().equals(principal.getName())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "삭제 권한이 없습니다.");
        }
    }

    @PostMapping("/questions/{questionId}/comments") // 질문 댓글 등록
    @PreAuthorize("isAuthenticated()")
    @Transactional
    public String writeOnQuestion(
            @PathVariable int questionId,
            @ModelAttribute("commentForm") @Valid WriteForm form,
            BindingResult bindingResult,
            Principal principal
    ) {
        Question question = questionService.getQuestion(questionId);

        // 한 줄짜리 입력이라 에러 화면을 따로 그리지 않는다. 비었거나 너무 길면 저장하지 않고 돌아간다.
        // (입력칸에 required, maxlength 를 걸어 브라우저가 먼저 막는다.)
        if (!bindingResult.hasErrors()) {
            SiteUser author = userService.findByUsername(principal.getName()).get();
            question.addComment(author, form.content().trim());
        }

        return "redirect:/questions/%d#question_comments".formatted(questionId);
    }

    @DeleteMapping("/questions/{questionId}/comments/{id}") // 질문 댓글 삭제
    @PreAuthorize("isAuthenticated()")
    @Transactional
    public String deleteOnQuestion(@PathVariable int questionId, @PathVariable int id, Principal principal) {
        Question question = questionService.getQuestion(questionId);
        Comment comment = question.findCommentById(id).orElseThrow(() -> new DataNotFoundException("comment not found"));
        checkAuthor(comment, principal);

        question.deleteComment(comment);

        return "redirect:/questions/%d#question_comments".formatted(questionId);
    }
}
