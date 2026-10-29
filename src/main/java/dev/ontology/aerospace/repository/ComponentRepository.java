package dev.ontology.aerospace.repository;

import java.util.List;

import org.springframework.data.neo4j.repository.Neo4jRepository;

import dev.ontology.aerospace.model.Component;

/** Same idea as a JpaRepository: CRUD plus derived queries, generated for you. */
public interface ComponentRepository extends Neo4jRepository<Component, String> {

    List<Component> findByCriticality(String criticality);
}
