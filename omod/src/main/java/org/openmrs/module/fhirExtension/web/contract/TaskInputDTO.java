package org.openmrs.module.fhirExtension.web.contract;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TaskInputDTO {
	
	private String type;
	
	private String valueText;
}
