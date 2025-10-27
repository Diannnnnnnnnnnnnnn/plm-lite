package com.example.graph_service.repository;

import com.example.graph_service.model.DocumentNode;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for DocumentNode operations in Neo4j.
 */
@Repository
public interface DocumentNodeRepository extends Neo4jRepository<DocumentNode, String> {

    /**
     * Find documents by status
     */
    List<DocumentNode> findByStatus(String status);

    /**
     * Find documents by version
     */
    List<DocumentNode> findByVersion(String version);

    /**
     * Find all documents affected by a change
     */
    @Query("""
        MATCH (c:Change {id: $changeId})-[:AFFECTS]->(p:Part)
        MATCH (p)<-[:LINKED_TO]-(d:Document)
        RETURN DISTINCT d
        """)
    List<DocumentNode> findDocumentsAffectedByChange(@Param("changeId") String changeId);

    /**
     * Find documents linked to a specific part
     */
    @Query("""
        MATCH (p:Part {id: $partId})-[:LINKED_TO]->(d:Document)
        RETURN d
        """)
    List<DocumentNode> findDocumentsByPart(@Param("partId") String partId);

    /**
     * Find documents by creator
     */
    @Query("""
        MATCH (u:User {id: $userId})<-[:CREATED_BY]-(d:Document)
        RETURN d
        """)
    List<DocumentNode> findDocumentsByCreator(@Param("userId") String userId);

    /**
     * Find related documents through parts
     */
    @Query("""
        MATCH (d1:Document {id: $documentId})<-[:LINKED_TO]-(p:Part)-[:LINKED_TO]->(d2:Document)
        WHERE d1.id <> d2.id
        RETURN DISTINCT d2
        """)
    List<DocumentNode> findRelatedDocuments(@Param("documentId") String documentId);
}

