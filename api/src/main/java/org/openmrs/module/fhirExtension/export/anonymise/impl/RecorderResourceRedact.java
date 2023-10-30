package org.openmrs.module.fhirExtension.export.anonymise.impl;

import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Condition;
import org.openmrs.module.fhirExtension.export.anonymise.ResourceRedact;

public class RecorderResourceRedact implements ResourceRedact {
	
	private RecorderResourceRedact() {
		
	}
	
	public static RecorderResourceRedact getInstance() {
		return RecorderResourceRedact.SingletonHelper.INSTANCE;
	}
	
	@Override
	public void redact(IBaseResource iBaseResource) {
		Condition condition = (Condition) iBaseResource;
		condition.setRecorder(null);
	}
	
	private static class SingletonHelper {
		
		private static final RecorderResourceRedact INSTANCE = new RecorderResourceRedact();
	}
}
