package com.blog.comment.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommentEvent {
    private Long id;
    private String content;
    private Long postId;
    private Long userId;
}