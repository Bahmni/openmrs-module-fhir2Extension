package org.openmrs.module.fhirExtension.export.anonymise.config;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FieldConfig {
	
	private String fieldName;
	
	private String method;
	
	private String value;
}
