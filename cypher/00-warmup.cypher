// Warm-up: the two triples from the lesson.
//   Application A  --dependsOn-->  Microservice B
//   Microservice B --runsOn-->     Azure
// Run this in the Neo4j Browser (http://localhost:7474), one statement at a time.

// 1. Create the triples. MERGE = "create if it doesn't already exist".
MERGE (a:App     {name: "Application A"})
MERGE (b:Service {name: "Microservice B"})
MERGE (c:Cloud   {name: "Azure"})
MERGE (a)-[:DEPENDS_ON]->(b)
MERGE (b)-[:RUNS_ON]->(c);

// 2. Where does Application A ultimately run?
MATCH (a:App {name: "Application A"})-[:DEPENDS_ON]->(:Service)-[:RUNS_ON]->(c:Cloud)
RETURN c.name;

// 3. Reverse question: if Azure goes down, what is affected? (arrows can be read backwards)
MATCH (c:Cloud {name: "Azure"})<-[:RUNS_ON]-(s)<-[:DEPENDS_ON*0..]-(x)
RETURN DISTINCT x.name;

// 4. Clean up so the warm-up doesn't mix with the aerospace data.
MATCH (n) WHERE n:App OR n:Service OR n:Cloud DETACH DELETE n;
