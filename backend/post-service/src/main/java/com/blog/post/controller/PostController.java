package com.blog.post.controller;

import com.blog.post.dto.PostCreateDto;
import com.blog.post.entity.Post;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;



import java.time.LocalDateTime;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@Path("/api/posts")
@Produces(MediaType.APPLICATION_JSON)
public class PostController {
    public static final java.nio.file.Path UPLOAD_DIRECTORY = getUploadPath();

    private static java.nio.file.Path getUploadPath() {
        java.nio.file.Path currentPath = Paths.get(System.getProperty("user.dir"));
        // Nếu đang ở trong backend/post-service, đi ngược lên 2 cấp để ra gốc dự án
        if (currentPath.toString().contains("post-service")) {
            return currentPath.getParent().getParent().resolve("uploads").normalize();
        }
        return currentPath.resolve("uploads");
    }

    @POST
    @Transactional
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createPost(PostCreateDto dto) {
        try {
            System.out.println("Nhan yeu cau tao post: " + dto.title);

            Post post = new Post();
            post.title = dto.title;
            post.content = dto.content;
            post.description = dto.description;
            post.authorId = dto.authorId;
            post.authorUsername = dto.authorUsername;
            post.createdAt = LocalDateTime.now();
            post.imageName = saveImage(dto.imageBase64, dto.imageFileName);

            post.persist();
            return Response.status(Response.Status.CREATED).entity(post).build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"message\": \"Loi createPost: " + e.getMessage() + "\"}").build();
        }
    }

    @GET
    public PageResponse<Post> getAllPosts(@QueryParam("page") @DefaultValue("0") int page,
                                          @QueryParam("size") @DefaultValue("5") int size,
                                          @QueryParam("sortBy") @DefaultValue("id") String sortBy,
                                          @QueryParam("sortDir") @DefaultValue("asc") String sortDir) {
        Sort sort = "desc".equalsIgnoreCase(sortDir)
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();
        PanacheQuery<Post> query = Post.findAll(sort).page(Page.of(page, size));
        return new PageResponse<>(query.list(), page, size, query.count());
    }

    @GET
    @Path("/{id}")
    public Post getPostById(@PathParam("id") Long id) {
        return findPost(id);
    }

    @GET
    @Path("/user/{userId}")
    public List<Post> getPostsByAuthorId(@PathParam("userId") Long userId) {
        return Post.findByAuthorId(userId);
    }

    @PUT
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Transactional
    public Post updatePost(@PathParam("id") Long id, PostCreateDto postDetails) {
        Post post = findPost(id);
        post.title = postDetails.title;
        post.content = postDetails.content;
        post.description = postDetails.description;
        post.authorId = postDetails.authorId != null ? postDetails.authorId : post.authorId;
        String imageName = saveImage(postDetails.imageBase64, postDetails.imageFileName);
        if (imageName != null) {
            post.imageName = imageName;
        }
        return post;
    }

    @DELETE
    @Path("/{id}")
    @Transactional
    @Produces(MediaType.TEXT_PLAIN)
    public String deletePost(@PathParam("id") Long id) {
        Post post = findPost(id);
        String imageName = post.imageName;

        // Xoá trong database
        post.delete();

        // Xoá file vật lý
        if (imageName != null && !imageName.isEmpty()) {
            try {
                java.nio.file.Path filePath = UPLOAD_DIRECTORY.resolve(imageName);
                Files.deleteIfExists(filePath);
            } catch (IOException e) {
                System.err.println("Lỗi khi xoá file ảnh: " + e.getMessage());
            }
        }
        return "Deleted post with ID: " + id;
    }

    private Post findPost(Long id) {
        return Post.<Post>findByIdOptional(id)
                .orElseThrow(() -> new WebApplicationException("Post not found with id: " + id, Response.Status.NOT_FOUND));
    }

    @GET
    @Path("/images/{filename}")
    public Response getImage(@PathParam("filename") String filename) throws IOException {
        java.nio.file.Path filePath = UPLOAD_DIRECTORY.resolve(filename);
        if (Files.exists(filePath)) {
            String contentType = Files.probeContentType(filePath);
            return Response.ok(Files.readAllBytes(filePath))
                    .header("Content-Type", contentType == null ? "image/jpeg" : contentType)
                    .build();
        }
        return Response.status(Response.Status.NOT_FOUND).build();
    }

    private String saveImage(String imageBase64, String imageFileName) {
        try {
            if (imageBase64 == null || imageBase64.isEmpty()) {
                return null;
            }

            Files.createDirectories(UPLOAD_DIRECTORY);
            String extension = ".jpg";
            if (imageFileName != null) {
                int i = imageFileName.lastIndexOf('.');
                if (i > 0) extension = imageFileName.substring(i);
            }
            String uniqueFilename = UUID.randomUUID().toString() + extension;
            java.nio.file.Path targetPath = UPLOAD_DIRECTORY.resolve(uniqueFilename);

            String base64Data = imageBase64;
            if (base64Data.contains(",")) {
                base64Data = base64Data.substring(base64Data.indexOf(",") + 1);
            }
            byte[] imageBytes = java.util.Base64.getDecoder().decode(base64Data);
            Files.write(targetPath, imageBytes);
            return uniqueFilename;
        } catch (Exception e) {
            throw new WebApplicationException("Không lưu được ảnh bài viết: " + e.getMessage(), Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    public record PageResponse<T>(List<T> content, int page, int size, long totalElements) {
    }
}
