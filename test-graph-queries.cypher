// ==========================================
// Test Queries for Neo4j PLM Graph
// ==========================================
// Run these in Neo4j Browser after populating data

// ==========================================
// 1. VIEW ALL DATA
// ==========================================

// View everything
MATCH (n) RETURN n LIMIT 100;

// View all relationships
MATCH p=()-[r]->() RETURN p LIMIT 100;

// ==========================================
// 2. BOM QUERIES
// ==========================================

// BOM Explosion - Show complete hierarchy from Engine
MATCH path = (p:Part {title: 'Engine Assembly'})-[:HAS_CHILD*]->(child:Part)
RETURN path;

// BOM Explosion - List all child parts with quantities
MATCH path = (p:Part {title: 'Engine Assembly'})-[r:HAS_CHILD*]->(child:Part)
RETURN child.title as Part, child.level as Level, 
       [rel in relationships(path) | rel.quantity] as Quantities;

// Where Used - Find what assemblies use a specific part
MATCH path = (p:Part {title: 'Piston Assembly'})<-[:HAS_CHILD*]-(parent:Part)
RETURN path;

// Find all parts at a specific level
MATCH (p:Part {level: '2'})
RETURN p.title, p.stage, p.creator;

// ==========================================
// 3. CHANGE IMPACT ANALYSIS
// ==========================================

// Find all parts directly affected by a change
MATCH (c:Change {id: 'change-001'})-[:AFFECTS]->(p:Part)
RETURN c.title as Change, p.title as AffectedPart;

// Find ALL parts affected (including children in BOM)
MATCH (c:Change {id: 'change-001'})-[:AFFECTS]->(p:Part)
MATCH (p)-[:HAS_CHILD*0..]->(child:Part)
RETURN DISTINCT child.title as AffectedPart, child.level as Level
ORDER BY child.level;

// Find documents affected by a change (through parts)
MATCH (c:Change {id: 'change-001'})-[:AFFECTS]->(p:Part)
MATCH (p)-[:HAS_CHILD*0..]->(affected:Part)-[:LINKED_TO]->(d:Document)
RETURN DISTINCT c.title as Change, d.name as AffectedDocument, d.status as Status;

// Calculate total impact of a change
MATCH (c:Change {id: 'change-001'})-[:AFFECTS]->(p:Part)
MATCH (p)-[:HAS_CHILD*0..]->(child:Part)
RETURN c.title as Change, COUNT(DISTINCT child) as TotalPartsImpacted;

// ==========================================
// 4. DOCUMENT QUERIES
// ==========================================

// Find all parts linked to a document
MATCH (d:Document {id: 'doc-002'})<-[:LINKED_TO]-(p:Part)
RETURN d.name as Document, p.title as LinkedPart;

// Find all documents for a part (including parent parts)
MATCH (p:Part {title: 'Piston Assembly'})
MATCH (parent:Part)-[:HAS_CHILD*0..]->(p)
MATCH (parent)-[:LINKED_TO]->(d:Document)
RETURN DISTINCT d.name as Document, d.version as Version, parent.title as RelatedPart;

// Find related documents (documents that share parts)
MATCH (d1:Document {id: 'doc-001'})<-[:LINKED_TO]-(p:Part)-[:LINKED_TO]->(d2:Document)
WHERE d1.id <> d2.id
RETURN d1.name as Document1, d2.name as RelatedDocument;

// ==========================================
// 5. USER & ORGANIZATIONAL QUERIES
// ==========================================

// Find all parts created by a user
MATCH (u:User {username: 'engineer1'})<-[:CREATED_BY]-(p:Part)
RETURN u.username as Engineer, p.title as PartCreated, p.stage as Stage;

// Find all changes initiated by a user
MATCH (u:User)<-[:INITIATED_BY]-(c:Change)
RETURN u.username as User, c.title as Change, c.status as Status;

// Find user's tasks
MATCH (u:User)-[:ASSIGNED_TO]->(t:Task)
RETURN u.username as User, t.title as Task, t.status as Status;

// ==========================================
// 6. ADVANCED ANALYTICS
// ==========================================

// Find most complex parts (most children)
MATCH (p:Part)-[:HAS_CHILD]->(child:Part)
RETURN p.title as Part, COUNT(child) as DirectChildren
ORDER BY DirectChildren DESC;

// Find parts in multiple changes (high risk)
MATCH (c:Change)-[:AFFECTS]->(p:Part)
RETURN p.title as Part, COUNT(c) as NumberOfChanges
ORDER BY NumberOfChanges DESC;

// Find most active engineers
MATCH (u:User)<-[:CREATED_BY]-(entity)
RETURN u.username as Engineer, 
       labels(entity)[0] as EntityType,
       COUNT(entity) as Count
ORDER BY Count DESC;

// Find orphan parts (no parent)
MATCH (p:Part)
WHERE NOT ()-[:HAS_CHILD]->(p)
RETURN p.title as OrphanPart, p.level as Level;

// Find parts with no documents
MATCH (p:Part)
WHERE NOT (p)-[:LINKED_TO]->(:Document)
RETURN p.title as PartWithoutDocs, p.stage as Stage;

// ==========================================
// 7. SHORTEST PATH QUERIES
// ==========================================

// Find connection between two parts
MATCH path = shortestPath(
    (p1:Part {title: 'Engine Assembly'})-[*]-(p2:Part {title: 'Head Gasket'})
)
RETURN path;

// Find how many steps from root part
MATCH path = (root:Part {level: '1'})-[:HAS_CHILD*]->(leaf:Part {title: 'Head Gasket'})
RETURN length(path) as Steps, [n in nodes(path) | n.title] as PathTitles;

// ==========================================
// 8. STATISTICS
// ==========================================

// Overall statistics
MATCH (n)
RETURN labels(n)[0] as NodeType, COUNT(n) as Count
ORDER BY Count DESC;

// Relationship statistics
MATCH ()-[r]->()
RETURN type(r) as RelationshipType, COUNT(r) as Count
ORDER BY Count DESC;

// Parts by stage
MATCH (p:Part)
RETURN p.stage as Stage, COUNT(p) as Count
ORDER BY Count DESC;

// Documents by status
MATCH (d:Document)
RETURN d.status as Status, COUNT(d) as Count
ORDER BY Count DESC;

// Changes by priority
MATCH (c:Change)
RETURN c.priority as Priority, COUNT(c) as Count
ORDER BY Count DESC;

// ==========================================
// Happy Querying! 🎉
// ==========================================

