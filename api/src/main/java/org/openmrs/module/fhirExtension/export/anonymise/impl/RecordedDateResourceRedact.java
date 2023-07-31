package org.openmrs.module.fhirExtension.export.anonymise.impl;

import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Condition;
import org.openmrs.module.fhirExtension.export.anonymise.ResourceRedact;

public class RecordedDateResourceRedact implements ResourceRedact {
	
	private RecordedDateResourceRedact() {
		
	}
	
	public static RecordedDateResourceRedact getInstance() {
		return RecordedDateResourceRedact.SingletonHelper.INSTANCE;
	}
	
	@Override
	public void redact(IBaseResource iBaseResource) {
		Condition condition = (Condition) iBaseResource;
		condition.setRecordedDate(null);
	}
	
	private static class SingletonHelper {
		private static final RecordedDateResourceRedact INSTANCE = new RecordedDateResourceRedact();
	}
}
