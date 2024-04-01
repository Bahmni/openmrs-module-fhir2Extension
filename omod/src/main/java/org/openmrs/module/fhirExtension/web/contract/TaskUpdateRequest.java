package org.openmrs.module.fhirExtension.web.contract;

import lombok.Getter;
import lombok.Setter;
import org.openmrs.module.fhir2.model.FhirTask;

import javax.validation.constraints.NotEmpty;
import java.util.Date;

@Getter
@Setter
public class TaskUpdateRequest {
	
	@NotEmpty
	private String uuid;
	
	private Date executionStartTime;
	
	private Date executionEndTime;
	
	private FhirTask.TaskStatus status;
	
}
