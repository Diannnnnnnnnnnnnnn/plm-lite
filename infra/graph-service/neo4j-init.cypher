// ==========================================
// Neo4j Initialization Script for PLM-Lite
// ==========================================
// Run this script in Neo4j Browser (http://localhost:7474) after starting Neo4j
// Or use cypher-shell: cypher-shell -u neo4j -p password < neo4j-init.cypher

// ==========================================
// 1. Create Uniqueness Constraints
// ==========================================
// These constraints ensure IDs are unique and automatically create indexes

CREATE CONSTRAINT part_id_unique IF NOT EXISTS 
FOR (p:Part) REQUIRE p.id IS UNIQUE;

CREATE CONSTRAINT user_id_unique IF NOT EXISTS 
FOR (u:User) REQUIRE u.id IS UNIQUE;

CREATE CONSTRAINT document_id_unique IF NOT EXISTS 
FOR (d:Document) REQUIRE d.id IS UNIQUE;

CREATE CONSTRAINT change_id_unique IF NOT EXISTS 
FOR (c:Change) REQUIRE c.id IS UNIQUE;

CREATE CONSTRAINT task_id_unique IF NOT EXISTS 
FOR (t:Task) REQUIRE t.id IS UNIQUE;

// ==========================================
// 2. Create Indexes for Common Queries
// ==========================================

// Part indexes
CREATE INDEX part_stage_idx IF NOT EXISTS 
FOR (p:Part) ON (p.stage);

CREATE INDEX part_level_idx IF NOT EXISTS 
FOR (p:Part) ON (p.level);

CREATE INDEX part_creator_idx IF NOT EXISTS 
FOR (p:Part) ON (p.creator);

CREATE INDEX part_title_idx IF NOT EXISTS 
FOR (p:Part) ON (p.title);

// Composite index for common queries
CREATE INDEX part_stage_level_idx IF NOT EXISTS 
FOR (p:Part) ON (p.stage, p.level);

// User indexes
CREATE INDEX user_username_idx IF NOT EXISTS 
FOR (u:User) ON (u.username);

CREATE INDEX user_email_idx IF NOT EXISTS 
FOR (u:User) ON (u.email);

CREATE INDEX user_department_idx IF NOT EXISTS 
FOR (u:User) ON (u.department);

CREATE INDEX user_role_idx IF NOT EXISTS 
FOR (u:User) ON (u.role);

// Document indexes
CREATE INDEX document_status_idx IF NOT EXISTS 
FOR (d:Document) ON (d.status);

CREATE INDEX document_version_idx IF NOT EXISTS 
FOR (d:Document) ON (d.version);

CREATE INDEX document_name_idx IF NOT EXISTS 
FOR (d:Document) ON (d.name);

// Change indexes
CREATE INDEX change_status_idx IF NOT EXISTS 
FOR (c:Change) ON (c.status);

CREATE INDEX change_priority_idx IF NOT EXISTS 
FOR (c:Change) ON (c.priority);

CREATE INDEX change_type_idx IF NOT EXISTS 
FOR (c:Change) ON (c.changeType);

// Task indexes
CREATE INDEX task_status_idx IF NOT EXISTS 
FOR (t:Task) ON (t.status);

// ==========================================
// 3. Verify Setup
// ==========================================

// Show all constraints
SHOW CONSTRAINTS;

// Show all indexes
SHOW INDEXES;

// ==========================================
// 4. Sample Data (Optional - for testing)
// ==========================================

// Create sample users
CREATE (u1:User {
    id: 'user-001',
    username: 'john.doe',
    email: 'john.doe@example.com',
    department: 'Engineering',
    role: 'Engineer'
})

CREATE (u2:User {
    id: 'user-002',
    username: 'jane.smith',
    email: 'jane.smith@example.com',
    department: 'Engineering',
    role: 'Senior Engineer'
})

// Create organizational hierarchy
MATCH (u1:User {id: 'user-001'}), (u2:User {id: 'user-002'})
CREATE (u1)-[:REPORTS_TO]->(u2);

// Create sample parts
CREATE (p1:Part {
    id: 'part-001',
    title: 'Engine Assembly',
    stage: 'DETAILED_DESIGN',
    level: '1',
    creator: 'john.doe'
})

CREATE (p2:Part {
    id: 'part-002',
    title: 'Cylinder Block',
    stage: 'DETAILED_DESIGN',
    level: '2',
    creator: 'john.doe'
})

CREATE (p3:Part {
    id: 'part-003',
    title: 'Piston',
    stage: 'PRODUCTION',
    level: '3',
    creator: 'jane.smith'
});

// Create part hierarchy
MATCH (p1:Part {id: 'part-001'}), (p2:Part {id: 'part-002'}), (p3:Part {id: 'part-003'})
CREATE (p1)-[:HAS_CHILD {quantity: 1}]->(p2)
CREATE (p2)-[:HAS_CHILD {quantity: 4}]->(p3);

// Link parts to creators
MATCH (p1:Part {id: 'part-001'}), (u1:User {id: 'user-001'})
CREATE (p1)-[:CREATED_BY]->(u1);

MATCH (p2:Part {id: 'part-002'}), (u1:User {id: 'user-001'})
CREATE (p2)-[:CREATED_BY]->(u1);

MATCH (p3:Part {id: 'part-003'}), (u2:User {id: 'user-002'})
CREATE (p3)-[:CREATED_BY]->(u2);

// Create sample documents
CREATE (d1:Document {
    id: 'doc-001',
    name: 'Engine Assembly Drawing',
    description: 'Technical drawing for engine assembly',
    version: '1.0',
    status: 'APPROVED'
})

CREATE (d2:Document {
    id: 'doc-002',
    name: 'Cylinder Block Specification',
    description: 'Material and dimension specs',
    version: '2.0',
    status: 'UNDER_REVIEW'
});

// Link documents to parts
MATCH (p1:Part {id: 'part-001'}), (d1:Document {id: 'doc-001'})
CREATE (p1)-[:LINKED_TO]->(d1);

MATCH (p2:Part {id: 'part-002'}), (d2:Document {id: 'doc-002'})
CREATE (p2)-[:LINKED_TO]->(d2);

// Link documents to creators
MATCH (d1:Document {id: 'doc-001'}), (u1:User {id: 'user-001'})
CREATE (d1)-[:CREATED_BY]->(u1);

// Create sample change request
CREATE (c1:Change {
    id: 'change-001',
    title: 'Update Cylinder Block Material',
    description: 'Change from aluminum to steel',
    status: 'PENDING',
    priority: 'HIGH',
    changeType: 'ENGINEERING_CHANGE'
});

// Link change to affected parts
MATCH (c1:Change {id: 'change-001'}), (p2:Part {id: 'part-002'})
CREATE (c1)-[:AFFECTS]->(p2);

// Link change to initiator
MATCH (c1:Change {id: 'change-001'}), (u2:User {id: 'user-002'})
CREATE (c1)-[:INITIATED_BY]->(u2);

// Create sample tasks
CREATE (t1:Task {
    id: 'task-001',
    title: 'Review Cylinder Block Change',
    description: 'Evaluate impact of material change',
    status: 'IN_PROGRESS'
});

// Link task to user and change
MATCH (t1:Task {id: 'task-001'}), (u1:User {id: 'user-001'}), (c1:Change {id: 'change-001'})
CREATE (u1)-[:ASSIGNED_TO]->(t1)
CREATE (t1)-[:RELATED_TO_CHANGE]->(c1);

// ==========================================
// 5. Verification Queries
// ==========================================

// Count nodes by type
MATCH (p:Part) RETURN 'Parts' as Type, COUNT(p) as Count
UNION
MATCH (u:User) RETURN 'Users' as Type, COUNT(u) as Count
UNION
MATCH (d:Document) RETURN 'Documents' as Type, COUNT(d) as Count
UNION
MATCH (c:Change) RETURN 'Changes' as Type, COUNT(c) as Count
UNION
MATCH (t:Task) RETURN 'Tasks' as Type, COUNT(t) as Count;

// Count relationships by type
MATCH ()-[r:HAS_CHILD]->() RETURN 'HAS_CHILD' as Type, COUNT(r) as Count
UNION
MATCH ()-[r:LINKED_TO]->() RETURN 'LINKED_TO' as Type, COUNT(r) as Count
UNION
MATCH ()-[r:CREATED_BY]->() RETURN 'CREATED_BY' as Type, COUNT(r) as Count
UNION
MATCH ()-[r:AFFECTS]->() RETURN 'AFFECTS' as Type, COUNT(r) as Count
UNION
MATCH ()-[r:ASSIGNED_TO]->() RETURN 'ASSIGNED_TO' as Type, COUNT(r) as Count;

// Show BOM hierarchy
MATCH path = (p1:Part {title: 'Engine Assembly'})-[:HAS_CHILD*]->(child:Part)
RETURN path;

// ==========================================
// Setup Complete!
// ==========================================
// You can now:
// 1. View the graph in Neo4j Browser
// 2. Start the graph-service (mvn spring-boot:run)
// 3. Test the REST APIs
// ==========================================

