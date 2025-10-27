package com.example.graph_service.repository;

import com.example.graph_service.model.ChangeNode;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for ChangeNode operations in Neo4j.
 */
@Repository
public interface ChangeNodeRepository extends Neo4jRepository<ChangeNode, String> {

    /**
     * Find changes by status
     */
    List<ChangeNode> findByStatus(String status);

    /**
     * Find changes by priority
     */
    List<ChangeNode> findByPriority(String priority);

    /**
     * Find changes affecting a specific part
     */
    @Query("""
        MATCH (c:Change)-[:AFFECTS]->(p:Part {id: $partId})
        RETURN c
        """)
    List<ChangeNode> findChangesAffectingPart(@Param("partId") String partId);

    /**
     * Find changes initiated by a user
     */
    @Query("""
        MATCH (u:User {id: $userId})<-[:INITIATED_BY]-(c:Change)
        RETURN c
        """)
    List<ChangeNode> findChangesByInitiator(@Param("userId") String userId);

    /**
     * Find changes reviewed by a user
     */
    @Query("""
        MATCH (u:User {id: $userId})<-[:REVIEWED_BY]-(c:Change)
        RETURN c
        """)
    List<ChangeNode> findChangesByReviewer(@Param("userId") String userId);

    /**
     * Find changes related to a document
     */
    @Query("""
        MATCH (d:Document {id: $documentId})-[:RELATED_TO]->(c:Change)
        RETURN c
        """)
    List<ChangeNode> findChangesByDocument(@Param("documentId") String documentId);

    /**
     * Calculate total impact of a change (number of affected parts and their children)
     */
    @Query("""
        MATCH (c:Change {id: $changeId})-[:AFFECTS]->(p:Part)
        MATCH (p)-[:HAS_CHILD*0..]->(child:Part)
        RETURN COUNT(DISTINCT child) as impactCount
        """)
    Integer calculateChangeImpact(@Param("changeId") String changeId);
}

