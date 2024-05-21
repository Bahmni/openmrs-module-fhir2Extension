package org.openmrs.module.fhirExtension.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.module.fhir2.model.FhirTask;
import java.util.Date;

public class FhirTaskRequestedPeriodTest {
	
	private FhirTaskRequestedPeriod period1;
	
	private FhirTaskRequestedPeriod period2;
	
	private FhirTaskRequestedPeriod period3;
	
	private FhirTask task1;
	
	private FhirTask task2;
	
	@Before
	public void setUp() {
		task1 = new FhirTask();
		task1.setId(1);
		
		task2 = new FhirTask();
		task2.setId(2);
		
		period1 = new FhirTaskRequestedPeriod();
		period1.setTask(task1);
		period1.setRequestedStartTime(new Date(1625072400000L)); // 30 Jun 2021 10:00:00 GMT
		period1.setRequestedEndTime(new Date(1625158800000L)); // 01 Jul 2021 10:00:00 GMT
		
		period2 = new FhirTaskRequestedPeriod();
		period2.setTask(task1);
		period2.setRequestedStartTime(new Date(1625072400000L));
		period2.setRequestedEndTime(new Date(1625158800000L));
		
		period3 = new FhirTaskRequestedPeriod();
		period3.setTask(task2);
		period3.setRequestedStartTime(new Date(1635073400000L));
		period3.setRequestedEndTime(new Date(1635158900000L));
	}
	
	@Test
	public void testEquals_sameObject() {
		assertEquals(period1, period1);
	}
	
	@Test
	public void testEquals_equalObjects() {
		assertEquals(period1, period2);
	}
	
	@Test
	public void testHashCodeEqualObjects() {
		assertEquals(period1.hashCode(), period2.hashCode());
	}
	
	@Test
	public void testNotEqualsDifferentType() {
		assertNotEquals(period1, task1);
	}
	
	@Test
	public void testNotEqualsNull() {
		assertNotEquals(period1, null);
	}
}
