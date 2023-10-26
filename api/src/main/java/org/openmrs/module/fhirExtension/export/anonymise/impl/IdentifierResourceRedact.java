package org.openmrs.module.fhirExtension.export.anonymise.impl;

import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Patient;
import org.openmrs.module.fhirExtension.export.anonymise.ResourceRedact;

import java.util.Collections;

public class IdentifierResourceRedact implements ResourceRedact {
	
	private IdentifierResourceRedact() {
		
	}
	
	public static IdentifierResourceRedact getInstance() {
		return SingletonHelper.INSTANCE;
	}
	
	@Override
	public void redact(IBaseResource iBaseResource) {
		Patient patient = (Patient) iBaseResource;
		patient.setIdentifier(Collections.emptyList());
	}
	
	private static class SingletonHelper {
		
		private static final IdentifierResourceRedact INSTANCE = new IdentifierResourceRedact();
	}
	
}
