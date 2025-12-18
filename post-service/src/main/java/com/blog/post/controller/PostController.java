package com.blog.post.controller;

import com.blog.post.entity.Post;
import com.blog.post.repository.PostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType; // <--- 1. QUAN TRỌNG: Import cái này
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@RestController
@RequestMapping("/api/posts")
public class PostController {

    @Autowired
    private PostRepository postRepository;

    // Định nghĩa thư mục lưu ảnh
    public static String UPLOAD_DIRECTORY = System.getProperty("user.dir") + "/uploads";

    // --- 1. TẠO BÀI VIẾT MỚI (Có up ảnh) ---
    // 2. QUAN TRỌNG: Thêm 'consumes' để Swagger biết đây là form upload file
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Post createPost(
            @RequestParam("title") String title,
            @RequestParam("content") String content,
            @RequestParam("description") String description,
            @RequestParam("authorId") Long authorId,
            @RequestParam("image") MultipartFile file) throws IOException {

        // Logic lưu file ảnh
        Path uploadPath = Paths.get(UPLOAD_DIRECTORY);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
        String originalFilename = file.getOriginalFilename();
        // Tạo tên file độc nhất để tránh bị trùng đè lên nhau
        String uniqueFilename = UUID.randomUUID().toString() + "_" + originalFilename;
        Path filePath = uploadPath.resolve(uniqueFilename);
        Files.copy(file.getInputStream(), filePath);

        // Logic lưu Database
        Post post = new Post();
        post.setTitle(title);
        post.setContent(content);
        post.setDescription(description);
        post.setAuthorId(authorId);
        post.setImageName(uniqueFilename); // Chỉ lưu tên file vào DB

        return postRepository.save(post);
    }

    // --- 2. LẤY TẤT CẢ BÀI VIẾT (Có phân trang) ---
    @GetMapping
    public Page<Post> getAllPosts(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "5") int size,
            @RequestParam(name = "sortBy", defaultValue = "id") String sortBy,
            @RequestParam(name = "sortDir", defaultValue = "asc") String sortDir
    ) {
        // Logic sắp xếp
        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name())
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        // Tạo đối tượng phân trang
        Pageable pageable = PageRequest.of(page, size, sort);

        return postRepository.findAll(pageable);
    }

    // --- 3. LẤY CHI TIẾT 1 BÀI THEO ID ---
    @GetMapping("/{id}")
    public Post getPostById(@PathVariable Long id) {
        return postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Post not found with id: " + id));
    }

    // --- 4. LẤY BÀI VIẾT CỦA 1 USER CỤ THỂ ---
    @GetMapping("/user/{userId}")
    public List<Post> getPostsByAuthorId(@PathVariable Long userId) {
        return postRepository.findByAuthorId(userId);
    }

    // --- 5. SỬA BÀI VIẾT (UPDATE) ---
    @PutMapping("/{id}")
    public Post updatePost(@PathVariable Long id, @RequestBody Post postDetails) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Post not found with id: " + id));

        post.setTitle(postDetails.getTitle());
        post.setContent(postDetails.getContent());
        post.setDescription(postDetails.getDescription());

        return postRepository.save(post);
    }

    // --- 6. XÓA BÀI VIẾT (DELETE) ---
    @DeleteMapping("/{id}")
    public String deletePost(@PathVariable Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Post not found with id: " + id));

        postRepository.delete(post);
        return "Đã xóa thành công bài viết có ID: " + id;
    }
}