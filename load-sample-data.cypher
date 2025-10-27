// ==========================================
// Quick Sample Data for Neo4j PLM Graph
// ==========================================
// Run this in Neo4j Browser: http://localhost:7474
// Copy & paste the entire script and hit Run

// ==========================================
// 1. Create Sample Parts (BOM Structure)
// ==========================================

CREATE (p1:Part {
    id: 'part-001',
    title: 'Engine Assembly',
    stage: 'DETAILED_DESIGN',
    level: '1',
    creator: 'engineer1'
});

CREATE (p2:Part {
    id: 'part-002',
    title: 'Cylinder Block',
    stage: 'DETAILED_DESIGN',
    level: '2',
    creator: 'engineer1'
});

CREATE (p3:Part {
    id: 'part-003',
    title: 'Piston Assembly',
    stage: 'PRODUCTION',
    level: '3',
    creator: 'engineer2'
});

CREATE (p4:Part {
    id: 'part-004',
    title: 'Crankshaft',
    stage: 'PRODUCTION',
    level: '2',
    creator: 'engineer1'
});

// ==========================================
// 2. Create BOM Hierarchy
// ==========================================

MATCH (p1:Part {id: 'part-001'}), (p2:Part {id: 'part-002'})
CREATE (p1)-[:HAS_CHILD {quantity: 1}]->(p2);

MATCH (p1:Part {id: 'part-001'}), (p4:Part {id: 'part-004'})
CREATE (p1)-[:HAS_CHILD {quantity: 1}]->(p4);

MATCH (p2:Part {id: 'part-002'}), (p3:Part {id: 'part-003'})
CREATE (p2)-[:HAS_CHILD {quantity: 4}]->(p3);

// ==========================================
// 3. Create Sample Documents
// ==========================================

CREATE (d1:Document {
    id: 'doc-001',
    name: 'Engine Assembly Drawing v1.0',
    description: 'Technical drawing',
    version: '1.0',
    status: 'APPROVED'
});

CREATE (d2:Document {
    id: 'doc-002',
    name: 'Piston Specifications',
    description: 'Material specs',
    version: '2.0',
    status: 'UNDER_REVIEW'
});

// ==========================================
// 4. Link Parts to Documents
// ==========================================

MATCH (p1:Part {id: 'part-001'}), (d1:Document {id: 'doc-001'})
CREATE (p1)-[:LINKED_TO]->(d1);

MATCH (p3:Part {id: 'part-003'}), (d2:Document {id: 'doc-002'})
CREATE (p3)-[:LINKED_TO]->(d2);

// ==========================================
// 5. Create Sample Changes
// ==========================================

CREATE (c1:Change {
    id: 'change-001',
    title: 'Material Change Request',
    description: 'Update cylinder block to aluminum',
    status: 'PENDING',
    priority: 'HIGH'
});

CREATE (c2:Change {
    id: 'change-002',
    title: 'Design Update',
    description: 'Update piston design',
    status: 'APPROVED',
    priority: 'MEDIUM'
});

// ==========================================
// 6. Link Changes to Parts
// ==========================================

MATCH (c1:Change {id: 'change-001'}), (p2:Part {id: 'part-002'})
CREATE (c1)-[:AFFECTS]->(p2);

MATCH (c2:Change {id: 'change-002'}), (p3:Part {id: 'part-003'})
CREATE (c2)-[:AFFECTS]->(p3);

// ==========================================
// ✅ DONE! Verify the data:
// ==========================================

// Count everything
MATCH (n)
RETURN labels(n)[0] as Type, COUNT(n) as Count
ORDER BY Count DESC;

