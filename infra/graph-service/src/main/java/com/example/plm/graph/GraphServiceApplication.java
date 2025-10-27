package com.example.plm.graph;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.neo4j.repository.config.EnableNeo4jRepositories;

@SpringBootApplication
@EnableFeignClients
@EnableNeo4jRepositories(basePackages = "com.example.graph_service.repository")
@ComponentScan(basePackages = {
    "com.example.plm.graph",
    "com.example.graph_service"
})
public class GraphServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(GraphServiceApplication.class, args);
	}
}