package com.blog.post.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.List;

@Entity
@Table(name = "posts")
public class Post extends PanacheEntityBase {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(nullable = false)
    public String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    public String content;

    public String description;

    @Column(name = "author_id")
    public Long authorId;

    @Column(name = "author_username")
    public String authorUsername;

    @Column(name = "image_name")
    public String imageName;

    @Column(name = "created_at")
    public java.time.LocalDateTime createdAt;

    public static List<Post> findByAuthorId(Long authorId) {
        return list("authorId", authorId);
    }
}
