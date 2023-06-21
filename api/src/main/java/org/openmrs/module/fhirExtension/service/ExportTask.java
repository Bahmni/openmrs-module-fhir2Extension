package org.openmrs.module.fhirExtension.service;

import lombok.extern.log4j.Log4j2;
import org.openmrs.api.ConceptService;
import org.openmrs.api.context.Context;
import org.openmrs.module.fhir2.api.dao.FhirTaskDao;
import org.openmrs.module.fhir2.model.FhirTask;
import org.openmrs.module.fhir2.model.FhirTaskOutput;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;

@Component
@Log4j2
public class ExportTask {
	
	private FhirTaskDao fhirTaskDao;
	
	private ConceptService conceptService;
	
	@Autowired
	public ExportTask(FhirTaskDao fhirTaskDao, ConceptService conceptService) {
		this.fhirTaskDao = fhirTaskDao;
		this.conceptService = conceptService;
	}
	
	public FhirTask getInitialTaskResponse() {
		FhirTask fhirTask = new FhirTask();
		fhirTask.setStatus(FhirTask.TaskStatus.ACCEPTED);
		String logMessage = "Patient Data Export by " + Context.getAuthenticatedUser().getUsername();
		fhirTask.setName(logMessage);
		log.info(logMessage);
		fhirTask.setIntent(FhirTask.TaskIntent.ORDER);
		
		FhirTaskOutput fhirTaskOutput = new FhirTaskOutput();
		fhirTaskOutput.setName("Download Link Name");
		fhirTaskOutput.setTask(fhirTask);
		fhirTaskOutput.setDescription("Download URL Desc");
		fhirTaskOutput.setValueText("Download URL Link");
		fhirTaskOutput.setType(conceptService.getConceptByName("Download URL"));
		fhirTask.setOutput(Collections.singleton(fhirTaskOutput));
		fhirTaskDao.createOrUpdate(fhirTask);

		return fhirTask;
	}
}
