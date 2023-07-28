package org.openmrs.module.fhirExtension.export.anonymise.impl;

import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.MedicationRequest;
import org.openmrs.module.fhirExtension.export.anonymise.ResourceRedact;

public class DosageInstructionsResourceRedact implements ResourceRedact {
    private DosageInstructionsResourceRedact() {

    }

    public static DosageInstructionsResourceRedact getInstance() {
        return DosageInstructionsResourceRedact.SingletonHelper.INSTANCE;
    }

    @Override
    public void redact(IBaseResource iBaseResource) {
        MedicationRequest medicationRequest = (MedicationRequest) iBaseResource;
        medicationRequest.setDosageInstruction(null);
    }

    private static class SingletonHelper {
        private static final DosageInstructionsResourceRedact INSTANCE = new DosageInstructionsResourceRedact();
    }
}
