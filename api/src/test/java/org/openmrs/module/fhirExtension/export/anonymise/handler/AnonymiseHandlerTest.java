package org.openmrs.module.fhirExtension.export.anonymise.handler;

import org.hl7.fhir.r4.model.Address;
import org.hl7.fhir.r4.model.HumanName;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Patient;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.openmrs.api.AdministrationService;
import org.openmrs.module.fhirExtension.export.anonymise.config.AnonymiserConfig;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@RunWith(PowerMockRunner.class)
@PowerMockIgnore("javax.management.*")
public class AnonymiseHandlerTest {
    @Mock
    private AdministrationService adminService;


    @InjectMocks
    AnonymiseHandler anonymiseHandler;


    @Test
    public void shouldDoNothing_whenLoadingConfigForNonAnonymisedData() {
        anonymiseHandler.loadAnonymiserConfig(false);
        verify(adminService, times(0)).getGlobalProperty(any());
    }
    @Test
    public void shouldLoadConfig_whenLoadingConfigForAnonymisedDataAndValidConfigPathProvided() {
        when(adminService.getGlobalProperty(any())).thenReturn("src/test/resources/FHIR Export/config/anonymouse-fhir.json");
        anonymiseHandler.loadAnonymiserConfig(true);
        verify(adminService, times(1)).getGlobalProperty(any());
    }

    @Test
    public void shouldAnonymiseInputResource_WhenInputResourceAndResourceTypePassed() {
        when(adminService.getGlobalProperty(any())).thenReturn("src/test/resources/FHIR Export/config/anonymouse-fhir.json");
        anonymiseHandler.loadAnonymiserConfig(true);
        Patient patient = mockPatientResource();
        int initialIdentifierSize = patient.getIdentifier().size();
        boolean isAddressPresentInitially = patient.hasAddress();
        boolean isNamePresentInitially = patient.hasName();
        anonymiseHandler.anonymise(patient, "patient");
        assertEquals(2, initialIdentifierSize);
        assertEquals(1, patient.getIdentifier().size());
        assertFalse(patient.hasAddress());
        assertTrue(isAddressPresentInitially);
        assertFalse(patient.hasName());
        assertTrue(isNamePresentInitially);
    }
    @Test
    public void shouldThrowException_WhenInvalidConfigFilePathProvided() {
        when(adminService.getGlobalProperty(any())).thenReturn("dummyPath");
        assertThrows(RuntimeException.class, ()-> anonymiseHandler.loadAnonymiserConfig(true));
    }
    private Patient mockPatientResource() {
        Patient patient = new Patient();
        List<Identifier> identifiers = new ArrayList<>();
        identifiers.add(new Identifier());
        identifiers.add(new Identifier());
        patient.setIdentifier(identifiers);
        List<HumanName> names = new ArrayList<>();
        names.add(new HumanName().addGiven("Dummy"));
        patient.setName(names);
        List<Address> addresses = Collections.singletonList(new Address().setCity("dummyCity"));
        patient.setAddress(addresses);
        return patient;
    }
}