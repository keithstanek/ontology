package dev.ontology.aerospace.repository;

import org.springframework.data.neo4j.repository.Neo4jRepository;

import dev.ontology.aerospace.model.Aircraft;

public interface AircraftRepository extends Neo4jRepository<Aircraft, String> {
}
