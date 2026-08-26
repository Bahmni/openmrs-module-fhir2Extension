package org.openmrs.module.fhirExtension.web.contract;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TaskInputRequestDTO {
	
	private String type;
	
	private String valueText;
}
