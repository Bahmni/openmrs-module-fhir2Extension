package org.openmrs.module.fhirExtension.export.anonymise.impl;

import org.hl7.fhir.r4.model.MedicationRequest;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
@RunWith(PowerMockRunner.class)
@PowerMockIgnore("javax.management.*")
class AuthoredOnResourceRedactTest {


    @Test
    public void shouldRedactAuthoredOnWhenCalledOnMedicationRequestResource() {
        MedicationRequest medicationRequest  = mockMedicationRequestResource();
        boolean isAuthoredOnPresentInitially = medicationRequest.hasAuthoredOn();
        AuthoredOnResourceRedact.getInstance().redact(medicationRequest);
        assertFalse(medicationRequest.hasAuthoredOn());
        assertTrue(isAuthoredOnPresentInitially);

    }
    private MedicationRequest mockMedicationRequestResource() {
        MedicationRequest medicationRequest = new MedicationRequest();
        medicationRequest.setAuthoredOn(new Date());
        return medicationRequest;
    }

}