package org.openmrs.module.fhirExtension.export.anonymise.impl;

import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.MedicationRequest;
import org.openmrs.module.fhirExtension.export.anonymise.ResourceRedact;

public class AuthoredOnResourceRedact implements ResourceRedact {
	
	private AuthoredOnResourceRedact() {
		
	}
	
	public static AuthoredOnResourceRedact getInstance() {
		return AuthoredOnResourceRedact.SingletonHelper.INSTANCE;
	}
	
	@Override
	public void redact(IBaseResource iBaseResource) {
		MedicationRequest medicationRequest = (MedicationRequest) iBaseResource;
		medicationRequest.setAuthoredOn(null);
	}
	
	private static class SingletonHelper {
		
		private static final AuthoredOnResourceRedact INSTANCE = new AuthoredOnResourceRedact();
	}
}
