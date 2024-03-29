package org.openmrs.module.fhirExtension.web.contract;

import lombok.Getter;
import lombok.Setter;
import org.openmrs.module.fhir2.model.FhirTask;

import java.util.Date;

@Getter
@Setter
public class TaskUpdateResponse {
	
	private String uuid;
	
	private String name;
	
	private FhirTask.TaskStatus status;
	
	private Date executionStartTime;
	
	private Date executionEndTime;
}