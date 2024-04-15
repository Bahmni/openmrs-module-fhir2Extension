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

			criteriaQuery.select(criteriaBuilder.construct(Task.class, fhirTaskJoin, fhirTaskRequestedPeriod));
			criteriaQuery.where(
					criteriaBuilder.and(
							criteriaBuilder.equal(fhirTaskJoin.get("forReference").get("targetUuid"),visit.getUuid()),
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

			Subquery<String> visitSubQuery = criteriaQuery.subquery(String.class);
			Root<Visit> visitRoot = visitSubQuery.from(Visit.class);
			Join<Visit, Patient> patientJoin = visitRoot.join("patient");
			visitSubQuery.select(visitRoot.get("uuid"));
			visitSubQuery.where(
					criteriaBuilder.and(
							criteriaBuilder.in(patientJoin.get("uuid")).value(patientUuids),
							criteriaBuilder.isNull(visitRoot.get("stopDateTime"))
					)
			);

			criteriaQuery.select(criteriaBuilder.construct(Task.class, fhirTaskJoin, fhirTaskRequestedPeriod));
			criteriaQuery.where(
					criteriaBuilder.and(
							criteriaBuilder.in(fhirTaskJoin.get("encounterReference").get("targetUuid")).value(visitSubQuery),
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
	public List<Task> getTasksByUuids(List<String> listOfUuids) {
		try {
			CriteriaBuilder criteriaBuilder = sessionFactory.getCurrentSession().getCriteriaBuilder();
			CriteriaQuery<Task> criteriaQuery = criteriaBuilder.createQuery(Task.class);
			Root<FhirTaskRequestedPeriod> fhirTaskRequestedPeriod = criteriaQuery.from(FhirTaskRequestedPeriod.class);
			Join<FhirTask, FhirTaskRequestedPeriod> fhirTaskJoin = fhirTaskRequestedPeriod.join("task");
			
			criteriaQuery.select(criteriaBuilder.construct(Task.class, fhirTaskJoin, fhirTaskRequestedPeriod)).where(
			    fhirTaskJoin.get("uuid").in(listOfUuids));
			TypedQuery<Task> query = sessionFactory.getCurrentSession().createQuery(criteriaQuery);
			
			return query.getResultList();
		}
		catch (Exception e) {
			System.out.println("Error " + e);
		}
		return null;
	}
	
	@Override
	public List<Task> searchTasks(List<String> taskNames, FhirTask.TaskStatus taskStatus) {
		try {
			CriteriaBuilder criteriaBuilder = sessionFactory.getCurrentSession().getCriteriaBuilder();
			CriteriaQuery<Task> criteriaQuery = criteriaBuilder.createQuery(Task.class);
			Root<FhirTaskRequestedPeriod> fhirTaskRequestedPeriod = criteriaQuery.from(FhirTaskRequestedPeriod.class);
			Join<FhirTask, FhirTaskRequestedPeriod> fhirTaskJoin = fhirTaskRequestedPeriod.join("task");
			criteriaQuery.select(criteriaBuilder.construct(Task.class, fhirTaskJoin, fhirTaskRequestedPeriod)).where(
			    fhirTaskJoin.get("name").in(taskNames), criteriaBuilder.equal(fhirTaskJoin.get("status"), taskStatus));
			TypedQuery<Task> query = sessionFactory.getCurrentSession().createQuery(criteriaQuery);
			return query.getResultList();
		}
		catch (Exception e) {
			System.out.println("Error " + e);
		}
		return null;
	}
	
	public List<FhirTask> save(List<FhirTask> tasks) {
		tasks.forEach(task -> {
			sessionFactory.getCurrentSession().persist(task);
		});
		sessionFactory.getCurrentSession().flush();
		return tasks;
	}
}
