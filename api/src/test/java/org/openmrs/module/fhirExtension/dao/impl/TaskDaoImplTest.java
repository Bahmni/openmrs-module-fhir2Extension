package org.openmrs.module.fhirExtension.dao.impl;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.Visit;
import org.openmrs.module.fhir2.model.FhirTask;
import org.openmrs.module.fhirExtension.model.FhirTaskRequestedPeriod;
import org.openmrs.module.fhirExtension.model.Task;
import org.openmrs.module.fhirExtension.model.TaskSearchRequest;
import javax.persistence.criteria.*;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class TaskDaoImplTest {
	
	@Mock
	private SessionFactory sessionFactory;
	
	@Mock
	private Session session;
	
	@Mock
	private CriteriaBuilder criteriaBuilder;
	
	@Mock
	private CriteriaQuery<Task> criteriaQuery;
	
	@Mock
	private Root<FhirTaskRequestedPeriod> root;
	
	@Mock
	private Join join;
	
	@Mock
	private Query typedQuery;
	
	@InjectMocks
	private TaskDaoImpl taskDao;
	
	@Before
	public void setUp() {
		MockitoAnnotations.openMocks(this);
		when(sessionFactory.getCurrentSession()).thenReturn(session);
		when(session.getCriteriaBuilder()).thenReturn(criteriaBuilder);
		when(criteriaBuilder.createQuery(Task.class)).thenReturn(criteriaQuery);
		when(criteriaQuery.from(FhirTaskRequestedPeriod.class)).thenReturn(root);
		when(root.join("task")).thenReturn(join);
		when(session.createQuery(criteriaQuery)).thenReturn(typedQuery);
	}
	
	@Test
	public void testGetTasksByVisitFilteredByTimeFrame() {
		Visit visit = new Visit(123);
		visit.setUuid("test-uuid");
		Date startTime = new Date();
		Date endTime = new Date();
		
		CompoundSelection<Task> compoundSelection = mock(CompoundSelection.class);
		Path visitUuidPath = mock(Path.class);
		Path requestedPeriodPath = mock(Path.class);
		Path forReferencePath = mock(Path.class);
		
		Predicate predicate1 = mock(Predicate.class);
		Predicate predicate2 = mock(Predicate.class);
		Predicate combinedPredicate = mock(Predicate.class);
		
		when(criteriaBuilder.construct(Task.class, join, root)).thenReturn(compoundSelection);
		when(root.get("requestedStartTime")).thenReturn(requestedPeriodPath);
		when(join.get("forReference")).thenReturn(forReferencePath);
		when(forReferencePath.get("targetUuid")).thenReturn(visitUuidPath);
		when(criteriaBuilder.equal(visitUuidPath, visit.getUuid())).thenReturn(predicate1);
		when(criteriaBuilder.between(requestedPeriodPath, startTime, endTime)).thenReturn(predicate2);
		when(criteriaBuilder.and(predicate1, predicate2)).thenReturn(combinedPredicate);
		when(criteriaQuery.where(combinedPredicate)).thenReturn(criteriaQuery);
		
		List<Task> expectedTasks = Arrays.asList(new Task());
		when(typedQuery.getResultList()).thenReturn(expectedTasks);
		
		List<Task> result = taskDao.getTasksByVisitFilteredByTimeFrame(visit, startTime, endTime);
		
		assertEquals(expectedTasks, result);
	}
	
	@Test
	public void testGetTasksByVisitFilteredByTimeFrameShouldReturnEmptyListForInvalidVisit() {
		Date startTime = new Date();
		Date endTime = new Date();

		CompoundSelection<Task> compoundSelection = mock(CompoundSelection.class);
		Path visitUuidPath = mock(Path.class);
		Path forReferencePath = mock(Path.class);

		when(criteriaBuilder.construct(Task.class, join, root)).thenReturn(compoundSelection);
		when(join.get("forReference")).thenReturn(forReferencePath);
		when(forReferencePath.get("targetUuid")).thenReturn(visitUuidPath);

		List<Task> expectedTasks = new ArrayList<>();
		List<Task> result = taskDao.getTasksByVisitFilteredByTimeFrame(null, startTime, endTime);

		assertEquals(expectedTasks, result);
	}
	
	@Test
	public void testGetTasksByPatientUuidsFilteredByTimeFrame_validPatientUuidsAndTimeFrame() {
		List<String> patientUuids = Arrays.asList("patient-uuid-1", "patient-uuid-2");
		Date startTime = new Date();
		Date endTime = new Date();
		
		Subquery<String> visitSubQuery = mock(Subquery.class);
		Root visitRoot = mock(Root.class);
		Path patientUuidPath = mock(Path.class);
		Path stopDatetimePath = mock(Path.class);
		Path requestedPeriodPath = mock(Path.class);
		Path forReferencePath = mock(Path.class);
		Path forReferenceTargetUuidPath = mock(Path.class);
		
		CriteriaBuilder.In<String> patientUuidIn = mock(CriteriaBuilder.In.class);
		Predicate isNullPredicate = mock(Predicate.class);
		CriteriaBuilder.In forReferenceIn = mock(CriteriaBuilder.In.class);
		Predicate predicate2 = mock(Predicate.class);
		
		when(criteriaQuery.subquery(String.class)).thenReturn(visitSubQuery);
		when(visitSubQuery.from(Visit.class)).thenReturn(visitRoot);
		when(visitRoot.join("patient")).thenReturn(join);
		when(join.get("uuid")).thenReturn(patientUuidPath);
		when(criteriaBuilder.in(patientUuidPath)).thenReturn(patientUuidIn);
		when(visitRoot.get("stopDatetime")).thenReturn(stopDatetimePath);
		when(criteriaBuilder.isNull(stopDatetimePath)).thenReturn(isNullPredicate);
		
		when(visitSubQuery.select(visitRoot.get("uuid"))).thenReturn(visitSubQuery);
		
		when(join.get("forReference")).thenReturn(forReferencePath);
		when(forReferencePath.get("targetUuid")).thenReturn(forReferenceTargetUuidPath);
		when(criteriaBuilder.in(forReferenceTargetUuidPath)).thenReturn(forReferenceIn);
		
		when(root.get("requestedStartTime")).thenReturn(requestedPeriodPath);
		when(criteriaBuilder.between(requestedPeriodPath, startTime, endTime)).thenReturn(predicate2);
		
		List<Task> expectedTasks = Arrays.asList(new Task());
		when(typedQuery.getResultList()).thenReturn(expectedTasks);
		
		List<Task> result = taskDao.getTasksByPatientUuidsFilteredByTimeFrame(patientUuids, startTime, endTime);
		
		assertEquals(expectedTasks, result);
	}
	
	@Test
	public void testGetTasksByPatientUuidsFilteredByTimeFrameShouldReturnEmptyListForInvalidPatient() {
		Date startTime = new Date();
		Date endTime = new Date();

		List<Task> expectedTasks = new ArrayList<>();

		List<Task> result = taskDao.getTasksByPatientUuidsFilteredByTimeFrame(null, startTime, endTime);

		assertEquals(expectedTasks, result);
	}
	
	@Test
	public void testGetTasksByUuidsValidUuids() {
		List listOfUuids = Arrays.asList(UUID.randomUUID().toString());
		
		Path<String> uuidPath = mock(Path.class);
		
		when(join.get("uuid")).thenReturn(uuidPath);
		when(criteriaQuery.select(criteriaBuilder.construct(Task.class, join, root))).thenReturn(criteriaQuery);
		
		List<Task> expectedTasks = Arrays.asList(new Task());
		when(typedQuery.getResultList()).thenReturn(expectedTasks);
		
		List<Task> result = taskDao.getTasksByUuids(listOfUuids);
		
		assertEquals(expectedTasks, result);
	}
	
	@Test
	public void testSearchTasksWithName() {
		TaskSearchRequest taskSearchRequest = new TaskSearchRequest();
		taskSearchRequest.setTaskName(Arrays.asList("Task1", "Task2"));
		
		Path namePath = mock(Path.class);
		
		when(join.get("name")).thenReturn(namePath);
		when(criteriaQuery.select(criteriaBuilder.construct(Task.class, join, root))).thenReturn(criteriaQuery);
		
		List<Task> expectedTasks = Arrays.asList(new Task(), new Task());
		when(typedQuery.getResultList()).thenReturn(expectedTasks);
		
		List<Task> result = taskDao.searchTasks(taskSearchRequest);
		
		assertEquals(expectedTasks, result);
	}
	
	@Test
	public void testSearchTasksWithStatus() {
		TaskSearchRequest taskSearchRequest = new TaskSearchRequest();
		taskSearchRequest.setTaskStatus(Arrays.asList(FhirTask.TaskStatus.COMPLETED, FhirTask.TaskStatus.REJECTED));
		
		Path statusPath = mock(Path.class);
		
		when(join.get("status")).thenReturn(statusPath);
		when(criteriaQuery.select(criteriaBuilder.construct(Task.class, join, root))).thenReturn(criteriaQuery);
		
		List<Task> expectedTasks = Arrays.asList(new Task(), new Task());
		when(typedQuery.getResultList()).thenReturn(expectedTasks);
		
		List<Task> result = taskDao.searchTasks(taskSearchRequest);
		
		assertEquals(expectedTasks, result);
	}
	
	@Test
	public void testSearchTasksShouldReturnEmptyListWithEmptyListSearchParams() {
		TaskSearchRequest taskSearchRequest = new TaskSearchRequest();
		taskSearchRequest.setTaskName(new ArrayList<>());
		taskSearchRequest.setTaskStatus(new ArrayList<>());

		when(criteriaQuery.select(criteriaBuilder.construct(Task.class, join, root))).thenReturn(criteriaQuery);

		List<Task> expectedTasks = new ArrayList<>();
		when(typedQuery.getResultList()).thenReturn(expectedTasks);

		List<Task> result = taskDao.searchTasks(taskSearchRequest);

		assertEquals(expectedTasks, result);
	}
	
	@Test
	public void testSave() {
		List<FhirTask> tasks = Arrays.asList(new FhirTask(), new FhirTask());
		
		taskDao.save(tasks);
		
		verify(session, times(2)).persist(any(FhirTask.class));
		verify(session).flush();
	}
}
