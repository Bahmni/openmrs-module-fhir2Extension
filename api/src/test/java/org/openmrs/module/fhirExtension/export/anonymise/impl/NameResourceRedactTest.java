package org.openmrs.module.fhirExtension.export.anonymise.impl;

import org.hl7.fhir.r4.model.HumanName;
import org.hl7.fhir.r4.model.Patient;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class NameResourceRedactTest {
	
	@Test
	public void shouldRedactNameWhenCalledOnPatientResource() {
		Patient patient = mockPatientResource();
		boolean isNamePresentInitially = patient.hasName();
		NameResourceRedact.getInstance().redact(patient);
		assertFalse(patient.hasName());
		assertTrue(isNamePresentInitially);
		
	}
	
	private Patient mockPatientResource() {
        Patient patient = new Patient();
        List<HumanName> names = new ArrayList<>();
        names.add(new HumanName().addGiven("Dummy"));
        patient.setName(names);
        return patient;
    }
}
