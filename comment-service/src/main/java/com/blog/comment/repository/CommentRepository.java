package com.blog.comment.repository;

import com.blog.comment.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    // Tìm tất cả bình luận của một bài viết cụ thể
    // Spring Data JPA sẽ tự động tạo câu lệnh: SELECT * FROM comments WHERE post_id = ?
    List<Comment> findByPostId(Long postId);
}