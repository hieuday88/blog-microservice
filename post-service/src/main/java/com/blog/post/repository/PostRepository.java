package com.blog.post.repository;

import com.blog.post.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {
    // Có thể thêm method tìm bài viết theo tác giả nếu cần
    List<Post> findByAuthorId(Long authorId);
}