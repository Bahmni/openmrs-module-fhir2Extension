package org.openmrs.module.fhirExtension.service;

import ca.uhn.fhir.parser.IParser;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.openmrs.api.APIException;
import org.openmrs.api.AdministrationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Log4j2
@Component
public class ExportToFileService {
	
	private static final String NDJSON_EXTENSION = ".ndjson";
	public static final String NEW_LINE = "\n";
	
	private AdministrationService adminService;
	private IParser parser;
	
	@Autowired
	public ExportToFileService(@Qualifier("adminService") AdministrationService adminService, IParser parser) {
		this.adminService = adminService;
		this.parser = parser;
	}
	
	public static final String FHIR_EXPORT_FILES_DIRECTORY_GLOBAL_PROP = "fhir.export.files.directory";
	
	public void createAndWriteToFile(List<IBaseResource> fhirResources, String directory) {
		if (fhirResources.isEmpty())
			return;
		String resourceType = fhirResources.get(0).getClass().getSimpleName();
		String basePath = getBasePath();
		Path filePath = Paths.get(basePath, directory, resourceType + NDJSON_EXTENSION);
		createFile(filePath);
		writeToFile(resourceType, fhirResources, filePath);
	}
	
	public void createDirectory(String directory) {
		String basePath = getBasePath();
		Path directoryPath = Paths.get(basePath, directory);
		try {
			if (!Files.exists(directoryPath)) {
				Files.createDirectories(directoryPath);
			}
		}
		catch (IOException e) {
			log.error("Error while creating directory " + directoryPath);
			throw new RuntimeException(e);
		}
	}
	
	private void writeToFile(String resourceType, List<IBaseResource> fhirResources, Path filePath) {
		try (RandomAccessFile writer = new RandomAccessFile(filePath.toFile(), "rw");
			 FileChannel channel = writer.getChannel()) {
            for (IBaseResource iBaseResource : fhirResources) {
                String jsonStr = parser.encodeResourceToString(iBaseResource) + NEW_LINE;
                ByteBuffer buffer = ByteBuffer.wrap(jsonStr.getBytes(StandardCharsets.UTF_8));
                channel.write(buffer);
            }
        } catch (IOException e) {
            log.error("Exception while processing data for " + resourceType);
            throw new RuntimeException(e);
        }
    }
	
	private String getBasePath() {
		String propertyValue = adminService.getGlobalProperty(FHIR_EXPORT_FILES_DIRECTORY_GLOBAL_PROP);
		if (StringUtils.isBlank(propertyValue))
			throw new APIException();
		return propertyValue;
	}
	
	private void createFile(Path filePath) {
		try {
			if (!Files.exists(filePath)) {
				Files.createFile(filePath);
			}
		}
		catch (IOException e) {
			log.error("Error while creating file " + filePath);
			throw new RuntimeException(e);
		}
	}
}
