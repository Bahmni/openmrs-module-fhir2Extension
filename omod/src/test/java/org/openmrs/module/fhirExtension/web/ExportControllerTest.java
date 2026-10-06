package org.openmrs.module.fhirExtension.web;

import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.openmrs.api.APIAuthenticationException;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
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
		when(exportTask.validateParams("2023-05-01", "2023-05-31", "true")).thenReturn(null);
		ResponseEntity<SimpleObject> responseEntity = exportController.export("2023-05-01", "2023-05-31", "true");
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
		when(exportTask.validateParams("2023-05-AB", "2023-05-31", "true")).thenReturn("Invalid Date Format [yyyy-mm-dd]");
		ResponseEntity<SimpleObject> responseEntity = exportController.export("2023-05-AB", "2023-05-31", "true");
		SimpleObject simpleObject = responseEntity.getBody();
		assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
		assertEquals("Invalid Date Format [yyyy-mm-dd]", simpleObject.get("error"));
	}
	
	@Test
	public void shouldGetBadRequest_whenEndDateIsBeforeStartDate() {
		String validationError = "End date [2023-05-31] should be on or after start date [2023-06-01]";
		when(exportTask.validateParams("2023-06-01", "2023-05-31", "true")).thenReturn(validationError);
		ResponseEntity<SimpleObject> responseEntity = exportController.export("2023-06-01", "2023-05-31", "true");
		assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
		assertEquals(validationError, responseEntity.getBody().get("error"));
		verify(exportTask, never()).getInitialTaskResponse(any(), any(), any(), anyBoolean());
		verify(exportAsyncServiceImpl, never()).export(any(), any(), any(), any(), anyBoolean());
	}
	
	@Test
	public void shouldReturnForbidden_whenLoggedInUserDoesNotHavePrivilegeToExportNonAnonymisedData() {
		ContextAuthenticationException exception = new ContextAuthenticationException(
				"Privileges required: Export Non Anonymised Patient Data");
		when(exportTask.getInitialTaskResponse(any(), any(), any(), anyBoolean())).thenThrow(exception);
		when(exportTask.validateParams("2023-05-01", "2023-05-31", "false")).thenReturn(null);
		assertThrows(ContextAuthenticationException.class,
				() -> exportController.export("2023-05-01", "2023-05-31", "false"));
		ResponseEntity<SimpleObject> responseEntity = exportController.handleContextAuthenticationException(exception);
		assertEquals(HttpStatus.FORBIDDEN, responseEntity.getStatusCode());
		assertEquals("Privileges required: Export Non Anonymised Patient Data", responseEntity.getBody().get("error"));
		verify(exportAsyncServiceImpl, never()).export(any(), any(), any(), any(), anyBoolean());
	}
	
	@Test
	public void shouldPropagateApiAuthenticationException_whenUserLacksExportPrivilege() {
		APIAuthenticationException exception = new APIAuthenticationException(
				"Privileges required: Export Patient Data");
		when(exportTask.validateParams("2023-05-01", "2023-05-31", "true")).thenThrow(exception);
		assertThrows(APIAuthenticationException.class,
				() -> exportController.export("2023-05-01", "2023-05-31", "true"));
		verify(exportTask, never()).getInitialTaskResponse(any(), any(), any(), anyBoolean());
		verify(exportAsyncServiceImpl, never()).export(any(), any(), any(), any(), anyBoolean());
	}
	
	@Test
	public void shouldReturnBadRequest_whenAnonymiseIsMissing() {
		when(exportTask.validateParams("2023-05-01", "2023-05-31", null)).thenReturn(
		    "Anonymise must be either true or false");
		ResponseEntity<SimpleObject> responseEntity = exportController.export("2023-05-01", "2023-05-31", null);
		assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
		assertEquals("Anonymise must be either true or false", responseEntity.getBody().get("error"));
		verify(exportTask).validateParams("2023-05-01", "2023-05-31", null);
		verify(exportTask, never()).getInitialTaskResponse(any(), any(), any(), anyBoolean());
	}
	
	@Test
	public void shouldReturnBadRequest_whenAnonymiseIsInvalid() {
		when(exportTask.validateParams("2023-05-01", "2023-05-31", "yes")).thenReturn(
		    "Anonymise must be either true or false");
		ResponseEntity<SimpleObject> responseEntity = exportController.export("2023-05-01", "2023-05-31", "yes");
		assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
		assertEquals("Anonymise must be either true or false", responseEntity.getBody().get("error"));
		verify(exportTask).validateParams("2023-05-01", "2023-05-31", "yes");
		verify(exportTask, never()).getInitialTaskResponse(any(), any(), any(), anyBoolean());
	}
	
	@Test
	public void shouldReturnBadRequest_whenAnonymiseIsEmpty() {
		when(exportTask.validateParams("2023-05-01", "2023-05-31", "")).thenReturn("Anonymise must be either true or false");
		ResponseEntity<SimpleObject> responseEntity = exportController.export("2023-05-01", "2023-05-31", "");
		assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
		assertEquals("Anonymise must be either true or false", responseEntity.getBody().get("error"));
		verify(exportTask).validateParams("2023-05-01", "2023-05-31", "");
		verify(exportTask, never()).getInitialTaskResponse(any(), any(), any(), anyBoolean());
	}

	@Test
	public void shouldAcceptRequest_whenAnonymiseIsMixedCase() {
		when(exportTask.validateParams("2023-05-01", "2023-05-31", "True")).thenReturn(null);
		when(exportTask.getInitialTaskResponse(eq("2023-05-01"), eq("2023-05-31"), any(), eq(true))).thenReturn(
		    mockFhirTask());
		doNothing().when(exportAsyncServiceImpl).export(any(), any(), any(), any(), anyBoolean());
		ResponseEntity<SimpleObject> responseEntity = exportController.export("2023-05-01", "2023-05-31", "True");
		assertEquals(HttpStatus.ACCEPTED, responseEntity.getStatusCode());
		verify(exportAsyncServiceImpl).export(any(), any(), any(), any(), eq(true));
	}

	@Test
	public void shouldAcceptRequest_whenStartDateIsMissing() {
		when(exportTask.validateParams(null, "2023-05-31", "true")).thenReturn(null);
		when(exportTask.getInitialTaskResponse(any(), any(), any(), anyBoolean())).thenReturn(mockFhirTask());
		doNothing().when(exportAsyncServiceImpl).export(any(), any(), any(), any(), anyBoolean());
		ResponseEntity<SimpleObject> responseEntity = exportController.export(null, "2023-05-31", "true");
		assertEquals(HttpStatus.ACCEPTED, responseEntity.getStatusCode());
	}
	
	@Test
	public void shouldAcceptRequest_whenEndDateIsMissing() {
		when(exportTask.validateParams("2023-05-01", null, "true")).thenReturn(null);
		when(exportTask.getInitialTaskResponse(any(), any(), any(), anyBoolean())).thenReturn(mockFhirTask());
		doNothing().when(exportAsyncServiceImpl).export(any(), any(), any(), any(), anyBoolean());
		ResponseEntity<SimpleObject> responseEntity = exportController.export("2023-05-01", null, "true");
		assertEquals(HttpStatus.ACCEPTED, responseEntity.getStatusCode());
	}
	
	private FhirTask mockFhirTask() {
		FhirTask fhirTask = new FhirTask();
		fhirTask.setStatus(FhirTask.TaskStatus.ACCEPTED);
		fhirTask.setUuid(FHIR_TASK_UUID);
		return fhirTask;
	}
}
