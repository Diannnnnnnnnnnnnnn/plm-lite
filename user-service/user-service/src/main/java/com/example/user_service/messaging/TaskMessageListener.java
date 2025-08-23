package com.example.user_service.messaging;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import com.example.user_service.config.RabbitMQConfig;

@Service
public class TaskMessageListener {

    @RabbitListener(queues = RabbitMQConfig.QUEUE)
    public void handleTaskCreatedMessage(String message) {
        // React to the message here
        System.out.println("📥 Received message in user-service: " + message);

        // You can add more logic, like notifying the user or logging it to DB
    }
}
