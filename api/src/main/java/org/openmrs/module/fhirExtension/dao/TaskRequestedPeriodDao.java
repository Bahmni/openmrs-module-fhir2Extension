package org.openmrs.module.fhirExtension.dao;

import java.util.List;
import org.openmrs.module.fhirExtension.model.FhirTaskRequestedPeriod;

public interface TaskRequestedPeriodDao {
	
	FhirTaskRequestedPeriod getTaskRequestedPeriodByTaskId(Integer taskId);
	
	FhirTaskRequestedPeriod save(FhirTaskRequestedPeriod fhirTaskRequestedPeriod);
	
	List<FhirTaskRequestedPeriod> save(List<FhirTaskRequestedPeriod> fhirTaskRequestedPeriods);
	
	List<FhirTaskRequestedPeriod> update(List<FhirTaskRequestedPeriod> fhirTaskRequestedPeriods);
}
