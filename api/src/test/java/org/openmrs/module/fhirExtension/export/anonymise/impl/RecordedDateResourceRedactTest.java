package org.openmrs.module.fhirExtension.export.anonymise.impl;

import org.hl7.fhir.r4.model.Condition;
import org.hl7.fhir.r4.model.MedicationRequest;
import org.hl7.fhir.r4.model.Reference;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class RecordedDateResourceRedactTest {
    @Test
    public void shouldRedactRecordedDateWhenCalledOnMedicationRequestResource() {
        Condition condition = mockConditionResource();
        boolean isRecordedDatePresentInitially = condition.hasRecordedDate();
        RecordedDateResourceRedact.getInstance().redact(condition);
        assertFalse(condition.hasRecordedDate());
        assertTrue(isRecordedDatePresentInitially);

    }
    private Condition mockConditionResource() {
        Condition condition = new Condition();
        condition.setRecordedDate(new Date());
        return condition;
    }
}