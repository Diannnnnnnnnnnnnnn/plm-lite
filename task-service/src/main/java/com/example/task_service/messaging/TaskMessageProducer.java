package com.example.task_service.messaging;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import com.example.task_service.config.RabbitMQConfig;

@Service
public class TaskMessageProducer {

    private final RabbitTemplate rabbitTemplate;

    // Constructor injection
    public TaskMessageProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    // Method to send message
    public void sendTaskCreatedMessage(String taskId) {
        // Create the message content (this can be a String, JSON, or custom object)
        String message = "Task created with ID: " + taskId;

        // Send the message to the exchange with the routing key
        rabbitTemplate.convertAndSend(
            RabbitMQConfig.EXCHANGE,      // exchange name
            RabbitMQConfig.ROUTING_KEY,   // routing key
            message                       // actual message
        );

        System.out.println("📨 Message sent to RabbitMQ: " + message);
    }
}
