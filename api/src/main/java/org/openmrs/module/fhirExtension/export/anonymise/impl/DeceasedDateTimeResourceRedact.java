package org.openmrs.module.fhirExtension.export.anonymise.impl;

import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Patient;
import org.openmrs.module.fhirExtension.export.anonymise.ResourceRedact;

public class DeceasedDateTimeResourceRedact implements ResourceRedact {
    private DeceasedDateTimeResourceRedact() {

    }

    @Override
    public void redact(IBaseResource iBaseResource) {
        Patient patient = (Patient) iBaseResource;
        patient.setDeceased(null);
    }

    private static class SingletonHelper {

        private static final DeceasedDateTimeResourceRedact INSTANCE = new DeceasedDateTimeResourceRedact();
    }

    public static DeceasedDateTimeResourceRedact getInstance() {
        return DeceasedDateTimeResourceRedact.SingletonHelper.INSTANCE;
    }
}
