package com.blog.comment.controller;

import com.blog.comment.dto.CommentEvent;
import com.blog.comment.entity.Comment;
import io.smallrye.reactive.messaging.rabbitmq.OutgoingRabbitMQMetadata;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.eclipse.microprofile.reactive.messaging.Message;

import java.util.List;

@Path("/api/posts/{postId}/comments")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class CommentController {
    @Inject
    @Channel("comment-events")
    Emitter<CommentEvent> emitter;

    @ConfigProperty(name = "rabbitmq.routing.key")
    String routingKey;

    @GET
    public List<Comment> getCommentsByPostId(@PathParam("postId") Long postId) {
        return Comment.findByPostId(postId);
    }

    @GET
    @Path("/{commentId}")
    public Comment getCommentById(@PathParam("postId") Long postId, @PathParam("commentId") Long commentId) {
        Comment comment = findCommentInPost(postId, commentId);
        return comment;
    }

    @POST
    @Transactional
    public Response createComment(@PathParam("postId") Long postId, Comment commentBody) {
        try {
            System.out.println("Nhan yeu cau binh luan cho post " + postId + ": " + (commentBody != null ? commentBody.content : "null"));
            commentBody.postId = postId;
            commentBody.persist();

            try {
                CommentEvent event = new CommentEvent(commentBody.id, commentBody.content, commentBody.postId, commentBody.userId);
                OutgoingRabbitMQMetadata metadata = OutgoingRabbitMQMetadata.builder()
                        .withRoutingKey(routingKey)
                        .build();
                emitter.send(Message.of(event).addMetadata(metadata));
            } catch (Exception e) {
                System.err.println("Loi gui message RabbitMQ: " + e.getMessage());
                // Khong throw exception o day de van cho phep tao comment neu RabbitMQ loi
            }

            return Response.status(Response.Status.CREATED).entity(commentBody).build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"message\": \"Loi createComment: " + e.getMessage() + "\"}").build();
        }
    }

    @PUT
    @Path("/{commentId}")
    @Transactional
    public Comment updateComment(@PathParam("postId") Long postId,
                                 @PathParam("commentId") Long commentId,
                                 Comment commentDetails) {
        Comment comment = findCommentInPost(postId, commentId);
        comment.content = commentDetails.content;
        return comment;
    }

    @DELETE
    @Path("/{commentId}")
    @Transactional
    @Produces(MediaType.TEXT_PLAIN)
    public String deleteComment(@PathParam("postId") Long postId, @PathParam("commentId") Long commentId) {
        Comment comment = findCommentInPost(postId, commentId);
        comment.delete();
        return "Deleted comment ID: " + commentId;
    }

    private Comment findCommentInPost(Long postId, Long commentId) {
        Comment comment = Comment.<Comment>findByIdOptional(commentId)
                .orElseThrow(() -> new WebApplicationException("Comment not found with id: " + commentId, Response.Status.NOT_FOUND));
        if (!comment.postId.equals(postId)) {
            throw new WebApplicationException("Comment does not belong to post ID: " + postId, Response.Status.BAD_REQUEST);
        }
        return comment;
    }
}
