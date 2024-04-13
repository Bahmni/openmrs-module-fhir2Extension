package org.openmrs.module.fhirExtension.service;

import org.openmrs.module.fhir2.model.FhirTask;
import org.openmrs.module.fhirExtension.model.Task;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Component
@Transactional
public interface TaskService {
	
	Task saveTask(Task task);
	
	List<Task> bulkSaveTasks(List<Task> tasks);
	
	List<Task> getTasksByVisitFilteredByTimeFrame(String visitUuid, Date startTime, Date endTime);
	
	List<Task> getTasksByPatientUuidsByTimeFrame(List<String> patientUuids, Date startTime, Date endTime);
	
	List<Task> getTasksByUuids(List<String> listOdUuids);
	
	List<Task> getTasksByNameAndStatus(List<String> taskNames, FhirTask.TaskStatus taskStatus);
}
