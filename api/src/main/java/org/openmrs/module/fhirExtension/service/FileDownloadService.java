package org.openmrs.module.fhirExtension.service;

import org.openmrs.annotation.Authorized;
import org.openmrs.api.AdministrationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class FileDownloadService {
	
	public static final String FHIR_EXPORT_FILES_DIRECTORY = "fhir.export.files.directory";
	
	private AdministrationService adminService;
	
	@Autowired
	public FileDownloadService(@Qualifier("adminService") AdministrationService adminService) {
		this.adminService = adminService;
	}
	
	@Authorized(value = { "Export Patient Data" })
	public byte[] getFile(String filename) throws IOException {
		String fileDirectory = adminService.getGlobalProperty(FHIR_EXPORT_FILES_DIRECTORY);
		Path path = Paths.get(fileDirectory, filename + ".zip");
		return Files.readAllBytes(path);
	}
	
}
