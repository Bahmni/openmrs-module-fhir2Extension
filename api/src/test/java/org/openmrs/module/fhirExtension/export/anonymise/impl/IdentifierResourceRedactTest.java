package org.openmrs.module.fhirExtension.export.anonymise.impl;

import org.hl7.fhir.r4.model.Address;
import org.hl7.fhir.r4.model.Identifier;
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
class IdentifierResourceRedactTest {

    @Test
    public void shouldRedactSecondaryIdentifierAndRetainFirstPatientIdentifierWhenCalledOnPatientResource() {
        Patient patient = mockPatientResource();
        int initialIdentifierSize = patient.getIdentifier().size();
        IdentifierResourceRedact.getInstance().redact(patient);
        assertEquals(2, initialIdentifierSize);
        assertEquals(1, patient.getIdentifier().size());

    }
    private Patient mockPatientResource() {
        Patient patient = new Patient();
        List<Identifier> identifiers = new ArrayList<>();
        identifiers.add(new Identifier());
        identifiers.add(new Identifier());
        patient.setIdentifier(identifiers);
        return patient;
    }

}