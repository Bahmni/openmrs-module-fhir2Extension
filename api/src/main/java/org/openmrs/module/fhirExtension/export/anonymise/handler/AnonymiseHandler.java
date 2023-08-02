package org.openmrs.module.fhirExtension.export.anonymise.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.openmrs.api.AdministrationService;
import org.openmrs.module.fhirExtension.export.anonymise.config.AnonymiseResourceConfig;
import org.openmrs.module.fhirExtension.export.anonymise.config.AnonymiseConfig;
import org.openmrs.module.fhirExtension.export.anonymise.factory.RandomiseFieldHandlerSingletonFactory;
import org.openmrs.module.fhirExtension.export.anonymise.factory.RedactFieldHandlerSingletonFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Component
@Log4j2
public class AnonymiseHandler {
	
	private static final String GP_ANONYMISATION_CONFIG_PROPERTIES_FILE_PATH = "fhir.export.anonymise.config.path";
	
	private static final String REDACT_METHOD_NAME = "redact";
	
	private static final String RANDOMISE_METHOD_NAME = "random";

	private final AdministrationService adminService;
	
	private AnonymiseConfig anonymiseConfig;
	
	@Autowired
	public AnonymiseHandler(AdministrationService adminService) {
		this.adminService = adminService;
	}
	
	public void anonymise(IBaseResource iBaseResource, String resourceType) {
        List<AnonymiseResourceConfig> anonymiseResourceConfigList = anonymiseConfig.getConfig().get(resourceType);
        if (anonymiseResourceConfigList == null) {
            return;
        }
        anonymiseResourceConfigList.forEach(fieldConfig -> {
            if (StringUtils.isBlank(fieldConfig.getFieldName())) {
                return;
            }
            if (REDACT_METHOD_NAME.equalsIgnoreCase(fieldConfig.getMethod())) {
                RedactFieldHandlerSingletonFactory.getInstance(fieldConfig.getFieldName()).redact(iBaseResource);
            } else if (RANDOMISE_METHOD_NAME.equalsIgnoreCase(fieldConfig.getMethod())) {
                RandomiseFieldHandlerSingletonFactory.getInstance(fieldConfig.getFieldName()).randomise(iBaseResource);
            }
        });
    }
	
	public void loadAnonymiserConfig(boolean isAnonymise) {
        if (!isAnonymise) {
            return;
        }
        String configPathGlobalPropertyValue = adminService.getGlobalProperty(GP_ANONYMISATION_CONFIG_PROPERTIES_FILE_PATH);
        Path configFilePath = null;
        if (StringUtils.isNotBlank(configPathGlobalPropertyValue)) {
            configFilePath = Paths.get(configPathGlobalPropertyValue);
        }
        if (StringUtils.isEmpty(configPathGlobalPropertyValue) || !Files.exists(configFilePath)) {
            String errorMessage = String.format("Fhir export anonymisation config file does not exist: [%s].", configFilePath);
            log.error(errorMessage);
            throw new RuntimeException(errorMessage);
        }
        try (InputStream configFile = Files.newInputStream(configFilePath)) {
            ObjectMapper mapper = new ObjectMapper();
            anonymiseConfig = mapper.readValue(configFile, AnonymiseConfig.class);
        } catch (Exception e) {
            String errorMessage = String.format("Exception while parsing the configuration: [%s].", configFilePath);
            log.error(errorMessage);
            throw new RuntimeException(errorMessage, e);
        }
    }
}
