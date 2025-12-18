package com.blog.comment.controller;

import com.blog.comment.dto.CommentEvent; // Import DTO Event
import com.blog.comment.entity.Comment;
import com.blog.comment.repository.CommentRepository;
import org.springframework.amqp.rabbit.core.RabbitTemplate; // Import RabbitMQ
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value; // Import để lấy cấu hình từ file properties
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
// URL theo cấu trúc Nested Resource
@RequestMapping("/api/posts/{postId}/comments")
public class CommentController {

    @Autowired
    private CommentRepository commentRepository;

    // 1. Inject RabbitTemplate để bắn tin nhắn
    @Autowired
    private RabbitTemplate rabbitTemplate;

    // 2. Lấy tên Exchange và Routing Key từ application.properties
    @Value("${rabbitmq.exchange.name}")
    private String exchange;

    @Value("${rabbitmq.routing.key}")
    private String routingKey;

    // --- 1. GET ALL COMMENTS ---
    @GetMapping
    public List<Comment> getCommentsByPostId(@PathVariable Long postId) {
        return commentRepository.findByPostId(postId);
    }

    // --- 2. GET COMMENT BY ID ---
    @GetMapping("/{commentId}")
    public Comment getCommentById(@PathVariable Long postId, @PathVariable Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found with id: " + commentId));

        if (!comment.getPostId().equals(postId)) {
            throw new RuntimeException("Comment này không thuộc về bài viết ID: " + postId);
        }

        return comment;
    }

    // --- 3. CREATE COMMENT (CÓ GỬI RABBITMQ) ---
    @PostMapping
    public Comment createComment(@PathVariable Long postId, @RequestBody Comment commentBody) {
        // B1: Gán postId và Lưu vào DB
        commentBody.setPostId(postId);
        Comment savedComment = commentRepository.save(commentBody);

        // B2: TẠO OBJECT EVENT (Để khớp với NotificationConsumer)
        CommentEvent event = new CommentEvent();
        event.setId(savedComment.getId());
        event.setContent(savedComment.getContent());
        event.setPostId(savedComment.getPostId());
        event.setUserId(savedComment.getUserId());

        // B3: GỬI SỰ KIỆN SANG RABBITMQ
        // Quan trọng: Gửi biến 'event', không gửi 'savedComment'
        rabbitTemplate.convertAndSend(exchange, routingKey, event);

        System.out.println("--> Đã gửi CommentEvent lên RabbitMQ: " + event.toString());

        return savedComment;
    }

    // --- 4. UPDATE COMMENT ---
    @PutMapping("/{commentId}")
    public Comment updateComment(@PathVariable Long postId,
                                 @PathVariable Long commentId,
                                 @RequestBody Comment commentDetails) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found"));

        if (!comment.getPostId().equals(postId)) {
            throw new RuntimeException("Comment không thuộc bài viết này!");
        }

        comment.setContent(commentDetails.getContent());
        return commentRepository.save(comment);
    }

    // --- 5. DELETE COMMENT ---
    @DeleteMapping("/{commentId}")
    public String deleteComment(@PathVariable Long postId, @PathVariable Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found"));

        if (!comment.getPostId().equals(postId)) {
            throw new RuntimeException("Comment không thuộc bài viết này!");
        }

        commentRepository.delete(comment);
        return "Đã xóa thành công comment ID: " + commentId;
    }
}