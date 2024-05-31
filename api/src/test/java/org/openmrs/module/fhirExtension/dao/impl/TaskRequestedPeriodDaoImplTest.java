package org.openmrs.module.fhirExtension.dao.impl;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.module.fhirExtension.model.FhirTaskRequestedPeriod;

import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class TaskRequestedPeriodDaoImplTest {
	
	@Mock
	private SessionFactory sessionFactory;
	
	@Mock
	private Session session;
	
	@InjectMocks
	private TaskRequestedPeriodDaoImpl taskRequestedPeriodDao;
	
	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		when(sessionFactory.getCurrentSession()).thenReturn(session);
	}
	
	@Test
	public void testSave() {
		FhirTaskRequestedPeriod fhirTaskRequestedPeriod = new FhirTaskRequestedPeriod();
		
		taskRequestedPeriodDao.save(fhirTaskRequestedPeriod);
		
		verify(session).save(fhirTaskRequestedPeriod);
	}
	
	@Test
	public void testSaveList() {
		FhirTaskRequestedPeriod fhirTaskRequestedPeriod1 = new FhirTaskRequestedPeriod();
		FhirTaskRequestedPeriod fhirTaskRequestedPeriod2 = new FhirTaskRequestedPeriod();
		List<FhirTaskRequestedPeriod> fhirTaskRequestedPeriods = Arrays.asList(fhirTaskRequestedPeriod1,
		    fhirTaskRequestedPeriod2);
		
		taskRequestedPeriodDao.save(fhirTaskRequestedPeriods);
		
		verify(session, times(2)).persist(any(FhirTaskRequestedPeriod.class));
		verify(session).flush();
	}
	
	@Test
	public void testUpdateList() {
		FhirTaskRequestedPeriod fhirTaskRequestedPeriod1 = new FhirTaskRequestedPeriod();
		FhirTaskRequestedPeriod fhirTaskRequestedPeriod2 = new FhirTaskRequestedPeriod();
		List<FhirTaskRequestedPeriod> fhirTaskRequestedPeriods = Arrays.asList(fhirTaskRequestedPeriod1,
		    fhirTaskRequestedPeriod2);
		
		taskRequestedPeriodDao.update(fhirTaskRequestedPeriods);
		
		verify(session, times(2)).merge(any(FhirTaskRequestedPeriod.class));
	}
}
