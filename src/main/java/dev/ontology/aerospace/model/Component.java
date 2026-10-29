package dev.ontology.aerospace.model;

import java.util.HashSet;
import java.util.Set;

import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;

/** A physical part. Like JPA's @Entity, but a node in the graph instead of a row. */
@Node("Component")
public class Component {

    /** Natural key, so no @GeneratedValue: the part number is the identity. */
    @Id
    private String partNumber;
    private String name;
    private String criticality;

    /** Outgoing (this)-[:DEPENDS_ON]->(other). Plays the role of a JPA @ManyToMany, with no join table. */
    @Relationship(type = "DEPENDS_ON", direction = Relationship.Direction.OUTGOING)
    private Set<Component> dependsOn = new HashSet<>();

    @Relationship(type = "SUPPLIED_BY", direction = Relationship.Direction.OUTGOING)
    private Set<Supplier> suppliers = new HashSet<>();

    protected Component() {
    }

    public Component(String partNumber, String name, String criticality) {
        this.partNumber = partNumber;
        this.name = name;
        this.criticality = criticality;
    }

    public String getPartNumber() {
        return partNumber;
    }

    public String getName() {
        return name;
    }

    public String getCriticality() {
        return criticality;
    }

    public Set<Component> getDependsOn() {
        return dependsOn;
    }

    public Set<Supplier> getSuppliers() {
        return suppliers;
    }
}
