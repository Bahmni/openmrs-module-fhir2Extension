package org.openmrs.module.fhirExtension.service.impl;

import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openmrs.api.VisitService;
import org.openmrs.module.fhir2.api.dao.FhirTaskDao;
import org.openmrs.module.fhir2.model.FhirTask;
import org.openmrs.module.fhirExtension.dao.TaskDao;
import org.openmrs.module.fhirExtension.dao.TaskRequestedPeriodDao;
import org.openmrs.module.fhirExtension.model.FhirTaskRequestedPeriod;
import org.openmrs.module.fhirExtension.model.Task;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.mockito.Mockito.*;

public class TaskServiceImplTest {
	
	@Mock
	private FhirTaskDao fhirTaskDao;
	
	@Mock
	private VisitService visitService;
	
	@Mock
	private TaskDao taskDao;
	
	@Mock
	private TaskRequestedPeriodDao taskRequestedPeriodDao;
	
	@InjectMocks
	private TaskServiceImpl taskService;
	
	public TaskServiceImplTest() {
		MockitoAnnotations.initMocks(this);
	}
	
	@Test
	public void testSaveTask() {
		FhirTask fhirTask = new FhirTask();
		FhirTaskRequestedPeriod requestedPeriod = new FhirTaskRequestedPeriod();
		requestedPeriod.setRequestedStartTime(new Date());
		Task task = new Task(fhirTask, requestedPeriod);
		
		when(fhirTaskDao.createOrUpdate(any())).thenReturn(task.getFhirTask());
		when(taskRequestedPeriodDao.save(any())).thenReturn(task.getFhirTaskRequestedPeriod());
		
		Task savedTask = taskService.saveTask(task);
		
		verify(fhirTaskDao, times(1)).createOrUpdate(any());
		verify(taskRequestedPeriodDao, times(1)).save(any());
	}
	
	@Test
    public void testGetTasksByVisitFilteredByTimeFrame() {
        String visitUuid = "sample-visit-uuid";
        Date startTime = new Date();
        Date endTime = new Date();

        List<Task> tasks = new ArrayList<>();

        when(visitService.getVisitByUuid(visitUuid)).thenReturn(null); // Mock return value of VisitService
        when(taskDao.getTasksByVisitFilteredByTimeFrame(any(), any(), any())).thenReturn(tasks);

        List<Task> result = taskService.getTasksByVisitFilteredByTimeFrame(visitUuid, startTime, endTime);

        verify(taskDao, times(1)).getTasksByVisitFilteredByTimeFrame(any(), any(), any());
    }
	
	@Test
    public void testGetTasksByPatientUuidsByTimeFrame() {
        List<String> patientUuids = new ArrayList<>();
        Date startTime = new Date();
        Date endTime = new Date();

        List<Task> tasks = new ArrayList<>();

        when(taskDao.getTasksByPatientUuidsFilteredByTimeFrame(any(), any(), any())).thenReturn(tasks);

        List<Task> result = taskService.getTasksByPatientUuidsByTimeFrame(patientUuids, startTime, endTime);

        verify(taskDao, times(1)).getTasksByPatientUuidsFilteredByTimeFrame(any(), any(), any());
    }
}
