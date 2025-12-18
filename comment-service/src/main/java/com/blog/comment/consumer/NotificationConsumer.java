package com.blog.comment.consumer;

import com.blog.comment.dto.CommentEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
public class NotificationConsumer {

    private static final Logger LOGGER = LoggerFactory.getLogger(NotificationConsumer.class);

    @RabbitListener(queues = "${rabbitmq.queue.name}")
    public void consume(CommentEvent event){
        LOGGER.info(String.format("DA NHAN DUOC EVENT TU RABBITMQ -> %s", event.toString()));

        // Giả lập logic gửi email mất 5s
        // Thread.sleep(5000);

        System.out.println("LOGIC GUI EMAIL: Đang gửi email tới tác giả bài viết " + event.getPostId());
    }
}