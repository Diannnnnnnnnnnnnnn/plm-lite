// ==========================================
// Populate Neo4j with Sample PLM Data
// ==========================================
// Run this in Neo4j Browser (http://localhost:7474)

// ==========================================
// 1. Create Sample Parts (BOM Structure)
// ==========================================

// Root part - Engine Assembly
CREATE (p1:Part {
    id: 'part-engine-001',
    title: 'Engine Assembly',
    stage: 'DETAILED_DESIGN',
    level: '1',
    creator: 'engineer1',
    createTime: datetime()
});

// Level 2 parts
CREATE (p2:Part {
    id: 'part-block-001',
    title: 'Cylinder Block',
    stage: 'DETAILED_DESIGN',
    level: '2',
    creator: 'engineer1',
    createTime: datetime()
});

CREATE (p3:Part {
    id: 'part-crank-001',
    title: 'Crankshaft',
    stage: 'PRODUCTION',
    level: '2',
    creator: 'engineer2',
    createTime: datetime()
});

// Level 3 parts
CREATE (p4:Part {
    id: 'part-piston-001',
    title: 'Piston Assembly',
    stage: 'PRODUCTION',
    level: '3',
    creator: 'engineer1',
    createTime: datetime()
});

CREATE (p5:Part {
    id: 'part-gasket-001',
    title: 'Head Gasket',
    stage: 'PRODUCTION',
    level: '3',
    creator: 'engineer2',
    createTime: datetime()
});

// ==========================================
// 2. Create BOM Hierarchy (HAS_CHILD)
// ==========================================

// Engine Assembly has Cylinder Block and Crankshaft
MATCH (p1:Part {id: 'part-engine-001'}), (p2:Part {id: 'part-block-001'})
CREATE (p1)-[:HAS_CHILD {quantity: 1, createdAt: datetime()}]->(p2);

MATCH (p1:Part {id: 'part-engine-001'}), (p3:Part {id: 'part-crank-001'})
CREATE (p1)-[:HAS_CHILD {quantity: 1, createdAt: datetime()}]->(p3);

// Cylinder Block has Pistons and Gasket
MATCH (p2:Part {id: 'part-block-001'}), (p4:Part {id: 'part-piston-001'})
CREATE (p2)-[:HAS_CHILD {quantity: 4, createdAt: datetime()}]->(p4);

MATCH (p2:Part {id: 'part-block-001'}), (p5:Part {id: 'part-gasket-001'})
CREATE (p2)-[:HAS_CHILD {quantity: 1, createdAt: datetime()}]->(p5);

// ==========================================
// 3. Create Sample Documents
// ==========================================

CREATE (d1:Document {
    id: 'doc-001',
    name: 'Engine Assembly Drawing',
    description: 'Technical drawing for complete engine assembly',
    version: '1.0',
    status: 'APPROVED',
    fileType: 'PDF',
    fileSize: 2048576,
    createTime: datetime()
});

CREATE (d2:Document {
    id: 'doc-002',
    name: 'Cylinder Block Specifications',
    description: 'Material specifications and tolerances',
    version: '2.1',
    status: 'APPROVED',
    fileType: 'PDF',
    fileSize: 1024000,
    createTime: datetime()
});

CREATE (d3:Document {
    id: 'doc-003',
    name: 'Piston Assembly Manual',
    description: 'Assembly instructions and quality checks',
    version: '1.5',
    status: 'UNDER_REVIEW',
    fileType: 'DOCX',
    fileSize: 512000,
    createTime: datetime()
});

// ==========================================
// 4. Link Parts to Documents (LINKED_TO)
// ==========================================

MATCH (p1:Part {id: 'part-engine-001'}), (d1:Document {id: 'doc-001'})
CREATE (p1)-[:LINKED_TO]->(d1);

MATCH (p2:Part {id: 'part-block-001'}), (d2:Document {id: 'doc-002'})
CREATE (p2)-[:LINKED_TO]->(d2);

MATCH (p4:Part {id: 'part-piston-001'}), (d3:Document {id: 'doc-003'})
CREATE (p4)-[:LINKED_TO]->(d3);

// ==========================================
// 5. Create Sample Changes
// ==========================================

CREATE (c1:Change {
    id: 'change-001',
    title: 'Material Change - Cylinder Block',
    description: 'Change from cast iron to aluminum alloy for weight reduction',
    status: 'PENDING_APPROVAL',
    priority: 'HIGH',
    changeType: 'ENGINEERING_CHANGE',
    createTime: datetime()
});

CREATE (c2:Change {
    id: 'change-002',
    title: 'Gasket Design Update',
    description: 'Update gasket design for better sealing',
    status: 'IN_REVIEW',
    priority: 'MEDIUM',
    changeType: 'DESIGN_CHANGE',
    createTime: datetime()
});

// ==========================================
// 6. Link Changes to Parts (AFFECTS)
// ==========================================

MATCH (c1:Change {id: 'change-001'}), (p2:Part {id: 'part-block-001'})
CREATE (c1)-[:AFFECTS]->(p2);

MATCH (c2:Change {id: 'change-002'}), (p5:Part {id: 'part-gasket-001'})
CREATE (c2)-[:AFFECTS]->(p5);

// ==========================================
// 7. Link to Users (CREATED_BY, INITIATED_BY)
// ==========================================

// Create engineer users if they don't exist
MERGE (u1:User {id: 'user-engineer1', username: 'engineer1', email: 'engineer1@example.com', department: 'Engineering', role: 'Engineer'});
MERGE (u2:User {id: 'user-engineer2', username: 'engineer2', email: 'engineer2@example.com', department: 'Engineering', role: 'Senior Engineer'});

// Link parts to creators
MATCH (p1:Part {id: 'part-engine-001'}), (u1:User {id: 'user-engineer1'})
CREATE (p1)-[:CREATED_BY]->(u1);

MATCH (p2:Part {id: 'part-block-001'}), (u1:User {id: 'user-engineer1'})
CREATE (p2)-[:CREATED_BY]->(u1);

MATCH (p3:Part {id: 'part-crank-001'}), (u2:User {id: 'user-engineer2'})
CREATE (p3)-[:CREATED_BY]->(u2);

// Link documents to creators
MATCH (d1:Document {id: 'doc-001'}), (u1:User {id: 'user-engineer1'})
CREATE (d1)-[:CREATED_BY]->(u1);

MATCH (d2:Document {id: 'doc-002'}), (u1:User {id: 'user-engineer1'})
CREATE (d2)-[:CREATED_BY]->(u1);

// Link changes to initiators
MATCH (c1:Change {id: 'change-001'}), (u2:User {id: 'user-engineer2'})
CREATE (c1)-[:INITIATED_BY]->(u2);

MATCH (c2:Change {id: 'change-002'}), (u1:User {id: 'user-engineer1'})
CREATE (c2)-[:INITIATED_BY]->(u1);

// ==========================================
// 8. Verification - Count Everything
// ==========================================

MATCH (p:Part) RETURN 'Parts' as Type, COUNT(p) as Count
UNION
MATCH (d:Document) RETURN 'Documents' as Type, COUNT(d) as Count
UNION
MATCH (c:Change) RETURN 'Changes' as Type, COUNT(c) as Count
UNION
MATCH (u:User) RETURN 'Users' as Type, COUNT(u) as Count
UNION
MATCH (t:Task) RETURN 'Tasks' as Type, COUNT(t) as Count;

// ==========================================
// 9. Verification - Count Relationships
// ==========================================

MATCH ()-[r:HAS_CHILD]->() RETURN 'HAS_CHILD (BOM)' as Type, COUNT(r) as Count
UNION
MATCH ()-[r:LINKED_TO]->() RETURN 'LINKED_TO (Part-Doc)' as Type, COUNT(r) as Count
UNION
MATCH ()-[r:AFFECTS]->() RETURN 'AFFECTS (Change-Part)' as Type, COUNT(r) as Count
UNION
MATCH ()-[r:CREATED_BY]->() RETURN 'CREATED_BY' as Type, COUNT(r) as Count
UNION
MATCH ()-[r:INITIATED_BY]->() RETURN 'INITIATED_BY' as Type, COUNT(r) as Count
UNION
MATCH ()-[r:ASSIGNED_TO]->() RETURN 'ASSIGNED_TO' as Type, COUNT(r) as Count;

// ==========================================
// Sample Data Created Successfully! 🎉
// ==========================================

