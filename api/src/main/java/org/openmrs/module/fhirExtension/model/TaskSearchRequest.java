package org.openmrs.module.fhirExtension.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.openmrs.module.fhir2.model.FhirTask;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TaskSearchRequest {
	
	private List<String> taskName;
	
	private List<FhirTask.TaskStatus> taskStatus;
}
