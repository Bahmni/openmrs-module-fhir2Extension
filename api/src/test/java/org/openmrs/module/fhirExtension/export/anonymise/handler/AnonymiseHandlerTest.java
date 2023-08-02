package org.openmrs.module.fhirExtension.export.anonymise.handler;

import org.hl7.fhir.r4.model.Address;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Condition;
import org.hl7.fhir.r4.model.ContactPoint;
import org.hl7.fhir.r4.model.DateTimeType;
import org.hl7.fhir.r4.model.DateType;
import org.hl7.fhir.r4.model.Dosage;
import org.hl7.fhir.r4.model.HumanName;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.MedicationRequest;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.ServiceRequest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.openmrs.api.AdministrationService;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PowerMockIgnore("javax.management.*")
public class AnonymiseHandlerTest {
	
	private static final String MOCK_REFERENCE_ID = "dummyReference";
	
	@InjectMocks
	AnonymiseHandler anonymiseHandler;
	
	@Mock
	private AdministrationService adminService;
	
	@Test
	public void shouldDoNothing_whenLoadingConfigForNonAnonymisedData() {
		anonymiseHandler.loadAnonymiserConfig(false);
		verify(adminService, times(0)).getGlobalProperty(any());
	}
	
	@Test
	public void shouldLoadConfig_whenLoadingConfigForAnonymisedDataAndValidConfigPathProvided() {
		when(adminService.getGlobalProperty(any())).thenReturn("src/test/resources/FHIR Export/config/anonymise-fhir.json");
		anonymiseHandler.loadAnonymiserConfig(true);
		verify(adminService, times(1)).getGlobalProperty(any());
	}
	
	@Test
	public void shouldAnonymisePatientResource_WhenPatientResourceAndPatientResourceTypePassed() {
		when(adminService.getGlobalProperty(any())).thenReturn("src/test/resources/FHIR Export/config/anonymise-fhir.json");
		anonymiseHandler.loadAnonymiserConfig(true);
		Patient patient = mockPatientResource();
		int initialIdentifierSize = patient.getIdentifier().size();
		boolean isAddressPresentInitially = patient.hasAddress();
		boolean isNamePresentInitially = patient.hasName();
		boolean isTelecomPresentInitially = patient.hasTelecom();
		
		anonymiseHandler.anonymise(patient, "patient");
		
		assertEquals(2, initialIdentifierSize);
		assertEquals(1, patient.getIdentifier().size());
		
		assertFalse(patient.hasAddress());
		assertTrue(isAddressPresentInitially);
		
		assertFalse(patient.hasName());
		assertTrue(isNamePresentInitially);
		
		assertFalse(patient.hasTelecom());
		assertTrue(isTelecomPresentInitially);
		
		assertEquals(1, patient.getBirthDateElement().getDay());
		assertEquals(Calendar.JANUARY, patient.getBirthDateElement().getMonth());
		
		assertEquals(1, patient.getDeceasedDateTimeType().getDay());
		assertEquals(Calendar.JANUARY, patient.getDeceasedDateTimeType().getMonth());
	}
	
	@Test
	public void shouldAnonymiseConditionResource_WhenConditionResourceAndConditionResourceTypePassed() {
		when(adminService.getGlobalProperty(any())).thenReturn("src/test/resources/FHIR Export/config/anonymise-fhir.json");
		anonymiseHandler.loadAnonymiserConfig(true);
		
		Condition condition = mockConditionResource();
		boolean isEncounterPresentInitially = condition.hasEncounter();
		boolean isRecorderPresentInitially = condition.hasRecorder();
		boolean isRecordedDatePresentInitially = condition.hasRecordedDate();
		
		anonymiseHandler.anonymise(condition, "condition");
		
		assertFalse(condition.hasRecordedDate());
		assertTrue(isRecordedDatePresentInitially);
		
		assertFalse(condition.hasRecorder());
		assertTrue(isRecorderPresentInitially);
		
		assertFalse(condition.hasEncounter());
		assertTrue(isEncounterPresentInitially);
	}
	
	@Test
	public void shouldAnonymiseMedicationRequestResource_WhenMedicationRequestResourceAndMedicationRequestResourceTypePassed() {
		when(adminService.getGlobalProperty(any())).thenReturn("src/test/resources/FHIR Export/config/anonymise-fhir.json");
		anonymiseHandler.loadAnonymiserConfig(true);
		
		MedicationRequest medicationRequest = mockMedicationRequestResource();
		boolean isEncounterPresentInitially = medicationRequest.hasEncounter();
		boolean isAuthoredOnPresentInitially = medicationRequest.hasAuthoredOn();
		boolean isDosageInstructionsPresentInitially = medicationRequest.hasDosageInstruction();
		boolean isPriorityPresentInitially = medicationRequest.hasPriority();
		boolean isRequesterPresentInitially = medicationRequest.hasRequester();
		
		anonymiseHandler.anonymise(medicationRequest, "medicationRequest");
		assertFalse(medicationRequest.hasRequester());
		assertTrue(isRequesterPresentInitially);
		
		assertFalse(medicationRequest.hasPriority());
		assertTrue(isPriorityPresentInitially);
		
		assertFalse(medicationRequest.hasDosageInstruction());
		assertTrue(isDosageInstructionsPresentInitially);
		
		assertFalse(medicationRequest.hasAuthoredOn());
		assertTrue(isAuthoredOnPresentInitially);
		
		assertFalse(medicationRequest.hasEncounter());
		assertTrue(isEncounterPresentInitially);
		
	}
	
	@Test
	public void shouldAnonymiseServiceRequestResource_WhenServiceRequestResourceAndServiceRequestResourceTypePassed() {
		when(adminService.getGlobalProperty(any())).thenReturn("src/test/resources/FHIR Export/config/anonymise-fhir.json");
		anonymiseHandler.loadAnonymiserConfig(true);
		
		ServiceRequest serviceRequest = mockServiceRequestResource();
		boolean isEncounterPresentInitially = serviceRequest.hasEncounter();
		
		anonymiseHandler.anonymise(serviceRequest, "serviceRequest");
		
		assertFalse(serviceRequest.hasEncounter());
		assertTrue(isEncounterPresentInitially);
		
	}
	
	@Test
    public void shouldThrowException_WhenInvalidConfigFilePathProvided() {
        when(adminService.getGlobalProperty(any())).thenReturn("dummyPath");
        assertThrows(RuntimeException.class, () -> anonymiseHandler.loadAnonymiserConfig(true));
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
        List<ContactPoint> contactPoints = new ArrayList<>();
        ContactPoint contactPoint = new ContactPoint();
        contactPoints.add(contactPoint.setValue("dummyValue"));
        patient.setTelecom(contactPoints);

		DateType birthDateElement = new DateType("2000-05-03");
		patient.setBirthDateElement(birthDateElement);
		patient.setDeceased(new DateTimeType("2023-07-31T00:00:00.000+00:00"));

        return patient;
    }
	
	private MedicationRequest mockMedicationRequestResource() {
		MedicationRequest medicationRequest = new MedicationRequest();
		medicationRequest.setEncounter(new Reference(MOCK_REFERENCE_ID));
		medicationRequest.setAuthoredOn(new Date());
		Dosage dosage = new Dosage();
		CodeableConcept codeableConcept = new CodeableConcept();
		codeableConcept.addCoding(new Coding("dummy", "dummy", "dummy"));
		dosage.addAdditionalInstruction(codeableConcept);
		List<Dosage> theDosageInstruction = Collections.singletonList(dosage);
		medicationRequest.setDosageInstruction(theDosageInstruction);
		medicationRequest.setEncounter(new Reference(MOCK_REFERENCE_ID));
		medicationRequest.setPriority(MedicationRequest.MedicationRequestPriority.URGENT);
		medicationRequest.setRequester(new Reference(MOCK_REFERENCE_ID));
		return medicationRequest;
	}
	
	private Condition mockConditionResource() {
		Condition condition = new Condition();
		condition.setEncounter(new Reference(MOCK_REFERENCE_ID));
		condition.setRecorder(new Reference(MOCK_REFERENCE_ID));
		condition.setRecordedDate(new Date());
		return condition;
	}
	
	private ServiceRequest mockServiceRequestResource() {
		ServiceRequest serviceRequest = new ServiceRequest();
		serviceRequest.setEncounter(new Reference(MOCK_REFERENCE_ID));
		return serviceRequest;
	}
}