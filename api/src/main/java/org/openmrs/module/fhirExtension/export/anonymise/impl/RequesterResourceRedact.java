package org.openmrs.module.fhirExtension.export.anonymise.impl;

import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.MedicationRequest;
import org.openmrs.module.fhirExtension.export.anonymise.ResourceRedact;

public class RequesterResourceRedact implements ResourceRedact {
	
	private RequesterResourceRedact() {
		
	}
	
	public static RequesterResourceRedact getInstance() {
		return RequesterResourceRedact.SingletonHelper.INSTANCE;
	}
	
	@Override
	public void redact(IBaseResource iBaseResource) {
		MedicationRequest medicationRequest = (MedicationRequest) iBaseResource;
		medicationRequest.setRequester(null);
	}
	
	private static class SingletonHelper {
		
		private static final RequesterResourceRedact INSTANCE = new RequesterResourceRedact();
	}
}
