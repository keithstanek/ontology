package dev.ontology.aerospace.web;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import dev.ontology.aerospace.model.AircraftSystem;
import dev.ontology.aerospace.repository.AircraftRepository;
import dev.ontology.aerospace.repository.ComponentRepository;
import dev.ontology.aerospace.service.ImpactAnalysisService;
import dev.ontology.aerospace.service.ImpactReport;

@RestController
@RequestMapping("/api")
public class FleetController {

    private final ComponentRepository components;
    private final AircraftRepository aircraft;
    private final ImpactAnalysisService impact;

    public FleetController(ComponentRepository components, AircraftRepository aircraft, ImpactAnalysisService impact) {
        this.components = components;
        this.aircraft = aircraft;
        this.impact = impact;
    }

    @GetMapping("/components")
    public List<ComponentView> listComponents() {
        return components.findAll().stream()
                .map(ComponentView::of)
                .sorted(Comparator.comparing(ComponentView::partNumber))
                .toList();
    }

    @GetMapping("/components/{partNumber}")
    public ResponseEntity<ComponentView> getComponent(@PathVariable String partNumber) {
        return ResponseEntity.of(components.findById(partNumber).map(ComponentView::of));
    }

    @GetMapping("/components/{partNumber}/dependencies")
    public List<String> dependencies(@PathVariable String partNumber) {
        return impact.transitiveDependencies(partNumber);
    }

    @GetMapping("/components/{partNumber}/impact")
    public ResponseEntity<ImpactReport> componentImpact(@PathVariable String partNumber) {
        return ResponseEntity.of(impact.impactOfComponentFailure(partNumber));
    }

    @GetMapping("/suppliers/{name}/impact")
    public ResponseEntity<ImpactReport> supplierImpact(@PathVariable String name) {
        return ResponseEntity.of(impact.impactOfSupplierOutage(name));
    }

    @GetMapping("/aircraft")
    public List<Map<String, Object>> listAircraft() {
        return aircraft.findAll().stream()
                .map(a -> Map.<String, Object>of(
                        "tailNumber", a.getTailNumber(),
                        "model", a.getModel(),
                        "systems", a.getSystems().stream().map(AircraftSystem::getName).sorted().toList()))
                .toList();
    }
}
