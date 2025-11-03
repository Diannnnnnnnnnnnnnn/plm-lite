package com.example.task_service.repository.neo4j;

import com.example.task_service.model.neo4j.TaskNode;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TaskNodeRepository extends Neo4jRepository<TaskNode, Long> {
    
    Optional<TaskNode> findByTaskId(String taskId);
    
    List<TaskNode> findByTaskStatus(String taskStatus);
    
    List<TaskNode> findByTaskType(String taskType);
    
    @Query("MATCH (t:Task)-[:ASSIGNED_TO]->(u:User {userId: $userId}) RETURN t")
    List<TaskNode> findTasksAssignedToUser(@Param("userId") String userId);
    
    @Query("MATCH (t:Task)-[:PART_OF]->(w:Workflow {workflowId: $workflowId}) RETURN t")
    List<TaskNode> findTasksByWorkflow(@Param("workflowId") String workflowId);
    
    @Query("MATCH (t1:Task)-[:DEPENDS_ON]->(t2:Task {taskId: $taskId}) RETURN t1")
    List<TaskNode> findDependentTasks(@Param("taskId") String taskId);
}

