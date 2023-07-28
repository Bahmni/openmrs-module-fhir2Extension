package org.openmrs.module.fhirExtension.export.anonymise.impl;

import org.hl7.fhir.r4.model.Address;
import org.hl7.fhir.r4.model.HumanName;
import org.hl7.fhir.r4.model.Patient;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
@RunWith(PowerMockRunner.class)
@PowerMockIgnore("javax.management.*")
class NameResourceRedactTest {

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