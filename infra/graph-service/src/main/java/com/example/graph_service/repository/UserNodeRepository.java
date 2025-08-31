package com.example.graph_service.repository;

import org.springframework.data.neo4j.repository.Neo4jRepository;

import com.example.graph_service.model.UserNode;


public interface UserNodeRepository extends Neo4jRepository<UserNode, String> {
}
