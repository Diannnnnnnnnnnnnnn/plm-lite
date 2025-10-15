package com.example.plm.change;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.neo4j.Neo4jDataAutoConfiguration;
import org.springframework.boot.autoconfigure.data.neo4j.Neo4jRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.elasticsearch.ElasticsearchRestClientAutoConfiguration;
import org.springframework.cloud.netflix.eureka.EurekaClientAutoConfiguration;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Profile;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.neo4j.repository.config.EnableNeo4jRepositories;

@SpringBootApplication(
    scanBasePackages = {"com.example.plm.change", "com.example.plm.common"},
    exclude = {
        Neo4jDataAutoConfiguration.class,
        Neo4jRepositoriesAutoConfiguration.class,
        ElasticsearchRestClientAutoConfiguration.class,
        EurekaClientAutoConfiguration.class,
        org.springframework.boot.autoconfigure.data.elasticsearch.ElasticsearchDataAutoConfiguration.class,
        org.springframework.boot.autoconfigure.data.elasticsearch.ElasticsearchRepositoriesAutoConfiguration.class,
        org.springframework.boot.autoconfigure.data.elasticsearch.ReactiveElasticsearchRepositoriesAutoConfiguration.class,
        org.springframework.boot.autoconfigure.data.neo4j.Neo4jReactiveDataAutoConfiguration.class,
        org.springframework.boot.autoconfigure.data.neo4j.Neo4jReactiveRepositoriesAutoConfiguration.class
    }
)
@EnableFeignClients
@EnableJpaRepositories(basePackages = "com.example.plm.change.repository.mysql")
public class ChangeServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ChangeServiceApplication.class, args);
    }

    @Profile("!dev")
    @EnableNeo4jRepositories(basePackages = "com.example.plm.change.repository.neo4j")
    @EnableElasticsearchRepositories(basePackages = "com.example.plm.change.repository.elasticsearch")
    static class ProductionConfiguration {
    }
}