package com.back.domain.question.comment.service;

import com.back.domain.question.comment.entity.Comment;
import com.back.domain.question.comment.repository.CommentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentService {
    private final CommentRepository commentRepository;

    public List<Comment> findRecent() {
        return commentRepository.findTop10ByOrderByIdDesc();
    }
}
