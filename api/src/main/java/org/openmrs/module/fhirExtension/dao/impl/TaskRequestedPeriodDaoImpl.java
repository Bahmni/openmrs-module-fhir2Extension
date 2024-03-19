package org.openmrs.module.fhirExtension.dao.impl;

import org.hibernate.SessionFactory;
import org.openmrs.module.fhirExtension.dao.TaskRequestedPeriodDao;
import org.openmrs.module.fhirExtension.model.FhirTaskRequestedPeriod;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

// TODO: This Dao is to be removed after support openmrs R5

@Repository
public class TaskRequestedPeriodDaoImpl implements TaskRequestedPeriodDao {
	
	@Autowired
	private SessionFactory sessionFactory;
	
	@Override
	public FhirTaskRequestedPeriod getTaskRequestedPeriodByTaskId(Integer taskId) {
		return null;
	}
	
	@Override
	public FhirTaskRequestedPeriod save(FhirTaskRequestedPeriod fhirTaskRequestedPeriod) {
		sessionFactory.getCurrentSession().save(fhirTaskRequestedPeriod);
		return fhirTaskRequestedPeriod;
	}
}
