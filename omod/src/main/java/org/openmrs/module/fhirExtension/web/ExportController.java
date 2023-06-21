package org.openmrs.module.fhirExtension.web;

import org.openmrs.module.fhir2.model.FhirTask;
import org.openmrs.module.fhirExtension.service.ExportTask;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.v1_0.controller.BaseRestController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.List;

@Controller
@RequestMapping(value = "/rest/" + RestConstants.VERSION_1 + "/export")
public class ExportController extends BaseRestController {
	
	public static final String FHIR2_R4_TASK_URI = "/ws/fhir2/R4/Task/";
	
	@Autowired
	private ExportTask exportTask;
	
	@RequestMapping(method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity export(
	        @RequestParam(value = "exportType", required = false, defaultValue = "NDJSON") String exportType,
	        @RequestParam(value = "startDate", required = false) String startDate,
	        @RequestParam(value = "endDate", required = false) String endDate,
	        @RequestParam(value = "resourceType", required = false) List<String> resourceTypes) {
		FhirTask task = exportTask.getInitialTaskResponse();
		return new ResponseEntity(getFhirTaskUri(task), HttpStatus.ACCEPTED);
	}
	
	private String getFhirTaskUri(FhirTask task) {
		return ServletUriComponentsBuilder.fromCurrentContextPath().path(FHIR2_R4_TASK_URI).path(task.getUuid()).build()
		        .toUriString();
	}
	
}
