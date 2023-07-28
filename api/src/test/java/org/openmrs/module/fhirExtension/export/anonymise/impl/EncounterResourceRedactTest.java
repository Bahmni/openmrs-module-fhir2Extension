package org.openmrs.module.fhirExtension.export.anonymise.impl;

import org.hl7.fhir.r4.model.Condition;
import org.hl7.fhir.r4.model.MedicationRequest;
import org.hl7.fhir.r4.model.Procedure;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.ServiceRequest;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@RunWith(PowerMockRunner.class)
@PowerMockIgnore("javax.management.*")
class EncounterResourceRedactTest {
    private static final String MOCK_REFERENCE_ID = "dummyReference";

    @Test
    public void shouldRedactEncounterWhenCalledOnMedicationRequestResource() {
        MedicationRequest medicationRequest = mockMedicationRequestResource();
        boolean isEncounterPresentInitially = medicationRequest.hasEncounter();
        EncounterResourceRedact.getInstance().redact(medicationRequest);
        assertFalse(medicationRequest.hasEncounter());
        assertTrue(isEncounterPresentInitially);
    }

    @Test
    public void shouldRedactEncounterWhenCalledOnConditionResource() {
        Condition condition = mockConditionResource();
        boolean isEncounterPresentInitially = condition.hasEncounter();
        EncounterResourceRedact.getInstance().redact(condition);
        assertFalse(condition.hasEncounter());
        assertTrue(isEncounterPresentInitially);
    }

    @Test
    public void shouldRedactEncounterWhenCalledOnServiceRequestResource() {
        ServiceRequest serviceRequest = mockServiceRequestResource();
        boolean isEncounterPresentInitially = serviceRequest.hasEncounter();
        EncounterResourceRedact.getInstance().redact(serviceRequest);
        assertFalse(serviceRequest.hasEncounter());
        assertTrue(isEncounterPresentInitially);
    }

    @Test
    public void shouldRedactEncounterWhenCalledOnProcedureResource() {
        Procedure procedure = mockProcedureResource();
        boolean isEncounterPresentInitially = procedure.hasEncounter();
        EncounterResourceRedact.getInstance().redact(procedure);
        assertFalse(procedure.hasEncounter());
        assertTrue(isEncounterPresentInitially);
    }

    private MedicationRequest mockMedicationRequestResource() {
        MedicationRequest medicationRequest = new MedicationRequest();
        medicationRequest.setEncounter(new Reference(MOCK_REFERENCE_ID));
        return medicationRequest;
    }

    private Condition mockConditionResource() {
        Condition condition = new Condition();
        condition.setEncounter(new Reference(MOCK_REFERENCE_ID));
        return condition;
    }

    private ServiceRequest mockServiceRequestResource() {
        ServiceRequest serviceRequest = new ServiceRequest();
        serviceRequest.setEncounter(new Reference(MOCK_REFERENCE_ID));
        return serviceRequest;
    }

    private Procedure mockProcedureResource() {
        Procedure procedure = new Procedure();
        procedure.setEncounter(new Reference(MOCK_REFERENCE_ID));
        return procedure;
    }

}