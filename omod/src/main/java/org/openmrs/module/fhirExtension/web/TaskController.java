package org.openmrs.module.fhirExtension.web;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.PatientService;
import org.openmrs.api.VisitService;
import org.openmrs.module.fhirExtension.model.Task;
import org.openmrs.module.fhirExtension.service.TaskService;
import org.openmrs.module.fhirExtension.web.contract.TaskRequest;
import org.openmrs.module.fhirExtension.web.contract.TaskResponse;
import org.openmrs.module.fhirExtension.web.mapper.TaskMapper;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.RestUtil;
import org.openmrs.module.webservices.rest.web.v1_0.controller.BaseRestController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

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
	
	@RequestMapping(method = RequestMethod.GET, params = {"startTime", "endTime"})
	@ResponseBody
	public ResponseEntity<Object> getSlotsForPatientsAndTime(@RequestParam(value = "startTime") Long startTime,
															 @RequestParam(value = "endTime") Long endTime,
															 @RequestParam(value = "visitUuid", required = false) String visitUuid,
															 @RequestParam(value = "patientUuids", required = false) List<String> patientUuids) throws IOException {
		try {
			if ((patientUuids == null || patientUuids.isEmpty()) && !(visitUuid == null || visitUuid.isEmpty())) {
				List<Task> tasks = taskService.getTasksByVisitFilteredByTimeFrame(visitUuid, new Date(TimeUnit.SECONDS.toMillis(startTime)), new Date(TimeUnit.SECONDS.toMillis(endTime)));
				return new ResponseEntity<>(tasks.stream().map(taskMapper::constructResponse).collect(Collectors.toList()), HttpStatus.OK);
			} else if ((visitUuid == null || visitUuid.isEmpty()) && !(patientUuids == null || patientUuids.isEmpty())) {
				Map<String, List<TaskResponse>> groupedResponses = constructGroupedResponses(patientUuids, new Date(TimeUnit.SECONDS.toMillis(startTime)), new Date(TimeUnit.SECONDS.toMillis(endTime)));
				return new ResponseEntity<>(groupedResponses, HttpStatus.OK);
			}
			else {
				throw new Exception();
			}
		} catch (Exception e) {
			log.error("Runtime error while fetching patient medication summaries", e);
			return new ResponseEntity<>(RestUtil.wrapErrorResponse(e, e.getMessage()), HttpStatus.BAD_REQUEST);
		}
	}
	
	private Map<String, List<TaskResponse>> constructGroupedResponses(List<String> patientUuids, Date startTime, Date endTime) {
		List<Task> response = taskService.getTasksByPatientUuidsByTimeFrame(patientUuids, startTime, endTime);
		Map<String, List<Task>> groupedResponses = response.stream()
				.collect(Collectors.groupingBy(task -> task.getFhirTask().getForReference().getTargetUuid()));

		Map<String, List<TaskResponse>> groupedTaskResponses = new HashMap<>();
		groupedResponses.forEach((uuid, tasks) -> {
			List<TaskResponse> taskResponses = tasks.stream().map(taskMapper::constructResponse).collect(Collectors.toList());
			groupedTaskResponses.put(uuid, taskResponses);
		});
		return groupedTaskResponses;
	}
}
