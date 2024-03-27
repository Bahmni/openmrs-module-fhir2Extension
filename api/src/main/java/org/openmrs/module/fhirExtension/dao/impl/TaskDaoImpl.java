package org.openmrs.module.fhirExtension.dao.impl;

import org.hibernate.SessionFactory;
import org.openmrs.Encounter;
import org.openmrs.Patient;
import org.openmrs.Visit;
import org.openmrs.module.fhir2.model.FhirTask;
import org.openmrs.module.fhirExtension.dao.TaskDao;
import org.openmrs.module.fhirExtension.model.FhirTaskRequestedPeriod;
import org.openmrs.module.fhirExtension.model.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Repository
public class TaskDaoImpl implements TaskDao {
	
	@Autowired
	private SessionFactory sessionFactory;
	
	@Override
	public List<Task> getTasksByVisitFilteredByTimeFrame(Visit visit, Date startTime, Date endTime) {
		try {
			CriteriaBuilder criteriaBuilder = sessionFactory.getCurrentSession().getCriteriaBuilder();
			CriteriaQuery<Task> criteriaQuery = criteriaBuilder.createQuery(Task.class);
			Root<FhirTaskRequestedPeriod> fhirTaskRequestedPeriod = criteriaQuery.from(FhirTaskRequestedPeriod.class);
			Join<FhirTask, FhirTaskRequestedPeriod> fhirTaskJoin = fhirTaskRequestedPeriod.join("task");

			Subquery<Encounter> encounterSubQuery = criteriaQuery.subquery(Encounter.class);
			Root<Encounter> encounter = encounterSubQuery.from(Encounter.class);
			encounterSubQuery.select(encounter.get("uuid"));
			encounterSubQuery.where(criteriaBuilder.equal(encounter.get("visit"), visit));


			criteriaQuery.select(criteriaBuilder.construct(Task.class, fhirTaskJoin, fhirTaskRequestedPeriod));
			criteriaQuery.where(
					criteriaBuilder.and(
							criteriaBuilder.in(fhirTaskJoin.get("encounterReference").get("targetUuid")).value(encounterSubQuery),
							criteriaBuilder.between(fhirTaskRequestedPeriod.get("requestedStartTime"), startTime, endTime)
					)
			);

			TypedQuery<Task> query = sessionFactory.getCurrentSession().createQuery(criteriaQuery);

			return query.getResultList();

		}
		catch (Exception ex){
			System.out.println("Error "+ ex);
		}
		return new ArrayList<>();
	}
	
	@Override
	public List<Task> getTasksByPatientUuidsFilteredByTimeFrame(List<String> patientUuids, Date startTime, Date endTime) {
		try {
			CriteriaBuilder criteriaBuilder = sessionFactory.getCurrentSession().getCriteriaBuilder();

			CriteriaQuery<Task> criteriaQuery = criteriaBuilder.createQuery(Task.class);
			Root<FhirTaskRequestedPeriod> fhirTaskRequestedPeriod = criteriaQuery.from(FhirTaskRequestedPeriod.class);
			Join<FhirTask, FhirTaskRequestedPeriod> fhirTaskJoin = fhirTaskRequestedPeriod.join("task");

			Subquery<String> encounterSubQuery = criteriaQuery.subquery(String.class);
			Root<Encounter> encounterRoot = encounterSubQuery.from(Encounter.class);
			Join<Encounter, Visit> visitJoin = encounterRoot.join("visit");
			Join<Visit, Patient> patientJoin = visitJoin.join("patient");
			encounterSubQuery.select(encounterRoot.get("uuid"));
			encounterSubQuery.where(criteriaBuilder.in(patientJoin.get("uuid")).value(patientUuids));

			criteriaQuery.select(criteriaBuilder.construct(Task.class, fhirTaskJoin, fhirTaskRequestedPeriod));
			criteriaQuery.where(
					criteriaBuilder.and(
							criteriaBuilder.in(fhirTaskJoin.get("encounterReference").get("targetUuid")).value(encounterSubQuery),
							criteriaBuilder.between(fhirTaskRequestedPeriod.get("requestedStartTime"), startTime, endTime)
					)
			);

			TypedQuery<Task> query = sessionFactory.getCurrentSession().createQuery(criteriaQuery);

			return query.getResultList();

		}
		catch (Exception ex){
			System.out.println("Error "+ ex);
		}
		return new ArrayList<>();
	}
}
