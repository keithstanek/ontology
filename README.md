# Aerospace Agent: learn ontologies by building one

A fictional aircraft fleet modeled as a knowledge graph in Neo4j, with a Spring Boot +
Spring Data Neo4j service that answers "what breaks if this part fails?"

Phase 1 (this repo now): graph model, Cypher, Spring Data Neo4j.
Phase 2 (next): a LangChain4j agent that calls `ImpactAnalysisService` as a tool.

## The ontology

An ontology is a shared vocabulary for a domain: what *kinds* of things exist (classes),
what facts you can state about them (properties), and how they can relate (relationships).
Every fact is a **triple**: subject → predicate → object.

```
(:Aircraft)-[:HAS_SYSTEM]->(:System)-[:COMPOSED_OF]->(:Component)
(:Component)-[:DEPENDS_ON]->(:Component)        "A needs B to work"
(:Component)-[:SUPPLIED_BY]->(:Supplier)
```

| Ontology term    | Neo4j                     | Java (SDN)                    | SQL analogue        |
|------------------|---------------------------|-------------------------------|---------------------|
| Class            | Label `:Component`        | `@Node("Component")`          | Table               |
| Individual       | Node                      | Entity instance               | Row                 |
| Data property    | Node property `name`      | Field                         | Column              |
| Object property  | Relationship `DEPENDS_ON` | `@Relationship` field         | Foreign key / join table |
| Key / identity   | Uniqueness constraint     | `@Id`                         | Primary key         |

Neo4j is a *property graph*. The W3C semantic-web stack (RDF, OWL, SPARQL) expresses the
same triples but adds formal reasoning, e.g. declaring `DEPENDS_ON` transitive so a
reasoner infers indirect dependencies. In Cypher you get the same answer by writing
`-[:DEPENDS_ON*1..]->` in the query.

## Run it

JDK 21, Maven and Neo4j Community 5.26 are unpacked in `~/tools` (prebuilt binaries;
Homebrew would have compiled them from source on this Mac).

```bash
source ~/tools/env.sh            # puts java, mvn, neo4j, cypher-shell on PATH

# 1. Start Neo4j (password already set to ontology123)
neo4j start                      # Browser UI: http://localhost:7474  (neo4j / ontology123)

# 2. Load the schema and data (already done; re-running is safe)
cypher-shell -u neo4j -p ontology123 -f cypher/01-schema.cypher
cypher-shell -u neo4j -p ontology123 -f cypher/02-seed.cypher

# 3. Run the service
mvn spring-boot:run

# When finished
neo4j stop
```

## Try it

```bash
curl localhost:8080/api/components
curl localhost:8080/api/components/ACT-1/dependencies     # everything the elevator actuator needs
curl localhost:8080/api/components/COOL-1/impact          # cooling loop fails -> radar out on the AX jets only
curl localhost:8080/api/components/GEN-1/impact           # generator fails -> whole fleet grounded
curl localhost:8080/api/suppliers/SkyWave%20Sensors/impact
curl localhost:8080/api/aircraft
```

## Learning path

1. `cypher/00-warmup.cypher`: the App → Service → Cloud triples, in the Browser.
2. `cypher/03-exercises.cypher`: eight queries, from simple lookups to impact analysis and
   supply-chain risk. Try to write each one before reading the answer.
3. Read `ImpactAnalysisService.java`: the same traversals, called from Java.
4. Extend the ontology. Some ideas:
   - The supplier-impact query has a modeling bug: IMU-1 is dual-sourced (NavCore *and*
     SkyWave), yet a SkyWave outage still marks it broken. Fix it so a part only fails when
     *all* of its suppliers are down.
   - Add a `REDUNDANT_WITH` relationship (BAT-1 backs up GEN-1) and change the impact query
     so a part only breaks when *all* of its redundant sources fail.
   - Add a `:Mission` class that `REQUIRES` systems, and ask "which missions can AX-101 still fly?"
   - Add `installedOn` dates or serial numbers so components are individual parts, not part types.
