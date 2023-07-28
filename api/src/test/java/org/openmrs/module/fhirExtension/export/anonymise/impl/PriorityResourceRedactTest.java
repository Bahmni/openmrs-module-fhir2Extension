package org.openmrs.module.fhirExtension.export.anonymise.impl;

import org.hl7.fhir.r4.model.HumanName;
import org.hl7.fhir.r4.model.MedicationRequest;
import org.hl7.fhir.r4.model.Patient;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PriorityResourceRedactTest {

    @Test
    public void shouldRedactPriorityWhenCalledOnMedicationRequestResource() {
        MedicationRequest medicationRequest = mockMedicationRequestResource();
        boolean isPriorityPresentInitially = medicationRequest.hasPriority();
        PriorityResourceRedact.getInstance().redact(medicationRequest);
        assertFalse(medicationRequest.hasPriority());
        assertTrue(isPriorityPresentInitially);

    }
    private MedicationRequest mockMedicationRequestResource() {
        MedicationRequest medicationRequest = new MedicationRequest();
        medicationRequest.setPriority(MedicationRequest.MedicationRequestPriority.URGENT);
        return medicationRequest;
    }

}