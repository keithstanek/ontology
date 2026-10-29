// A small, fictional fleet. Safe to re-run: everything uses MERGE.
//
// Ontology (classes and relationships):
//   (:Aircraft)-[:HAS_SYSTEM]->(:System)-[:COMPOSED_OF]->(:Component)
//   (:Component)-[:DEPENDS_ON]->(:Component)      "A needs B to work"
//   (:Component)-[:SUPPLIED_BY]->(:Supplier)

// ---- Components -------------------------------------------------------------
UNWIND [
  {pn: "GEN-1",  name: "Main Generator",              crit: "HIGH"},
  {pn: "BAT-1",  name: "Backup Battery",              crit: "MEDIUM"},
  {pn: "BUS-1",  name: "Primary Power Bus",           crit: "HIGH"},
  {pn: "PUMP-1", name: "Hydraulic Pump",              crit: "HIGH"},
  {pn: "IMU-1",  name: "Inertial Measurement Unit",   crit: "HIGH"},
  {pn: "FCC-1",  name: "Flight Control Computer",     crit: "HIGH"},
  {pn: "ACT-1",  name: "Elevator Actuator",           crit: "HIGH"},
  {pn: "ACT-2",  name: "Aileron Actuator",            crit: "HIGH"},
  {pn: "MC-1",   name: "Mission Computer",            crit: "MEDIUM"},
  {pn: "DISP-1", name: "Cockpit Display",             crit: "MEDIUM"},
  {pn: "COOL-1", name: "Liquid Cooling Loop",         crit: "LOW"},
  {pn: "RDR-1",  name: "Radar Array",                 crit: "MEDIUM"},
  {pn: "RAMP-1", name: "Cargo Ramp Actuator",         crit: "LOW"},
  {pn: "CLS-1",  name: "Cargo Loading System",        crit: "LOW"}
] AS row
MERGE (c:Component {partNumber: row.pn})
SET c.name = row.name, c.criticality = row.crit;

// ---- Dependencies (A)-[:DEPENDS_ON]->(B) --------------------------------------
UNWIND [
  ["BUS-1","GEN-1"],
  ["PUMP-1","BUS-1"],
  ["IMU-1","BUS-1"],
  ["FCC-1","BUS-1"], ["FCC-1","IMU-1"],
  ["ACT-1","PUMP-1"], ["ACT-1","FCC-1"],
  ["ACT-2","PUMP-1"], ["ACT-2","FCC-1"],
  ["MC-1","BUS-1"],  ["MC-1","IMU-1"],
  ["DISP-1","BUS-1"], ["DISP-1","MC-1"],
  ["COOL-1","BUS-1"],
  ["RDR-1","MC-1"],  ["RDR-1","COOL-1"],
  ["RAMP-1","PUMP-1"],
  ["CLS-1","RAMP-1"], ["CLS-1","BUS-1"]
] AS pair
MATCH (a:Component {partNumber: pair[0]}), (b:Component {partNumber: pair[1]})
MERGE (a)-[:DEPENDS_ON]->(b);

// ---- Systems ----------------------------------------------------------------
UNWIND [
  {sys: "Electrical Power", parts: ["GEN-1","BAT-1","BUS-1"]},
  {sys: "Hydraulic Power",  parts: ["PUMP-1"]},
  {sys: "Flight Control",   parts: ["FCC-1","IMU-1","ACT-1","ACT-2"]},
  {sys: "Avionics",         parts: ["MC-1","DISP-1"]},
  {sys: "Radar",            parts: ["RDR-1","COOL-1"]},
  {sys: "Cargo Handling",   parts: ["RAMP-1","CLS-1"]}
] AS row
MERGE (s:System {name: row.sys})
WITH s, row
UNWIND row.parts AS pn
MATCH (c:Component {partNumber: pn})
MERGE (s)-[:COMPOSED_OF]->(c);

// ---- Aircraft ---------------------------------------------------------------
UNWIND [
  {tail: "AX-101", model: "AX Falcon", systems: ["Electrical Power","Hydraulic Power","Flight Control","Avionics","Radar"]},
  {tail: "AX-102", model: "AX Falcon", systems: ["Electrical Power","Hydraulic Power","Flight Control","Avionics","Radar"]},
  {tail: "CX-201", model: "CX Hauler", systems: ["Electrical Power","Hydraulic Power","Flight Control","Avionics","Cargo Handling"]}
] AS row
MERGE (a:Aircraft {tailNumber: row.tail})
SET a.model = row.model
WITH a, row
UNWIND row.systems AS sysName
MATCH (s:System {name: sysName})
MERGE (a)-[:HAS_SYSTEM]->(s);

// ---- Suppliers --------------------------------------------------------------
UNWIND [
  {name: "Aerotron Power",  parts: ["GEN-1","BAT-1","BUS-1"]},
  {name: "HydraFlow",       parts: ["PUMP-1","ACT-1","ACT-2","RAMP-1"]},
  {name: "NavCore",         parts: ["IMU-1","FCC-1","MC-1","DISP-1"]},
  {name: "SkyWave Sensors", parts: ["RDR-1","COOL-1","IMU-1"]},
  {name: "CargoTech",       parts: ["CLS-1"]}
] AS row
MERGE (s:Supplier {name: row.name})
WITH s, row
UNWIND row.parts AS pn
MATCH (c:Component {partNumber: pn})
MERGE (c)-[:SUPPLIED_BY]->(s);
