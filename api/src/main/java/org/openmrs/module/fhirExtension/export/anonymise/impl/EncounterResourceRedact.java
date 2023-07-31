package org.openmrs.module.fhirExtension.export.anonymise.impl;

import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Condition;
import org.hl7.fhir.r4.model.MedicationRequest;
import org.hl7.fhir.r4.model.Procedure;
import org.hl7.fhir.r4.model.ServiceRequest;
import org.openmrs.module.fhirExtension.export.anonymise.ResourceRedact;

public class EncounterResourceRedact implements ResourceRedact {
	
	private EncounterResourceRedact() {
		
	}
	
	public static EncounterResourceRedact getInstance() {
		return EncounterResourceRedact.SingletonHelper.INSTANCE;
	}
	
	@Override
	public void redact(IBaseResource iBaseResource) {
		if (iBaseResource instanceof Condition) {
			Condition condition = (Condition) iBaseResource;
			condition.setEncounter(null);
			return;
		}
		if (iBaseResource instanceof MedicationRequest) {
			MedicationRequest medicationRequest = (MedicationRequest) iBaseResource;
			medicationRequest.setEncounter(null);
			return;
		}
		if (iBaseResource instanceof ServiceRequest) {
			ServiceRequest serviceRequest = (ServiceRequest) iBaseResource;
			serviceRequest.setEncounter(null);
			return;
		}
		if (iBaseResource instanceof Procedure) {
			Procedure procedure = (Procedure) iBaseResource;
			procedure.setEncounter(null);
		}
	}
	
	private static class SingletonHelper {
		private static final EncounterResourceRedact INSTANCE = new EncounterResourceRedact();
	}
}
