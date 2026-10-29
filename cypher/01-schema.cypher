// The ontology's "rules": each class (label) has a unique identifying property.
// In OWL/RDF terms this is closest to an inverse-functional / key property.
CREATE CONSTRAINT aircraft_tail   IF NOT EXISTS FOR (a:Aircraft)  REQUIRE a.tailNumber IS UNIQUE;
CREATE CONSTRAINT system_name     IF NOT EXISTS FOR (s:System)    REQUIRE s.name       IS UNIQUE;
CREATE CONSTRAINT component_part  IF NOT EXISTS FOR (c:Component) REQUIRE c.partNumber IS UNIQUE;
CREATE CONSTRAINT supplier_name   IF NOT EXISTS FOR (s:Supplier)  REQUIRE s.name       IS UNIQUE;
