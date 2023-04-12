package org.openmrs.module.fhirExtension.service;

import org.openmrs.Encounter;
import org.openmrs.Location;
import org.openmrs.module.fhir2.model.FhirDiagnosticReport;
import org.openmrs.parameter.EncounterSearchCriteria;

import java.util.Optional;

public interface EncounterMatcher {
	
	Encounter findOrCreateEncounter(FhirDiagnosticReport diagnosticReport);
	
	Optional<Encounter> findMatchingEncounter(EncounterSearchCriteria searchCriteria, Location userLocation);
}
