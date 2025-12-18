package com.blog.post.dto;

import lombok.Data;

@Data
public class PostDto {
    private String title;
    private String content;
    private String description;
    // Client gửi token lên, ta sẽ giải mã token để lấy authorId sau.
    // Nhưng tạm thời để test, ta cho phép gửi authorId trực tiếp.
    private Long authorId;
}