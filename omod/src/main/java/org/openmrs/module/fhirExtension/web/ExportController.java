package org.openmrs.module.fhirExtension.web;

import org.openmrs.api.context.Context;
import org.openmrs.module.fhir2.model.FhirTask;
import org.openmrs.module.fhirExtension.service.ExportAsyncService;
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

@Controller
@RequestMapping(value = "/rest/" + RestConstants.VERSION_1 + "/export")
public class ExportController extends BaseRestController {
	
	public static final String FHIR2_R4_TASK_URI = "/ws/fhir2/R4/Task/";
	
	public static final String FILE_DOWNLOAD_URI = "/ws/rest/v1/fhirExtension/export";
	
	private ExportTask exportTask;
	
	private ExportAsyncService exportAsyncService;
	
	@Autowired
	public ExportController(ExportTask exportTask, ExportAsyncService exportAsyncService) {
		this.exportTask = exportTask;
		this.exportAsyncService = exportAsyncService;
	}
	
	@RequestMapping(method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity export(@RequestParam(value = "startDate", required = false) String startDate,
	        @RequestParam(value = "endDate", required = false) String endDate) {
		FhirTask fhirTask = exportTask.getInitialTaskResponse();
		exportAsyncService.export(fhirTask, startDate, endDate, Context.getUserContext(), ServletUriComponentsBuilder
		        .fromCurrentContextPath().toUriString() + FILE_DOWNLOAD_URI);
		return new ResponseEntity(getFhirTaskUri(fhirTask), HttpStatus.ACCEPTED);
	}
	
	private String getFhirTaskUri(FhirTask task) {
		return ServletUriComponentsBuilder.fromCurrentContextPath().path(FHIR2_R4_TASK_URI).path(task.getUuid()).build()
		        .toUriString();
	}
	
}
