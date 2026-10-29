package dev.ontology.aerospace.model;

import java.util.HashSet;
import java.util.Set;

import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;

@Node("Aircraft")
public class Aircraft {

    @Id
    private String tailNumber;
    private String model;

    @Relationship(type = "HAS_SYSTEM", direction = Relationship.Direction.OUTGOING)
    private Set<AircraftSystem> systems = new HashSet<>();

    protected Aircraft() {
    }

    public Aircraft(String tailNumber, String model) {
        this.tailNumber = tailNumber;
        this.model = model;
    }

    public String getTailNumber() {
        return tailNumber;
    }

    public String getModel() {
        return model;
    }

    public Set<AircraftSystem> getSystems() {
        return systems;
    }
}
