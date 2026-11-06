package org.openmrs.module.fhirExtension.web;

import org.hamcrest.CoreMatchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.api.context.Context;
import org.openmrs.module.fhirExtension.service.FileDownloadService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Objects;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class FileDownloadControllerTest {
	
	@InjectMocks
	private FileDownloadController fileDownloadController;
	
	@Mock
	private FileDownloadService fileDownloadService;
	
	private MockedStatic<Context> mockedContext;
	
	@Before
	public void setup() {
		mockedContext = Mockito.mockStatic(Context.class);
		mockedContext.when(() -> Context.getRegisteredComponent("fileDownloadService", FileDownloadService.class))
		        .thenReturn(fileDownloadService);
		HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
		RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
	}
	
	@After
	public void tearDown() {
		mockedContext.close();
	}
	
	@Test
	public void shouldReturn_File_WhenExists() throws Exception {
		byte[] bytes = new byte[10];
		when(fileDownloadService.getFile(any())).thenReturn(bytes);
		ResponseEntity<?> responseEntity = fileDownloadController.getFile("fileName");
		assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
		assertThat(Objects.requireNonNull(responseEntity.getHeaders().get("Content-Disposition")).get(0),
		    CoreMatchers.containsString("fileName.zip"));
		assertEquals(bytes, responseEntity.getBody());
	}
	
	@Test
	public void shouldReturn_NotFound_WhenMissingFile() throws Exception {
		when(fileDownloadService.getFile(any())).thenThrow(new IOException());
		ResponseEntity<?> responseEntity = fileDownloadController.getFile("fileName");
		assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
	}
	
}
