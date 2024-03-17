package org.openmrs.module.fhirExtension.model;

import lombok.*;
import org.openmrs.module.fhir2.model.FhirTask;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Task {
	
	private FhirTask fhirTask;
	
	private FhirTaskRequestedPeriod fhirTaskRequestedPeriod;
	
}
