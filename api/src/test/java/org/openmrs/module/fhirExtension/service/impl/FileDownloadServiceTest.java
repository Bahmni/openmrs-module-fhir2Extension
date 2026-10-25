package org.openmrs.module.fhirExtension.service.impl;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.UserContext;
import org.springframework.beans.factory.annotation.Qualifier;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.mockito.Mockito.when;
import static org.openmrs.module.fhirExtension.service.FileExportService.FHIR_EXPORT_FILES_DIRECTORY_GLOBAL_PROP;

@RunWith(MockitoJUnitRunner.class)
public class FileDownloadServiceTest {
	
	@Mock
	@Qualifier("adminService")
	AdministrationService administrationService;
	
	@Mock
	private UserContext userContext;
	
	@InjectMocks
	private FileDownloadServiceImpl fileDownloadService;
	
	private MockedStatic<Context> mockedContext;
	
	String basePath = System.getProperty("java.io.tmpdir");
	
	@Before
	public void setup() {
		mockedContext = Mockito.mockStatic(Context.class);
		mockedContext.when(Context::getAdministrationService).thenReturn(administrationService);
	}
	
	@After
	public void tearDown() throws IOException {
		mockedContext.close();
		Path newFilePath = Paths.get(basePath, "dummy.zip");
		Files.deleteIfExists(newFilePath);
	}
	
	@Test
	public void shouldReturnFileContentWhenFileToDownloadExistsInFilePath() throws IOException {
		
		Path newFilePath = Paths.get(basePath, "dummy.zip");
		Files.createFile(newFilePath);
		when(administrationService.getGlobalProperty(FHIR_EXPORT_FILES_DIRECTORY_GLOBAL_PROP)).thenReturn(basePath);
		byte[] result = fileDownloadService.getFile("dummy");
		Assert.assertNotNull(result);
	}
	
	@Test
    public void shouldThrowErrorWhenFileToDownloadDoesNotExist() {
        Assert.assertThrows(IOException.class, () -> fileDownloadService.getFile("nonExistingFile"));
    }
	
	@Test
    public void shouldThrowErrorWhenFileDownloadDirectoryIsInvalid() {
        when(administrationService.getGlobalProperty(FHIR_EXPORT_FILES_DIRECTORY_GLOBAL_PROP)).thenReturn("invalidPath");
        Assert.assertThrows(NoSuchFileException.class, () -> fileDownloadService.getFile("dummy"));
    }
}
