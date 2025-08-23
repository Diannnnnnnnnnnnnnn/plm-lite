package com.example.graph_service.repository;

import org.springframework.data.neo4j.repository.Neo4jRepository;

import com.example.graph_service.model.TaskNode;

public interface TaskNodeRepository extends Neo4jRepository<TaskNode, String> {
}
