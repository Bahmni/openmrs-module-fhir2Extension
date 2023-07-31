package org.openmrs.module.fhirExtension.export.anonymise.impl;

import org.hl7.fhir.r4.model.Condition;
import org.hl7.fhir.r4.model.Reference;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RecorderResourceRedactTest {
	
	@Test
	public void shouldRedactRecorderWhenCalledOnMedicationRequestResource() {
		Condition condition = mockConditionResource();
		boolean isRecorderPresentInitially = condition.hasRecorder();
		RecorderResourceRedact.getInstance().redact(condition);
		assertFalse(condition.hasRecorder());
		assertTrue(isRecorderPresentInitially);
		
	}
	
	private Condition mockConditionResource() {
		Condition condition = new Condition();
		condition.setRecorder(new Reference("DummyReference"));
		return condition;
	}
	
}
