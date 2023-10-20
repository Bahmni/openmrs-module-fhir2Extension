package org.openmrs.module.fhirExtension.service;

import ca.uhn.fhir.rest.api.server.IBundleProvider;
import ca.uhn.fhir.rest.server.SimpleBundleProvider;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Condition;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.openmrs.Concept;
import org.openmrs.Person;
import org.openmrs.User;
import org.openmrs.api.ConceptService;
import org.openmrs.api.context.Context;
import org.openmrs.module.fhir2.api.FhirConditionService;
import org.openmrs.module.fhir2.api.dao.FhirTaskDao;
import org.openmrs.module.fhir2.model.FhirTask;
import org.openmrs.module.fhirExtension.export.Exporter;
import org.openmrs.module.fhirExtension.export.anonymise.handler.AnonymiseHandler;
import org.openmrs.module.fhirExtension.export.anonymise.impl.CorrelationCache;
import org.openmrs.module.fhirExtension.export.impl.ConditionExport;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest(Context.class)
@PowerMockIgnore("javax.management.*")
public class ExportAsyncServiceTest {
	
	@Mock
	private FhirTaskDao fhirTaskDao;
	
	@Mock
	private ConceptService conceptService;
	
	@Mock
	private FileExportService fileExportService;
	
	@Mock
	private FhirConditionService fhirConditionService;
	
	@Mock
	private AnonymiseHandler anonymiseHandler;
	
	@Mock
	private CorrelationCache correlationCache;
	
	@InjectMocks
	private ExportAsyncService exportAsyncService;
	
	@Before
	public void setUp() {
		PowerMockito.mockStatic(Context.class);
		User authenticatedUser = new User();
		authenticatedUser.setPerson(new Person());
		when(Context.getAuthenticatedUser()).thenReturn(authenticatedUser);
	}
	
	@Test
	public void shouldExportPatientDataAndUpdateFhirTaskStatusToCompleted_whenValidDateRangeProvided() {
		FhirTask fhirTask = mockFhirTask();
		when(conceptService.getConceptByName(any())).thenReturn(new Concept());
		exportAsyncService.export(fhirTask, "2023-01-01", "2023-12-31", Context.getUserContext(), "", false);
		
		assertEquals(FhirTask.TaskStatus.COMPLETED, fhirTask.getStatus());
		verify(conceptService, times(1)).getConceptByName("Download URL");
		verify(fhirTaskDao, times(1)).createOrUpdate(any(FhirTask.class));
	}
	
	@Test
	public void shouldChangeFhirTaskStatusToRejected_whenInvalidDateRangeProvided() {
		List<Exporter> exporters = new ArrayList<>();
		exporters.add(new ConditionExport(fhirConditionService));
		when(Context.getRegisteredComponents(Exporter.class)).thenReturn(exporters);

		FhirTask fhirTask = mockFhirTask();

		exportAsyncService.export(fhirTask, "2023-AB-CD", "2023-12-31", Context.getUserContext(), "", false);

		assertEquals(FhirTask.TaskStatus.REJECTED, fhirTask.getStatus());
		verify(fhirTaskDao, times(1)).createOrUpdate(any(FhirTask.class));
	}
	
	@Test
	public void shouldCallAnonymiseHandler_whenValidDateRangeProvided() {
		List<Exporter> exporters = new ArrayList<>();
		exporters.add(new ConditionExport(fhirConditionService));
		when(Context.getRegisteredComponents(Exporter.class)).thenReturn(exporters);
		when(fhirConditionService.searchConditions(any(), any(), any(), any(), any(), any(), any(), any(), any(), any())).thenReturn(getMockConditionBundle(1));
		when(conceptService.getConceptByName(any())).thenReturn(new Concept());

		FhirTask fhirTask = mockFhirTask();

		exportAsyncService.export(fhirTask, "2023-01-01", "2023-12-31", Context.getUserContext(), "", true);

		assertEquals(FhirTask.TaskStatus.COMPLETED, fhirTask.getStatus());
		verify(anonymiseHandler, times(1)).anonymise(any(IBaseResource.class), eq("condition"));
		verify(fhirTaskDao, times(1)).createOrUpdate(any(FhirTask.class));
	}
	
	private FhirTask mockFhirTask() {
		FhirTask fhirTask = new FhirTask();
		fhirTask.setStatus(FhirTask.TaskStatus.ACCEPTED);
		return fhirTask;
	}
	
	private IBundleProvider getMockConditionBundle(int count) {
		Condition activeConditionResource = new Condition();
		CodeableConcept activeClinicalStatus = new CodeableConcept();
		activeClinicalStatus.setCoding(Collections.singletonList(new Coding("dummy", "active", "active")));
		activeConditionResource.setClinicalStatus(activeClinicalStatus);
		Condition inactiveConditionResource = new Condition();
		CodeableConcept inactiveClinicalStatus = new CodeableConcept();
		inactiveClinicalStatus.setCoding(Collections.singletonList(new Coding("dummy", "history", "history")));
		inactiveConditionResource.setClinicalStatus(inactiveClinicalStatus);
		
		IBundleProvider iBundleProvider = null;
		if (count == 1) {
			iBundleProvider = new SimpleBundleProvider(Arrays.asList(activeConditionResource));
		} else {
			iBundleProvider = new SimpleBundleProvider(Arrays.asList(activeConditionResource, inactiveConditionResource));
		}
		
		return iBundleProvider;
	}
}
