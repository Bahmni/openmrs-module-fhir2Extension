package org.openmrs.module.fhirExtension.dao;

import org.openmrs.Visit;
import org.openmrs.module.fhir2.model.FhirTask;
import org.openmrs.module.fhirExtension.model.FhirTaskRequestedPeriod;

import java.util.Date;
import java.util.List;

public interface TaskRequestedPeriodDao {
	
	FhirTaskRequestedPeriod getTaskRequestedPeriodByTaskId(Integer taskId);
	
	FhirTaskRequestedPeriod save(FhirTaskRequestedPeriod fhirTaskRequestedPeriod);
	
}
