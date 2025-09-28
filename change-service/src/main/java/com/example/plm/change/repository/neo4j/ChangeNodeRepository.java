package com.example.plm.change.repository.neo4j;

import com.example.plm.change.model.ChangeNode;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ChangeNodeRepository extends Neo4jRepository<ChangeNode, String> {

    @Query("MATCH (c:Change)-[:AFFECTS_DOCUMENT]->(d:Document) WHERE c.id = $changeId RETURN d")
    List<String> findAffectedDocumentsByChangeId(@Param("changeId") String changeId);

    @Query("MATCH (c:Change)-[:AFFECTS_PART]->(p:Part) WHERE c.id = $changeId RETURN p")
    List<String> findAffectedPartsByChangeId(@Param("changeId") String changeId);

    @Query("MATCH (c:Change)-[:CREATED_BY]->(u:User) WHERE c.id = $changeId RETURN u")
    List<String> findCreatorsByChangeId(@Param("changeId") String changeId);

    @Query("MATCH (d:Document)<-[:AFFECTS_DOCUMENT]-(c:Change) WHERE d.id = $documentId RETURN c")
    List<ChangeNode> findChangesByAffectedDocument(@Param("documentId") String documentId);

    @Query("MATCH (p:Part)<-[:AFFECTS_PART]-(c:Change) WHERE p.id = $partId RETURN c")
    List<ChangeNode> findChangesByAffectedPart(@Param("partId") String partId);

    @Query("MATCH (u:User)<-[:CREATED_BY]-(c:Change) WHERE u.id = $userId RETURN c")
    List<ChangeNode> findChangesByCreator(@Param("userId") String userId);

    @Query("""
        MATCH (c:Change {id: $changeId})
        MATCH (d:Document {id: $documentId})
        MERGE (c)-[:AFFECTS_DOCUMENT]->(d)
        """)
    void createDocumentRelationship(@Param("changeId") String changeId, @Param("documentId") String documentId);

    @Query("""
        MATCH (c:Change {id: $changeId})
        MATCH (p:Part {id: $partId})
        MERGE (c)-[:AFFECTS_PART]->(p)
        """)
    void createPartRelationship(@Param("changeId") String changeId, @Param("partId") String partId);

    @Query("""
        MATCH (c:Change {id: $changeId})
        MATCH (u:User {id: $userId})
        MERGE (c)-[:CREATED_BY]->(u)
        """)
    void createCreatorRelationship(@Param("changeId") String changeId, @Param("userId") String userId);
}