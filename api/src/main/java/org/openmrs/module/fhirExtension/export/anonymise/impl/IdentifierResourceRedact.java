package org.openmrs.module.fhirExtension.export.anonymise.impl;

import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Patient;
import org.openmrs.module.fhirExtension.export.anonymise.ResourceRedact;

import java.util.Collections;
import java.util.List;

public class IdentifierResourceRedact implements ResourceRedact {
	
	private IdentifierResourceRedact() {
		
	}
	
	public static IdentifierResourceRedact getInstance() {
		return SingletonHelper.INSTANCE;
	}
	
	@Override
	public void redact(IBaseResource iBaseResource) {
		Patient patient = (Patient) iBaseResource;
		List<Identifier> identifiersList = patient.getIdentifier();
		if (!identifiersList.isEmpty()) {
			Identifier patientIdentifier = identifiersList.get(0);
			patient.setIdentifier(Collections.singletonList(patientIdentifier));
		}
	}
	
	private static class SingletonHelper {
		private static final IdentifierResourceRedact INSTANCE = new IdentifierResourceRedact();
	}
	
}
