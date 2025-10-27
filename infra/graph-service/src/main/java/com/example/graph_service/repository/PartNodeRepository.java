package com.example.graph_service.repository;

import com.example.graph_service.model.PartNode;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for PartNode operations in Neo4j.
 * Provides both CRUD operations and advanced graph queries.
 */
@Repository
public interface PartNodeRepository extends Neo4jRepository<PartNode, String> {

    /**
     * Find parts by stage
     */
    List<PartNode> findByStage(String stage);

    /**
     * Find parts by creator
     */
    List<PartNode> findByCreator(String creator);

    /**
     * Find all parts affected by a specific change
     */
    @Query("""
        MATCH (c:Change {id: $changeId})-[:AFFECTS]->(p:Part)
        RETURN p
        """)
    List<PartNode> findPartsAffectedByChange(@Param("changeId") String changeId);

    /**
     * Find all child parts affected by a change (recursive)
     */
    @Query("""
        MATCH (c:Change {id: $changeId})-[:AFFECTS]->(p:Part)
        MATCH (p)-[:HAS_CHILD*]->(child:Part)
        RETURN DISTINCT child
        """)
    List<PartNode> findAllChildPartsAffectedByChange(@Param("changeId") String changeId);

    /**
     * Get BOM explosion (all descendants) for a part
     */
    @Query("""
        MATCH path = (p:Part {id: $partId})-[:HAS_CHILD*]->(child:Part)
        RETURN child
        """)
    List<PartNode> findBomExplosion(@Param("partId") String partId);

    /**
     * Get where-used (all ancestors) for a part
     */
    @Query("""
        MATCH path = (p:Part {id: $partId})<-[:HAS_CHILD*]-(parent:Part)
        RETURN parent
        """)
    List<PartNode> findWhereUsed(@Param("partId") String partId);

    /**
     * Find all parts linked to a specific document
     */
    @Query("""
        MATCH (d:Document {id: $documentId})<-[:LINKED_TO]-(p:Part)
        RETURN p
        """)
    List<PartNode> findPartsByDocument(@Param("documentId") String documentId);

    /**
     * Find all parts impacted by a document (including hierarchy)
     */
    @Query("""
        MATCH (d:Document {id: $documentId})<-[:LINKED_TO]-(p:Part)
        MATCH (p)-[:HAS_CHILD*0..]->(related:Part)
        RETURN DISTINCT related
        """)
    List<PartNode> findAllPartsImpactedByDocument(@Param("documentId") String documentId);

    /**
     * Find parts at a specific level
     */
    List<PartNode> findByLevel(String level);

    /**
     * Check if there's a circular dependency
     */
    @Query("""
        MATCH path = (p1:Part {id: $partId1})-[:HAS_CHILD*]->(p2:Part {id: $partId2})
        RETURN COUNT(path) > 0 as hasPath
        """)
    Boolean hasCircularDependency(@Param("partId1") String partId1, @Param("partId2") String partId2);
}

