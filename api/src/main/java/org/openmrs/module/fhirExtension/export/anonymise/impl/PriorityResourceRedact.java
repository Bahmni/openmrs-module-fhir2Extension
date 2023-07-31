package org.openmrs.module.fhirExtension.export.anonymise.impl;

import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.MedicationRequest;
import org.openmrs.module.fhirExtension.export.anonymise.ResourceRedact;

public class PriorityResourceRedact implements ResourceRedact {
	
	private PriorityResourceRedact() {
		
	}
	
	public static PriorityResourceRedact getInstance() {
		return PriorityResourceRedact.SingletonHelper.INSTANCE;
	}
	
	@Override
	public void redact(IBaseResource iBaseResource) {
		MedicationRequest medicationRequest = (MedicationRequest) iBaseResource;
		medicationRequest.setPriority(null);
	}
	
	private static class SingletonHelper {
		private static final PriorityResourceRedact INSTANCE = new PriorityResourceRedact();
	}
}
