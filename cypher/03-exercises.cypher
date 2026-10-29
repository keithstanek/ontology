// Practice queries against the aerospace graph. Try writing each one yourself
// before reading the answer underneath.

// 1. Show the whole graph (click nodes in the Browser to explore).
MATCH (n) RETURN n;

// 2. Which components make up the Flight Control system?
MATCH (:System {name: "Flight Control"})-[:COMPOSED_OF]->(c:Component)
RETURN c.partNumber, c.name;

// 3. What does the Elevator Actuator directly depend on?
MATCH (:Component {partNumber: "ACT-1"})-[:DEPENDS_ON]->(dep)
RETURN dep.name;

// 4. ...and transitively? (*1.. = follow the arrow one or more hops)
MATCH (:Component {partNumber: "ACT-1"})-[:DEPENDS_ON*1..]->(dep)
RETURN DISTINCT dep.name;

// 5. Impact analysis: if the cooling loop fails, what else breaks, and which aircraft are hit?
MATCH (failed:Component {partNumber: "COOL-1"})
OPTIONAL MATCH (d:Component)-[:DEPENDS_ON*1..]->(failed)
WITH failed, [failed] + collect(DISTINCT d) AS broken
UNWIND broken AS c
MATCH (a:Aircraft)-[:HAS_SYSTEM]->(s:System)-[:COMPOSED_OF]->(c)
RETURN collect(DISTINCT c.name) AS broken, collect(DISTINCT s.name) AS systems, collect(DISTINCT a.tailNumber) AS aircraft;

// 6. Single-sourced parts (only one supplier): a supply-chain risk.
MATCH (c:Component)-[:SUPPLIED_BY]->(s:Supplier)
WITH c, collect(s.name) AS suppliers
WHERE size(suppliers) = 1
RETURN c.partNumber, c.name, suppliers[0] AS soleSupplier;

// 7. The most "load-bearing" components: how many parts depend on each, directly or not.
MATCH (c:Component)
OPTIONAL MATCH (d:Component)-[:DEPENDS_ON*1..]->(c)
RETURN c.name, count(DISTINCT d) AS dependents
ORDER BY dependents DESC;

// 8. Show the dependency path from the Radar down to the Generator.
MATCH p = shortestPath((:Component {partNumber: "RDR-1"})-[:DEPENDS_ON*]->(:Component {partNumber: "GEN-1"}))
RETURN p;
