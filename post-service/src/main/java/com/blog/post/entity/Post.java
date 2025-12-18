package com.blog.post.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "posts")
public class Post {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String content;

    private String description;

    // MICROSERVICE PATTERN:
    // Thay vì: private User author;
    // Chúng ta chỉ lưu ID của tác giả
    @Column(name = "author_id")
    private Long authorId;

    @Column(name = "image_name")
    private String imageName; // Lưu tên file ảnh
}