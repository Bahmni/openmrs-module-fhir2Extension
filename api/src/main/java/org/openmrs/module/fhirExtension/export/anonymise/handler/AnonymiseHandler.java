package org.openmrs.module.fhirExtension.export.anonymise.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.openmrs.api.AdministrationService;
import org.openmrs.module.fhirExtension.export.anonymise.config.AnonymiseConfig;
import org.openmrs.module.fhirExtension.export.anonymise.config.FieldConfig;
import org.openmrs.module.fhirExtension.export.anonymise.factory.RandomiseFieldHandlerSingletonFactory;
import org.openmrs.module.fhirExtension.export.anonymise.factory.RedactFieldHandlerSingletonFactory;
import org.openmrs.module.fhirExtension.export.anonymise.impl.CorrelationCache;
import org.openmrs.module.fhirExtension.export.anonymise.impl.IdResourceCorrelate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

@Component
@Log4j2
public class AnonymiseHandler {
	
	private static final String GP_ANONYMISATION_CONFIG_PROPERTIES_FILE_PATH = "fhir.export.anonymise.config.path";
	
	private static final String REDACT_METHOD_NAME = "redact";
	
	private static final List<String> RANDOMISE_METHOD_NAMES = Arrays.asList("random", "firstOfMonth", "fixed");

	private static final String CORRELATE_METHOD_NAME = "correlate";
	
	private final AdministrationService adminService;
	
	private AnonymiseConfig anonymiseConfig;
	
	private CorrelationCache correlationCache;

	private byte[] salt;

	@Autowired
	public AnonymiseHandler(AdministrationService adminService, CorrelationCache correlationCache) {
		this.adminService = adminService;
		this.correlationCache = correlationCache;
	}
	
	public void anonymise(IBaseResource iBaseResource, String resourceType) {
        List<FieldConfig> fieldConfigList = anonymiseConfig.getResources().get(resourceType);
        if (fieldConfigList == null) {
            return;
        }
        fieldConfigList.forEach(fieldConfig -> {
            if (StringUtils.isBlank(fieldConfig.getFieldName())) {
                return;
            }
            if (REDACT_METHOD_NAME.equalsIgnoreCase(fieldConfig.getMethod())) {
                RedactFieldHandlerSingletonFactory.getInstance(fieldConfig.getFieldName()).redact(iBaseResource);
            } else if (RANDOMISE_METHOD_NAMES.contains(fieldConfig.getMethod())) {
                RandomiseFieldHandlerSingletonFactory.getInstance(fieldConfig.getFieldName()).randomise(iBaseResource, fieldConfig.getValue());
            } else if (CORRELATE_METHOD_NAME.equalsIgnoreCase(fieldConfig.getMethod())) {
                IdResourceCorrelate.getInstance().correlateResource(iBaseResource, resourceType, correlationCache, salt);
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
            if(anonymiseConfig.getParameters()!=null && anonymiseConfig.getParameters().containsKey("oneWayHashSalt")) {
                String saltStr = anonymiseConfig.getParameters().get("oneWayHashSalt");
                salt = saltStr.getBytes();
            }
        } catch (Exception e) {
            String errorMessage = String.format("Exception while parsing the configuration: [%s].", configFilePath);
            log.error(errorMessage);
            throw new RuntimeException(errorMessage, e);
        }
    }
}
