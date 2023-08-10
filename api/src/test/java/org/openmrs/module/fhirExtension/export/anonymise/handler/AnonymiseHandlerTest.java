package org.openmrs.module.fhirExtension.export.anonymise.handler;

import org.hl7.fhir.r4.model.Address;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Condition;
import org.hl7.fhir.r4.model.ContactPoint;
import org.hl7.fhir.r4.model.DateTimeType;
import org.hl7.fhir.r4.model.DateType;
import org.hl7.fhir.r4.model.Dosage;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.HumanName;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.MedicationRequest;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.ServiceRequest;
import org.hl7.fhir.r4.model.StringType;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.openmrs.api.AdministrationService;
import org.openmrs.module.fhirExtension.export.anonymise.impl.CorrelationCache;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
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
	
	@Mock
	private CorrelationCache correlationCache;
	
	@Test
	public void shouldNotLoadAnonymizeConfig_whenAnonymizeIsDisabled() {
		anonymiseHandler.loadAnonymiserConfig(false);
		verify(adminService, times(0)).getGlobalProperty(any());
	}
	
	@Test
	public void shouldLoadAnonymizeConfig_whenAnonymizeIsEnabled() {
		when(adminService.getGlobalProperty(any())).thenReturn("src/test/resources/FHIR Export/config/anonymise-fhir.json");
		anonymiseHandler.loadAnonymiserConfig(true);
		verify(adminService, times(1)).getGlobalProperty(any());
	}
	
	@Test
	public void shouldAnonymisePatientResourceWithRedact_WhenPatientResourceTypeFieldsAreConfiguredWithMethodAsRedact() {
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
	public void shouldAnonymisePatientResourceWithRandomValue_WhenPatientResourceTypeFieldsAreConfiguredWithMethodAsRandom() {
		when(adminService.getGlobalProperty(any())).thenReturn(
		    "src/test/resources/FHIR Export/config/anonymise-fhir-random.json");
		anonymiseHandler.loadAnonymiserConfig(true);
		Patient patient = mockPatientResource();
		boolean isAddressPresentInitially = patient.hasAddress();
		boolean isTelecomPresentInitially = patient.hasTelecom();
		
		anonymiseHandler.anonymise(patient, "patient");
		
		assertTrue(patient.hasAddress());
		assertTrue(isAddressPresentInitially);
		
		assertTrue(patient.hasTelecom());
		assertTrue(isTelecomPresentInitially);
		
		Address address = patient.getAddress().get(0);
		Extension extension = address.getExtension().get(0);
		assertNotEquals(address.getCity(), "previousDummyValue");
		assertNotEquals(address.getDistrict(), "previousDummyValue");
		assertNotEquals(address.getCountry(), "previousDummyValue");
		assertNotEquals(extension.getValue().primitiveValue(), "previousDummyValue");
		ContactPoint contactPoint = patient.getTelecom().get(0);
		assertNotEquals(contactPoint.getValue(), "0123456789");
		assertTrue(contactPoint.getValue().matches("-?\\d+(\\.\\d+)?"));
	}
	
	@Test
	public void shouldAnonymisePatientResourceWithGivenFixedValue_WhenPatientResourceTypeFieldsAreConfiguredWithMethodAsFixed() {
		when(adminService.getGlobalProperty(any())).thenReturn(
		    "src/test/resources/FHIR Export/config/anonymise-fhir-fixed.json");
		anonymiseHandler.loadAnonymiserConfig(true);
		Patient patient = mockPatientResource();
		boolean isAddressPresentInitially = patient.hasAddress();
		boolean isTelecomPresentInitially = patient.hasTelecom();
		
		anonymiseHandler.anonymise(patient, "patient");
		
		assertTrue(patient.hasAddress());
		assertTrue(isAddressPresentInitially);
		Address address = patient.getAddress().get(0);
		Extension extension = address.getExtension().get(0);
		assertEquals(address.getCity(), "fixedDummyValue");
		assertEquals(address.getDistrict(), "fixedDummyValue");
		assertEquals(address.getCountry(), "fixedDummyValue");
		assertEquals(extension.getValue().primitiveValue(), "fixedDummyValue");
		ContactPoint contactPoint = patient.getTelecom().get(0);
		assertEquals(contactPoint.getValue(), "9876543210");
		
		assertTrue(patient.hasTelecom());
		assertTrue(isTelecomPresentInitially);
	}
	
	@Test
	public void shouldAnonymiseConditionResource_WhenConditionResourceTypeFieldsAreConfigured() {
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
	public void shouldAnonymiseMedicationRequestResource_WhenMedicationRequestResourceTypeFieldsAreConfigured() {
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
	public void shouldAnonymiseServiceRequestResource_WhenServiceRequestResourceTypeFieldsAreConfigured() {
		when(adminService.getGlobalProperty(any())).thenReturn("src/test/resources/FHIR Export/config/anonymise-fhir.json");
		anonymiseHandler.loadAnonymiserConfig(true);
		
		ServiceRequest serviceRequest = mockServiceRequestResource();
		boolean isEncounterPresentInitially = serviceRequest.hasEncounter();
		
		anonymiseHandler.anonymise(serviceRequest, "serviceRequest");
		
		assertFalse(serviceRequest.hasEncounter());
		assertTrue(isEncounterPresentInitially);
		
	}
	
	@Test
	public void shouldAnonymisePatientResourceWithCorrelation_WhenAllResourceTypesAreConfiguredWithMethodAsCorrelate() {
		when(adminService.getGlobalProperty(any())).thenReturn(
		    "src/test/resources/FHIR Export/config/anonymise-fhir-correlate.json");
		when(correlationCache.readDigest(anyString(), any())).thenReturn("newDummyId");
		
		anonymiseHandler.loadAnonymiserConfig(true);
		Patient patient = mockPatientResource();
		Condition condition = mockConditionResource();
		MedicationRequest medicationRequest = mockMedicationRequestResource();
		ServiceRequest serviceRequest = mockServiceRequestResource();
		
		assertEquals(patient.getId(), "DummyId");
		assertEquals(condition.getSubject().getReference(), "Patient/DummyId");
		assertEquals(medicationRequest.getSubject().getReference(), "Patient/DummyId");
		assertEquals(serviceRequest.getSubject().getReference(), "Patient/DummyId");
		
		anonymiseHandler.anonymise(patient, "patient");
		anonymiseHandler.anonymise(condition, "condition");
		anonymiseHandler.anonymise(medicationRequest, "medicationRequest");
		anonymiseHandler.anonymise(serviceRequest, "serviceRequest");
		
		assertEquals(patient.getId(), "newDummyId");
		assertEquals(condition.getSubject().getReference(), "Patient/newDummyId");
		assertEquals(medicationRequest.getSubject().getReference(), "Patient/newDummyId");
		assertEquals(serviceRequest.getSubject().getReference(), "Patient/newDummyId");
	}
	
	@Test
    public void shouldThrowException_WhenInvalidConfigFilePathProvided() {
        when(adminService.getGlobalProperty(any())).thenReturn("dummyPath");
        assertThrows(RuntimeException.class, () -> anonymiseHandler.loadAnonymiserConfig(true));
    }
	
	private Patient mockPatientResource() {
        Patient patient = new Patient();
		patient.setId("DummyId");
        List<Identifier> identifiers = new ArrayList<>();
        identifiers.add(new Identifier());
        identifiers.add(new Identifier());
        patient.setIdentifier(identifiers);
        List<HumanName> names = new ArrayList<>();
        names.add(new HumanName().addGiven("Dummy"));
        patient.setName(names);
        List<Address> addresses = Collections.singletonList(new Address().setCity("previousDummyValue").setCountry("previousDummyValue").setDistrict("previousDummyValue"));
		List<Extension> extensions = Collections.singletonList(new Extension("dummyUrl", new StringType("dummyExtensionValue")));
		addresses.get(0).setExtension(extensions);
        patient.setAddress(addresses);
        List<ContactPoint> contactPoints = new ArrayList<>();
        ContactPoint contactPoint = new ContactPoint();
        contactPoints.add(contactPoint.setValue("0123456789"));
        patient.setTelecom(contactPoints);

		DateType birthDateElement = new DateType("2000-05-03");
		patient.setBirthDateElement(birthDateElement);
		patient.setDeceased(new DateTimeType("2023-07-31T00:00:00.000+00:00"));

        return patient;
    }
	
	private MedicationRequest mockMedicationRequestResource() {
		MedicationRequest medicationRequest = new MedicationRequest();
		medicationRequest.setSubject(mockSubjectReferenceWith("DummyId"));
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
		condition.setSubject(mockSubjectReferenceWith("DummyId"));
		condition.setEncounter(new Reference(MOCK_REFERENCE_ID));
		condition.setRecorder(new Reference(MOCK_REFERENCE_ID));
		condition.setRecordedDate(new Date());
		return condition;
	}
	
	private ServiceRequest mockServiceRequestResource() {
		ServiceRequest serviceRequest = new ServiceRequest();
		serviceRequest.setSubject(mockSubjectReferenceWith("DummyId"));
		serviceRequest.setEncounter(new Reference(MOCK_REFERENCE_ID));
		return serviceRequest;
	}
	
	private Reference mockSubjectReferenceWith(String id) {
		String subjectReferenceStr = new StringBuilder("Patient/").append(id).toString();
		return new Reference(subjectReferenceStr);
	}
}
