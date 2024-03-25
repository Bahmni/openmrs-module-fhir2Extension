package org.openmrs.module.fhirExtension.dao.impl;

import org.hibernate.SessionFactory;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openmrs.Visit;
import org.openmrs.module.fhir2.model.FhirTask;
import org.openmrs.module.fhirExtension.dao.TaskDao;
import org.openmrs.module.fhirExtension.model.FhirTaskRequestedPeriod;
import org.openmrs.module.fhirExtension.model.Task;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.when;

public class TaskDaoImplTest {
	
	@Mock
	private SessionFactory sessionFactory;
	
	@Mock
	private TaskDao taskDao;
	
	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
	}
	
	@Test
    public void testGetTasksByVisitFilteredByTimeFrame() {
        // Prepare test data
        Visit visit = new Visit();
        Date startTime = new Date();
        Date endTime = new Date();

        Task task = new Task();
        FhirTask fhirTask = mock(FhirTask.class);
        when(fhirTask.getName()).thenReturn("Sample Task");
        when(fhirTask.getUuid()).thenReturn("sample-uuid");
        FhirTaskRequestedPeriod fhirTaskRequestedPeriod = mock(FhirTaskRequestedPeriod.class);
        task.setFhirTask(fhirTask);
        task.setFhirTaskRequestedPeriod(fhirTaskRequestedPeriod);
        List<Task> expectedTasks = new ArrayList<>();
        expectedTasks.add(task);

        when(taskDao.getTasksByVisitFilteredByTimeFrame(any(Visit.class), any(Date.class), any(Date.class)))
                .thenReturn(expectedTasks);

        List<Task> actualTasks = taskDao.getTasksByVisitFilteredByTimeFrame(visit, startTime, endTime);

        verify(taskDao).getTasksByVisitFilteredByTimeFrame(visit, startTime, endTime);

        assertEquals(expectedTasks, actualTasks);
    }
	
	@Test
    public void testGetTasksByPatientUuidsFilteredByTimeFrame() {
        List<String> patientUuids = new ArrayList<>();
        Date startTime = new Date();
        Date endTime = new Date();

        Task task = new Task();
        FhirTask fhirTask = mock(FhirTask.class);
        when(fhirTask.getName()).thenReturn("Sample Task");
        when(fhirTask.getUuid()).thenReturn("sample-uuid");
        FhirTaskRequestedPeriod fhirTaskRequestedPeriod = mock(FhirTaskRequestedPeriod.class);
        task.setFhirTask(fhirTask);
        task.setFhirTaskRequestedPeriod(fhirTaskRequestedPeriod);
        List<Task> expectedTasks = new ArrayList<>();
        expectedTasks.add(task);

        when(taskDao.getTasksByPatientUuidsFilteredByTimeFrame(anyList(), any(Date.class), any(Date.class)))
                .thenReturn(expectedTasks);

        List<Task> actualTasks = taskDao.getTasksByPatientUuidsFilteredByTimeFrame(patientUuids, startTime, endTime);

        verify(taskDao).getTasksByPatientUuidsFilteredByTimeFrame(patientUuids, startTime, endTime);

        assertEquals(expectedTasks, actualTasks);
    }
}
