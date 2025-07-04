package org.openmrs.module.fhirExtension.web;

import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.ContextAuthenticationException;
import org.openmrs.module.fhir2.model.FhirTask;
import org.openmrs.module.fhirExtension.service.impl.ExportAsyncServiceImpl;
import org.openmrs.module.fhirExtension.service.ExportTask;
import org.openmrs.module.webservices.rest.SimpleObject;
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
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
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
	private ExportAsyncServiceImpl exportAsyncServiceImpl;
	
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
		doNothing().when(exportAsyncServiceImpl).export(any(), any(), any(), any(), anyBoolean());
		when(exportTask.getInitialTaskResponse(any(), any(), any(), anyBoolean())).thenReturn(mockFhirTask());
		when(exportTask.validateParams("2023-05-01", "2023-05-31")).thenReturn(null);
		ResponseEntity<SimpleObject> responseEntity = exportController.export("2023-05-01", "2023-05-31", true);
		SimpleObject simpleObject = responseEntity.getBody();
		assertEquals(HttpStatus.ACCEPTED, responseEntity.getStatusCode());
		assertEquals("ACCEPTED", simpleObject.get("status"));
		assertEquals(FHIR_TASK_UUID, simpleObject.get("taskId"));
		assertThat(simpleObject.get("link"), CoreMatchers.containsString(FHIR2_R4_TASK_URI + FHIR_TASK_UUID));
	}
	
	@Test
	public void shouldGetBadRequest_whenFhirExportCalledWithInvalidDateFormat() {
		doNothing().when(exportAsyncServiceImpl).export(any(), any(), any(), any(), anyBoolean());
		when(exportTask.getInitialTaskResponse(any(), any(), any(), anyBoolean())).thenReturn(mockFhirTask());
		when(exportTask.validateParams("2023-05-AB", "2023-05-31")).thenReturn("Invalid Date Format [yyyy-mm-dd]");
		ResponseEntity<SimpleObject> responseEntity = exportController.export("2023-05-AB", "2023-05-31", true);
		SimpleObject simpleObject = responseEntity.getBody();
		assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
		assertEquals("Invalid Date Format [yyyy-mm-dd]", simpleObject.get("error"));
	}
	
	@Test
	public void shouldThrowException_whenLoggedInUserDoesNotHavePrivilegeToExportNonAnonymisedData() {
		when(exportTask.getInitialTaskResponse(any(), any(), any(),anyBoolean())).thenThrow( new ContextAuthenticationException( "Privileges required: Export Non Anonymised Patient Data"));
		when(exportTask.validateParams("2023-05-AB", "2023-05-31")).thenReturn(null);
		assertThrows(ContextAuthenticationException.class, () -> exportController.export("2023-05-AB", "2023-05-31", false));
	}
	
	private FhirTask mockFhirTask() {
		FhirTask fhirTask = new FhirTask();
		fhirTask.setStatus(FhirTask.TaskStatus.ACCEPTED);
		fhirTask.setUuid(FHIR_TASK_UUID);
		return fhirTask;
	}
}
