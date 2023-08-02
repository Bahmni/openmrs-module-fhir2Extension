package org.openmrs.module.fhirExtension.export.anonymise.impl;

import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Patient;
import org.openmrs.module.fhirExtension.export.anonymise.ResourceRedact;

public class AddressResourceRedact implements ResourceRedact {
	
	private AddressResourceRedact() {
		
	}
	
	@Override
	public void redact(IBaseResource iBaseResource) {
		Patient patient = (Patient) iBaseResource;
		patient.setAddress(null);
	}
	
	private static class SingletonHelper {
		private static final AddressResourceRedact INSTANCE = new AddressResourceRedact();
	}
	
	public static AddressResourceRedact getInstance() {
		return AddressResourceRedact.SingletonHelper.INSTANCE;
	}
}
