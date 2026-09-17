package org.openmrs.module.fhirExtension.web.contract;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TaskInputResponseDTO {

	// Represents concept reference (Object from REST API conversion);
	// asymmetry with TaskInputRequestDTO.typeUuid (String) is intentional:
	// request takes UUID for lookup, response returns converted concept reference
	private Object type;
	
	private String valueText;
}
