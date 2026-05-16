package com.blog.comment.consumer;

import com.blog.comment.dto.CommentEvent;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.reactive.messaging.Incoming;

@ApplicationScoped
public class NotificationConsumer {
    @Incoming("notifications")
    public void consume(CommentEvent event) {
        Log.infof("Received RabbitMQ event -> %s", event);
        Log.infof("Sending email notification for post %d", event.postId);
    }
}
