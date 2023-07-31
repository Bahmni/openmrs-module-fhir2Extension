package org.openmrs.module.fhirExtension.export.impl;

import ca.uhn.fhir.rest.api.server.IBundleProvider;
import ca.uhn.fhir.rest.server.SimpleBundleProvider;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.HumanName;
import org.hl7.fhir.r4.model.Patient;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.module.fhir2.api.FhirPatientService;
import org.openmrs.module.fhir2.api.search.param.PatientSearchParams;
import org.openmrs.module.fhirExtension.export.anonymise.config.AnonymiserConfig;
import org.openmrs.module.fhirExtension.export.anonymise.handler.AnonymiseHandler;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PatientExportTest {
	
	@Mock
	private FhirPatientService fhirPatientService;
	
	@Mock
	private AnonymiseHandler anonymiseHandler;
	
	@InjectMocks
	private PatientExport patientExport;
	
	@Test
	public void shouldExportPatientDataInFhirFormat_whenValidDateRangeProvided() {
		when(fhirPatientService.searchForPatients(any(PatientSearchParams.class))).thenReturn(getMockPatientBundle());
		
		List<IBaseResource> patientResources = patientExport.export("2023-05-01", "2023-05-31", false);
		verify(anonymiseHandler, times(0)).anonymise(any(), any());
		
		assertNotNull(patientResources);
		assertEquals(1, patientResources.size());
	}
	
	@Test
	public void shouldExportAnonymisedPatientDataInFhirFormat_whenValidDateRangeProvided() {
		when(fhirPatientService.searchForPatients(any(PatientSearchParams.class))).thenReturn(getMockPatientBundle());
		List<IBaseResource> patientResources = patientExport.export("2023-05-01", "2023-05-31", true);
		verify(anonymiseHandler, times(1)).anonymise(any(IBaseResource.class), eq("patient"));
		assertNotNull(patientResources);
		assertEquals(1, patientResources.size());
	}
	
	private IBundleProvider getMockPatientBundle() {
		HumanName humanName = new HumanName();
		humanName.addGiven("John");
		humanName.setFamily("Smith");
		
		Patient fhirPatient = new org.hl7.fhir.r4.model.Patient();
		fhirPatient.setId("PATIENT_UUID");
		fhirPatient.addName(humanName);
		IBundleProvider iBundleProvider = new SimpleBundleProvider(Arrays.asList(fhirPatient));
		return iBundleProvider;
	}
}
