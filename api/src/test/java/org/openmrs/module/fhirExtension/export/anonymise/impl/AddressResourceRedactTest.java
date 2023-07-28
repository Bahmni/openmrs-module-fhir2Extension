package org.openmrs.module.fhirExtension.export.anonymise.impl;

import org.hl7.fhir.r4.model.Address;
import org.hl7.fhir.r4.model.Patient;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
@RunWith(PowerMockRunner.class)
@PowerMockIgnore("javax.management.*")
class AddressResourceRedactTest {

    @Test
    public void shouldRedactAddressResourceWhenCalledOnPatientResource() {
        Patient patient = mockPatientResource();
        boolean isAddressPresentInitially = patient.hasAddress();
        AddressResourceRedact.getInstance().redact(patient);
        assertFalse(patient.hasAddress());
        assertTrue(isAddressPresentInitially);

    }
    private Patient mockPatientResource() {
        Patient patient = new Patient();
        List<Address> addresses = Collections.singletonList(new Address().setCity("dummyCity"));
        patient.setAddress(addresses);
        return patient;
    }

}