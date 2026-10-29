package dev.ontology.aerospace.web;

import java.util.List;

import dev.ontology.aerospace.model.Component;
import dev.ontology.aerospace.model.Supplier;

/** Flat JSON shape, so serializing a Component doesn't recurse through the whole dependency graph. */
public record ComponentView(
        String partNumber,
        String name,
        String criticality,
        List<String> dependsOn,
        List<String> suppliers) {

    static ComponentView of(Component c) {
        return new ComponentView(
                c.getPartNumber(),
                c.getName(),
                c.getCriticality(),
                c.getDependsOn().stream().map(Component::getPartNumber).sorted().toList(),
                c.getSuppliers().stream().map(Supplier::getName).sorted().toList());
    }
}
