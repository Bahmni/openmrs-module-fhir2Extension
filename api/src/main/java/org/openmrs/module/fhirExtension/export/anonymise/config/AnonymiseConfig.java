package org.openmrs.module.fhirExtension.export.anonymise.config;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
public class AnonymiseConfig {
	
	private Map<String, List<AnonymiseResourceConfig>> config;
	
}
