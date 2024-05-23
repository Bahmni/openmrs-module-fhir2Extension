package org.openmrs.module.fhirExtension.model;

import lombok.*;
import org.openmrs.module.fhir2.model.FhirTask;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskSearchRequest {
	
	private List<String> taskName;
	
	private List<FhirTask.TaskStatus> taskStatus;
}
