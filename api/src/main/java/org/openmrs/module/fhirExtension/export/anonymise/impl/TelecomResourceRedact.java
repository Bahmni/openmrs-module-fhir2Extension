package org.openmrs.module.fhirExtension.export.anonymise.impl;

import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Patient;
import org.openmrs.module.fhirExtension.export.anonymise.ResourceRedact;

public class TelecomResourceRedact implements ResourceRedact {
	
	private TelecomResourceRedact() {
		
	}
	
	@Override
	public void redact(IBaseResource iBaseResource) {
		Patient patient = (Patient) iBaseResource;
		patient.setTelecom(null);
	}
	
	private static class SingletonHelper {
		private static final TelecomResourceRedact INSTANCE = new TelecomResourceRedact();
	}
	
	public static TelecomResourceRedact getInstance() {
		return TelecomResourceRedact.SingletonHelper.INSTANCE;
	}
}
