package dev.ontology.aerospace.service;

import java.util.List;
import java.util.Optional;

import org.neo4j.driver.Value;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.stereotype.Service;

/**
 * Graph traversals that would be painful in SQL (recursive CTEs) are a few lines of Cypher.
 * Neo4jClient is the graph equivalent of JdbcTemplate: raw query in, mapped records out.
 */
@Service
public class ImpactAnalysisService {

    // Everything that transitively depends on the failed part breaks too; then walk up to systems and aircraft.
    private static final String COMPONENT_IMPACT = """
            MATCH (failed:Component {partNumber: $partNumber})
            OPTIONAL MATCH (d:Component)-[:DEPENDS_ON*1..]->(failed)
            WITH failed, [failed] + collect(DISTINCT d) AS broken
            UNWIND broken AS c
            OPTIONAL MATCH (a:Aircraft)-[:HAS_SYSTEM]->(s:System)-[:COMPOSED_OF]->(c)
            RETURN failed.name AS failed,
                   collect(DISTINCT c.name) AS brokenComponents,
                   collect(DISTINCT s.name) AS affectedSystems,
                   collect(DISTINCT a.tailNumber) AS affectedAircraft
            """;

    // Same traversal, but the starting set is every part a supplier provides.
    private static final String SUPPLIER_IMPACT = """
            MATCH (sup:Supplier {name: $supplier})<-[:SUPPLIED_BY]-(p:Component)
            OPTIONAL MATCH (d:Component)-[:DEPENDS_ON*1..]->(p)
            WITH sup, collect(DISTINCT p) + collect(DISTINCT d) AS broken
            UNWIND broken AS c
            WITH sup, collect(DISTINCT c) AS broken
            UNWIND broken AS c
            OPTIONAL MATCH (a:Aircraft)-[:HAS_SYSTEM]->(s:System)-[:COMPOSED_OF]->(c)
            RETURN sup.name AS failed,
                   collect(DISTINCT c.name) AS brokenComponents,
                   collect(DISTINCT s.name) AS affectedSystems,
                   collect(DISTINCT a.tailNumber) AS affectedAircraft
            """;

    private static final String DEPENDENCIES = """
            MATCH (:Component {partNumber: $partNumber})-[:DEPENDS_ON*1..]->(dep:Component)
            RETURN DISTINCT dep.name AS name
            ORDER BY name
            """;

    private final Neo4jClient neo4jClient;

    public ImpactAnalysisService(Neo4jClient neo4jClient) {
        this.neo4jClient = neo4jClient;
    }

    public Optional<ImpactReport> impactOfComponentFailure(String partNumber) {
        return neo4jClient.query(COMPONENT_IMPACT)
                .bind(partNumber).to("partNumber")
                .fetchAs(ImpactReport.class)
                .mappedBy((typeSystem, record) -> toReport(record))
                .one();
    }

    public Optional<ImpactReport> impactOfSupplierOutage(String supplier) {
        return neo4jClient.query(SUPPLIER_IMPACT)
                .bind(supplier).to("supplier")
                .fetchAs(ImpactReport.class)
                .mappedBy((typeSystem, record) -> toReport(record))
                .one();
    }

    public List<String> transitiveDependencies(String partNumber) {
        return List.copyOf(neo4jClient.query(DEPENDENCIES)
                .bind(partNumber).to("partNumber")
                .fetchAs(String.class)
                .mappedBy((typeSystem, record) -> record.get("name").asString())
                .all());
    }

    private static ImpactReport toReport(org.neo4j.driver.Record record) {
        return new ImpactReport(
                record.get("failed").asString(),
                record.get("brokenComponents").asList(Value::asString),
                record.get("affectedSystems").asList(Value::asString),
                record.get("affectedAircraft").asList(Value::asString));
    }
}
