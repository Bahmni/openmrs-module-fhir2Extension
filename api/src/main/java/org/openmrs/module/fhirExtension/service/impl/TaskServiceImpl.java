package org.openmrs.module.fhirExtension.service.impl;

import org.openmrs.api.VisitService;
import org.openmrs.module.fhir2.api.dao.FhirTaskDao;
import org.openmrs.module.fhirExtension.dao.TaskDao;
import org.openmrs.module.fhirExtension.dao.TaskRequestedPeriodDao;
import org.openmrs.module.fhirExtension.model.Task;
import org.openmrs.module.fhirExtension.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Transactional
@Component
public class TaskServiceImpl implements TaskService {
	
	@Autowired
	private FhirTaskDao fhirTaskDao;
	
	@Autowired
	private VisitService visitService;
	
	@Autowired
	private TaskDao taskDao;
	
	@Autowired
	private TaskRequestedPeriodDao taskRequestedPeriodDao;
	
	@Override
	public Task saveTask(Task task) {
		fhirTaskDao.createOrUpdate(task.getFhirTask());
		if (task.getFhirTaskRequestedPeriod() != null) {
			taskRequestedPeriodDao.save(task.getFhirTaskRequestedPeriod());
		}
		return task;
	}
	
	@Override
	public List<Task> getTasksByVisitFilteredByTimeFrame(String visitUuid, Date startTime, Date endTime) {
		return taskDao.getTasksByVisitFilteredByTimeFrame(visitService.getVisitByUuid(visitUuid), startTime, endTime);
	}

	@Override
	public List<Task> getTasksByPatientUuidsByTimeFrame(List<String> patientUuids, Date startTime, Date endTime) {
		return taskDao.getTasksByPatientUuidsFilteredByTimeFrame(patientUuids, startTime, endTime);
	}

	@Override
	public List<Task> getTaskByUuid(List<String> listOdUuids) {
		return taskDao.getTaskByUuid(listOdUuids);
	}
}
