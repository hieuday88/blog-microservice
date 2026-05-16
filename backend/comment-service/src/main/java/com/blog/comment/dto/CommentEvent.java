package com.blog.comment.dto;

public class CommentEvent {
    public Long id;
    public String content;
    public Long postId;
    public Long userId;

    public CommentEvent() {
    }

    public CommentEvent(Long id, String content, Long postId, Long userId) {
        this.id = id;
        this.content = content;
        this.postId = postId;
        this.userId = userId;
    }

    @Override
    public String toString() {
        return "CommentEvent{" +
                "id=" + id +
                ", content='" + content + '\'' +
                ", postId=" + postId +
                ", userId=" + userId +
                '}';
    }
}
