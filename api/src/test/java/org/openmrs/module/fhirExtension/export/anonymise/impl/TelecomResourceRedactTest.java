package org.openmrs.module.fhirExtension.export.anonymise.impl;

import org.hl7.fhir.r4.model.ContactPoint;
import org.hl7.fhir.r4.model.Patient;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TelecomResourceRedactTest {
	
	@Test
	public void shouldRedactTelecomWhenCalledOnPatientResource() {
		Patient patient = mockPatientResource();
		boolean isTelecomPresentInitially = patient.hasTelecom();
		TelecomResourceRedact.getInstance().redact(patient);
		assertFalse(patient.hasTelecom());
		assertTrue(isTelecomPresentInitially);
		
	}
	
	private Patient mockPatientResource() {
        Patient patient = new Patient();
        List<ContactPoint> contactPoints = new ArrayList<>();
        ContactPoint contactPoint = new ContactPoint();
        contactPoints.add(contactPoint.setValue("dummyValue"));
        patient.setTelecom(contactPoints);
        return patient;
    }
}
