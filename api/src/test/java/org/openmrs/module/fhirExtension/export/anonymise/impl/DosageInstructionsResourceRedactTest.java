package org.openmrs.module.fhirExtension.export.anonymise.impl;

import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Dosage;
import org.hl7.fhir.r4.model.MedicationRequest;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
@RunWith(PowerMockRunner.class)
@PowerMockIgnore("javax.management.*")
class DosageInstructionsResourceRedactTest {

    @Test
    public void shouldRedactDosageInstructionsWhenCalledOnMedicationRequestResource() {
        MedicationRequest medicationRequest  = mockMedicationRequestResource();
        boolean isDosageInstructionsPresentInitially = medicationRequest.hasDosageInstruction();
        DosageInstructionsResourceRedact.getInstance().redact(medicationRequest);
        assertFalse(medicationRequest.hasDosageInstruction());
        assertTrue(isDosageInstructionsPresentInitially);
    }
    private MedicationRequest mockMedicationRequestResource() {
        MedicationRequest medicationRequest = new MedicationRequest();
        Dosage dosage = new Dosage();
        CodeableConcept codeableConcept = new CodeableConcept();
        codeableConcept.addCoding(new Coding("dummy", "dummy", "dummy"));
        dosage.addAdditionalInstruction(codeableConcept);
        List<Dosage> theDosageInstruction = Collections.singletonList(dosage);
        medicationRequest.setDosageInstruction(theDosageInstruction);
        return medicationRequest;
    }

}