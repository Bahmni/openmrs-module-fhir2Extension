package org.openmrs.module.fhirExtension.export.anonymise.impl;

import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Patient;
import org.openmrs.module.fhirExtension.export.anonymise.ResourceRedact;

public class NameResourceRedact implements ResourceRedact {
	
	private NameResourceRedact() {
		
	}
	
	public static NameResourceRedact getInstance() {
		return NameResourceRedact.SingletonHelper.INSTANCE;
	}
	
	@Override
	public void redact(IBaseResource iBaseResource) {
		Patient patient = (Patient) iBaseResource;
		patient.setName(null);
	}
	
	private static class SingletonHelper {
		
		private static final NameResourceRedact INSTANCE = new NameResourceRedact();
	}
}
