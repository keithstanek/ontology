package dev.ontology.aerospace.service;

import java.util.List;

/** What stops working when {@code failed} goes down. */
public record ImpactReport(
        String failed,
        List<String> brokenComponents,
        List<String> affectedSystems,
        List<String> affectedAircraft) {
}
