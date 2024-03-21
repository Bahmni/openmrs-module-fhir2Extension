package org.openmrs.module.fhirExtension.dao.impl;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.query.Query;
import org.hibernate.transform.Transformers;
import org.openmrs.Encounter;
import org.openmrs.Visit;
import org.openmrs.module.fhir2.model.FhirReference;
import org.openmrs.module.fhir2.model.FhirTask;
import org.openmrs.module.fhirExtension.dao.TaskDao;
import org.openmrs.module.fhirExtension.model.FhirTaskRequestedPeriod;
import org.openmrs.module.fhirExtension.model.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import java.util.ArrayList;
import java.util.Collections;
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
	public List<Object> getTasksByPatientUuidsFilteredByTimeFrame(List<String> patientUuids, Date startTime, Date endTime) {
		try {
			CriteriaBuilder criteriaBuilder = sessionFactory.getCurrentSession().getCriteriaBuilder();
			CriteriaQuery<Object[]> criteriaQuery = criteriaBuilder.createQuery(Object[].class);
			Root<FhirTask> taskRoot = criteriaQuery.from(FhirTask.class);
			Join<FhirTask, FhirReference> referenceJoin = taskRoot.join("reference_id");
			Join<FhirTask, FhirTaskRequestedPeriod> requestedPeriodJoin = taskRoot.join("task_id");

			criteriaQuery.select(criteriaBuilder.array(taskRoot, referenceJoin));
			Predicate[] predicates = new Predicate[4];
			predicates[0] = criteriaBuilder.greaterThanOrEqualTo(requestedPeriodJoin.get("requested_start_time"), startTime);
			predicates[1] = criteriaBuilder.lessThanOrEqualTo(requestedPeriodJoin.get("requested_end_time"), endTime);
			predicates[2] = criteriaBuilder.equal(taskRoot.get("retired"), 0);
			predicates[3] = referenceJoin.get("target_uuid").in(patientUuids);

			criteriaQuery.where(predicates);

			Query<Object[]> query = sessionFactory.getCurrentSession().createQuery(criteriaQuery);
			return Collections.singletonList(query.getResultList());
		} catch (Exception ex) {
			System.out.println("Error " + ex);
		}
		return new ArrayList<>();
	}
}
