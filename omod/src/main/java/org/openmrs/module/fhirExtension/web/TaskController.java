package org.openmrs.module.fhirExtension.web;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Patient;
import org.openmrs.Visit;
import org.openmrs.api.PatientService;
import org.openmrs.api.VisitService;
import org.openmrs.api.context.Context;
import org.openmrs.module.fhir2.model.FhirTask;
import org.openmrs.module.fhirExtension.model.Task;
import org.openmrs.module.fhirExtension.service.TaskService;
import org.openmrs.module.fhirExtension.web.contract.TaskRequest;
import org.openmrs.module.fhirExtension.web.mapper.TaskMapper;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.RestUtil;
import org.openmrs.module.webservices.rest.web.v1_0.controller.BaseRestController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.validation.Valid;
import java.io.IOException;
import java.lang.reflect.Array;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.OK;

@Controller
@RequestMapping(value = "/rest/" + RestConstants.VERSION_1 + "/tasks")
public class TaskController extends BaseRestController {
	
	@Autowired
	private TaskService taskService;
	
	@Autowired
	private VisitService visitService;
	
	@Autowired
	private PatientService patientService;
	
	@Autowired
	private TaskMapper taskMapper;
	
	private Log log = LogFactory.getLog(this.getClass());
	
	@RequestMapping(method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<Object> saveTask(@Valid @RequestBody TaskRequest taskRequest) throws IOException {
		try {
			Task task = taskMapper.fromRequest(taskRequest);
			taskService.saveTask(task);
			return new ResponseEntity<>(taskMapper.constructResponse(task), HttpStatus.OK);
		}
		catch (RuntimeException ex){
			log.error("Runtime error while trying to create new task", ex);
			return new ResponseEntity<>(RestUtil.wrapErrorResponse(ex, ex.getMessage()), HttpStatus.BAD_REQUEST);
		}
	}
	
	@RequestMapping(method = RequestMethod.GET, params = {"visitUuid", "startTime", "endTime", "patientIds"})
	@ResponseBody
	public ResponseEntity<Object> getSlotsForPatientsAndTime(@RequestParam(value = "visitUuid", required = false) String visitUuid,
															 @RequestParam(value = "startTime") Long startTime,
															 @RequestParam(value = "endTime") Long endTime,
															 @RequestParam(value = "patientIds", required = false) List<String> patientIds) throws IOException{
		try {
			if((patientIds == null || patientIds.isEmpty()) && !(visitUuid == null || visitUuid.isEmpty())) {
				List<Task> tasks = taskService.getTasksByVisitFilteredByTimeFrame(visitUuid,new Date(startTime),new Date(endTime));
				return new ResponseEntity<>(tasks.stream().map(taskMapper::constructResponse).collect(Collectors.toList()), HttpStatus.OK);
			}
			else if ((visitUuid == null || visitUuid.isEmpty()) && !(patientIds == null || patientIds.isEmpty())){
				for (String patientId: patientIds) {
					Patient patient = patientService.getPatientByUuid(patientId);
					List<Visit> visit = visitService.getActiveVisitsByPatient(patient);
					System.out.println(visit);
					List<Task> tasks = taskService.getTasksByVisitFilteredByTimeFrame(visit.get(0).getUuid(),new Date(startTime),new Date(endTime));
				}
				return null;
			}

		} catch (Exception e) {
			log.error("Runtime error while fetching patient medication summaries", e);
			return new ResponseEntity<>(RestUtil.wrapErrorResponse(e, e.getMessage()), BAD_REQUEST);
		}
		return null;
	}
}
