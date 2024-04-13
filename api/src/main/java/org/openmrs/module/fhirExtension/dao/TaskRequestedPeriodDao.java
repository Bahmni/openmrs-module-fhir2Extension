package org.openmrs.module.fhirExtension.dao;

import java.util.List;
import org.openmrs.Visit;
import org.openmrs.module.fhir2.model.FhirTask;
import org.openmrs.module.fhirExtension.model.FhirTaskRequestedPeriod;

import java.util.Date;
import java.util.List;

public interface TaskRequestedPeriodDao {
	
	FhirTaskRequestedPeriod getTaskRequestedPeriodByTaskId(Integer taskId);
	
	FhirTaskRequestedPeriod save(FhirTaskRequestedPeriod fhirTaskRequestedPeriod);
	
	List<FhirTaskRequestedPeriod> bulkSave(List<FhirTaskRequestedPeriod> fhirTaskRequestedPeriods);
	
	List<FhirTaskRequestedPeriod> bulkUpdate(List<FhirTaskRequestedPeriod> fhirTaskRequestedPeriods);
}
