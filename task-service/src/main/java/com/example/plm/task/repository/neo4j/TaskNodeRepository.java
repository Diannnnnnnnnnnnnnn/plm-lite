package com.example.plm.task.repository.neo4j;

import com.example.plm.task.model.neo4j.TaskNode;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskNodeRepository extends Neo4jRepository<TaskNode, String> {

    List<TaskNode> findByAssignedTo(String assignedTo);

    List<TaskNode> findByWorkflowId(String workflowId);

    List<TaskNode> findByContextTypeAndContextId(String contextType, String contextId);

    @Query("MATCH (t:Task)-[:DEPENDS_ON]->(dep:Task) WHERE t.id = $taskId RETURN dep")
    List<TaskNode> findDependencies(@Param("taskId") String taskId);

    @Query("MATCH (t:Task)-[:PARENT_OF]->(child:Task) WHERE t.id = $taskId RETURN child")
    List<TaskNode> findSubtasks(@Param("taskId") String taskId);

    @Query("MATCH (t:Task {id: $taskId})-[:DEPENDS_ON]->(dep:Task) " +
           "CREATE (t)-[:DEPENDS_ON]->(newDep:Task {id: $dependencyId})")
    void createDependency(@Param("taskId") String taskId, @Param("dependencyId") String dependencyId);

    @Query("MATCH (parent:Task {id: $parentId})-[:PARENT_OF]->(child:Task {id: $childId}) " +
           "DELETE r")
    void removeParentChildRelationship(@Param("parentId") String parentId, @Param("childId") String childId);

    @Query("MATCH (parent:Task {id: $parentId}), (child:Task {id: $childId}) " +
           "CREATE (parent)-[:PARENT_OF]->(child)")
    void createParentChildRelationship(@Param("parentId") String parentId, @Param("childId") String childId);

    @Query("MATCH (t:Task)-[:ASSIGNED_TO]->(u:User) WHERE t.id = $taskId RETURN u")
    List<Object> findAssignedUsers(@Param("taskId") String taskId);

    @Query("MATCH (t:Task {id: $taskId}), (u:User {id: $userId}) " +
           "CREATE (t)-[:ASSIGNED_TO]->(u)")
    void assignToUser(@Param("taskId") String taskId, @Param("userId") String userId);

    @Query("MATCH (workflow:Workflow {id: $workflowId}), (task:Task {id: $taskId}) " +
           "CREATE (workflow)-[:CONTAINS]->(task)")
    void linkToWorkflow(@Param("workflowId") String workflowId, @Param("taskId") String taskId);
}