package org.openmrs.module.fhirExtension.web.contract;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class PatientTaskResponse {
	
	private String patientUuid;
	
	private List<TaskResponse> tasks;
}
