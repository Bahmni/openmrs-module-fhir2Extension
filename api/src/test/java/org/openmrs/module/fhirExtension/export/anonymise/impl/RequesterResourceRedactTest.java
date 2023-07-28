package org.openmrs.module.fhirExtension.export.anonymise.impl;

import org.hl7.fhir.r4.model.MedicationRequest;
import org.hl7.fhir.r4.model.Reference;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RequesterResourceRedactTest {
    @Test
    public void shouldRedactRequesterWhenCalledOnMedicationRequestResource() {
        MedicationRequest medicationRequest = mockMedicationRequestResource();
        boolean isRequesterPresentInitially = medicationRequest.hasRequester();
        RequesterResourceRedact.getInstance().redact(medicationRequest);
        assertFalse(medicationRequest.hasRequester());
        assertTrue(isRequesterPresentInitially);

    }
    private MedicationRequest mockMedicationRequestResource() {
        MedicationRequest medicationRequest = new MedicationRequest();
        medicationRequest.setRequester(new Reference("DummyReference"));
        return medicationRequest;
    }

}