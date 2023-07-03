package org.openmrs.module.fhirExtension.web;

import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.openmrs.api.context.Context;
import org.openmrs.module.fhir2.model.FhirTask;
import org.openmrs.module.fhirExtension.service.ExportAsyncService;
import org.openmrs.module.fhirExtension.service.ExportTask;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ Context.class })
@PowerMockIgnore("javax.management.*")
public class ExportControllerTest {
	
	public static final String FHIR2_R4_TASK_URI = "/ws/fhir2/R4/Task/";
	
	public static final String FHIR_TASK_UUID = "8bb0795c-4ff0-0305-1990-000000000001";
	
	@Mock
	private ExportTask exportTask;
	
	@Mock
	private ExportAsyncService exportAsyncService;
	
	@InjectMocks
	private ExportController exportController;
	
	@Mock
	HttpServletRequest request;
	
	@Before
	public void setUp() {
		PowerMockito.mockStatic(Context.class);
		RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
	}
	
	@Test
	public void shouldGetFhirTaskUrl_whenFhirExportCalled() {
		doNothing().when(exportAsyncService).export(any(), any(), any(), any(), any());
		when(exportTask.getInitialTaskResponse()).thenReturn(mockFhirTask());
		ResponseEntity responseEntity = exportController.export("2023-05-01", "2023-05-31");
		assertEquals(HttpStatus.ACCEPTED, responseEntity.getStatusCode());
		assertThat(responseEntity.getBody().toString(), CoreMatchers.containsString(FHIR2_R4_TASK_URI + FHIR_TASK_UUID));
	}
	
	private FhirTask mockFhirTask() {
		FhirTask fhirTask = new FhirTask();
		fhirTask.setStatus(FhirTask.TaskStatus.ACCEPTED);
		fhirTask.setUuid(FHIR_TASK_UUID);
		return fhirTask;
	}
}
