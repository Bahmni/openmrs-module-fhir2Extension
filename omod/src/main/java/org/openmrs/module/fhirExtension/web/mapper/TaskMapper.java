package org.openmrs.module.fhirExtension.web.mapper;

import org.openmrs.Encounter;
import org.openmrs.Patient;
import org.openmrs.Visit;
import org.openmrs.api.EncounterService;
import org.openmrs.api.LocationService;
import org.openmrs.api.PatientService;
import org.openmrs.api.VisitService;
import org.openmrs.api.context.Context;
import org.openmrs.module.fhir2.model.FhirReference;
import org.openmrs.module.fhir2.model.FhirTask;
import org.openmrs.module.fhirExtension.model.FhirTaskRequestedPeriod;
import org.openmrs.module.fhirExtension.model.Task;
import org.openmrs.module.fhirExtension.web.contract.TaskRequest;
import org.openmrs.module.fhirExtension.web.contract.TaskResponse;
import org.openmrs.module.fhirExtension.web.contract.TaskUpdateRequest;
import org.openmrs.module.webservices.rest.web.ConversionUtil;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.openmrs.parameter.EncounterSearchCriteria;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TaskMapper {
	
	@Autowired
	private EncounterService encounterService;
	
	@Autowired
	private VisitService visitService;
	
	@Autowired
	private PatientService patientService;
	
	public Task fromRequest(TaskRequest taskRequest) {
		
		Task task = new Task();
		FhirTask fhirTask = new FhirTask();
		fhirTask.setName(taskRequest.getName());
		fhirTask.setTaskCode(Context.getConceptService().getConceptByName(taskRequest.getTaskType()));
		
		if (taskRequest.getPatientUuid() != null) {
			Visit activeVisit = visitService.getActiveVisitsByPatient(
			    patientService.getPatientByUuid(taskRequest.getPatientUuid())).get(0);
			FhirReference forReference = new FhirReference();
			forReference.setType(Visit.class.getTypeName());
			forReference.setReference(Visit.class.getTypeName() + "/" + activeVisit.getUuid());
			forReference.setTargetUuid(activeVisit.getUuid());
			fhirTask.setForReference(forReference);
		} else if (taskRequest.getVisitUuid() != null) {
			FhirReference forReference = new FhirReference();
			forReference.setType(Visit.class.getTypeName());
			forReference.setReference(Visit.class.getTypeName() + "/" + taskRequest.getVisitUuid());
			forReference.setTargetUuid(taskRequest.getVisitUuid());
			fhirTask.setForReference(forReference);
		}
		
		if (taskRequest.getEncounterUuid() != null) {
			FhirReference encounterReference = new FhirReference();
			encounterReference.setType(Encounter.class.getTypeName());
			encounterReference.setReference(Encounter.class.getTypeName() + "/" + taskRequest.getEncounterUuid());
			encounterReference.setTargetUuid(taskRequest.getEncounterUuid());
			fhirTask.setEncounterReference(encounterReference);
		}
		
		fhirTask.setStatus(taskRequest.getStatus());
		fhirTask.setIntent(taskRequest.getIntent());
		fhirTask.setComment(taskRequest.getComment());
		
		if (taskRequest.getRequestedStartTime() != null || taskRequest.getRequestedEndTime() != null) {
			FhirTaskRequestedPeriod fhirTaskRequestedPeriod = new FhirTaskRequestedPeriod();
			fhirTaskRequestedPeriod.setTask(fhirTask);
			fhirTaskRequestedPeriod.setRequestedStartTime(taskRequest.getRequestedStartTime());
			fhirTaskRequestedPeriod.setRequestedEndTime(taskRequest.getRequestedEndTime());
			task.setFhirTaskRequestedPeriod(fhirTaskRequestedPeriod);
		}
		task.setFhirTask(fhirTask);
		return task;
	}
	
	public TaskResponse constructResponse(Task task) {
		TaskResponse response = new TaskResponse();
		response.setName(task.getFhirTask().getName());
		response.setUuid(task.getFhirTask().getUuid());
		response.setStatus(task.getFhirTask().getStatus());
		response.setIntent(task.getFhirTask().getIntent());
		response.setPatientUuid(task.getFhirTask().getForReference().getTargetUuid());
		response.setRequestedStartTime(task.getFhirTaskRequestedPeriod().getRequestedStartTime());
		response.setRequestedEndTime(task.getFhirTaskRequestedPeriod().getRequestedEndTime());
		response.setCreator(ConversionUtil.convertToRepresentation(task.getFhirTask().getCreator(), Representation.REF));
		response.setTaskType(ConversionUtil.convertToRepresentation(task.getFhirTask().getTaskCode(), Representation.REF));
		response.setExecutionStartTime(task.getFhirTask().getExecutionStartTime());
		response.setExecutionEndTime(task.getFhirTask().getExecutionEndTime());
		response.setComment(task.getFhirTask().getComment());
		return response;
	}
	
	public void fromRequest(TaskUpdateRequest taskUpdateRequest, Task task) {
		FhirTask fhirTask = task.getFhirTask();
		
		fhirTask.setStatus(taskUpdateRequest.getStatus());
		fhirTask.setExecutionStartTime(taskUpdateRequest.getExecutionStartTime());
		fhirTask.setExecutionEndTime(taskUpdateRequest.getExecutionEndTime());
		fhirTask.setComment(taskUpdateRequest.getComment());
	}
	
}
