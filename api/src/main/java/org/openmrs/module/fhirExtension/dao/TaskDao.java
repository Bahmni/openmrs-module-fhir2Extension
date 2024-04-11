package org.openmrs.module.fhirExtension.dao;

import org.openmrs.Visit;
import org.openmrs.module.fhir2.model.FhirTask;
import org.openmrs.module.fhirExtension.model.Task;

import java.util.Date;
import java.util.List;

public interface TaskDao {
	
	List<Task> getTasksByVisitFilteredByTimeFrame(Visit visit, Date startTime, Date endTime);
	
	List<Task> getTasksByPatientUuidsFilteredByTimeFrame(List<String> patientUuids, Date startTime, Date endTime);
	
	List<Task> getTasksByUuids(List<String> listOfUuids);
	
	List<Task> getTasksByNameAndStatus(List<String> taskNames, String taskStatus);
	
	List<FhirTask> bulkSave(List<FhirTask> tasks);
}
