package org.openmrs.module.fhirExtension.export.anonymise.impl;

import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Patient;
import org.openmrs.module.fhirExtension.export.anonymise.ResourceRedact;

public class BirthDateResourceRedact implements ResourceRedact {
	
	private BirthDateResourceRedact() {
		
	}
	
	@Override
	public void redact(IBaseResource iBaseResource) {
		Patient patient = (Patient) iBaseResource;
		patient.setBirthDate(null);
	}
	
	private static class SingletonHelper {
		
		private static final BirthDateResourceRedact INSTANCE = new BirthDateResourceRedact();
	}
	
	public static BirthDateResourceRedact getInstance() {
		return SingletonHelper.INSTANCE;
	}
}
