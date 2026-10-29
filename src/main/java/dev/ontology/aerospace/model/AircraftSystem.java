package dev.ontology.aerospace.model;

import java.util.HashSet;
import java.util.Set;

import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;

/** Mapped to the "System" label; named AircraftSystem to avoid clashing with java.lang.System. */
@Node("System")
public class AircraftSystem {

    @Id
    private String name;

    @Relationship(type = "COMPOSED_OF", direction = Relationship.Direction.OUTGOING)
    private Set<Component> components = new HashSet<>();

    protected AircraftSystem() {
    }

    public AircraftSystem(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public Set<Component> getComponents() {
        return components;
    }
}
