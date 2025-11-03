package com.example.task_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication(
    exclude = {
        org.springframework.cloud.netflix.eureka.EurekaClientAutoConfiguration.class
    },
    scanBasePackages = {"com.example.task_service"}
)
@EnableFeignClients(basePackages = {"com.example.task_service"})
// @EnableCaching  // Enable caching - disabled for now

public class TaskServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(TaskServiceApplication.class, args);
	}

}

