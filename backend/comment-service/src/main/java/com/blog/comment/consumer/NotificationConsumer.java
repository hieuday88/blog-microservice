package com.blog.comment.consumer;

import com.blog.comment.dto.CommentEvent;
import io.quarkus.logging.Log;
import io.vertx.core.json.JsonObject;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.reactive.messaging.Incoming;

@ApplicationScoped
public class NotificationConsumer {
    @Incoming("notifications")
    public void consume(JsonObject json) {
        CommentEvent event = json.mapTo(CommentEvent.class);
        Log.infof("Received RabbitMQ event -> %s", event);
        Log.infof("Sending email notification for post %d", event.postId);
    }
}
