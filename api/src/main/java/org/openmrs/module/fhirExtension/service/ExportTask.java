package org.openmrs.module.fhirExtension.service;

import lombok.extern.log4j.Log4j2;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.openmrs.api.ConceptService;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.UserContext;
import org.openmrs.module.fhir2.api.dao.FhirTaskDao;
import org.openmrs.module.fhir2.model.FhirTask;
import org.openmrs.module.fhir2.model.FhirTaskOutput;
import org.openmrs.module.fhirExtension.export.Exporter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
@Log4j2
public class ExportTask {
	
	private FhirTaskDao fhirTaskDao;
	private ConceptService conceptService;
	private ExportToFileService exportToFileService;
	
	@Autowired
	public ExportTask(FhirTaskDao fhirTaskDao, ConceptService conceptService, ExportToFileService exportToFileService) {
		this.fhirTaskDao = fhirTaskDao;
		this.conceptService = conceptService;
		this.exportToFileService = exportToFileService;
	}
	
	public FhirTask getInitialTaskResponse() {
		FhirTask fhirTask = new FhirTask();
		fhirTask.setStatus(FhirTask.TaskStatus.ACCEPTED);
		String logMessage = "Patient Data Export by " + Context.getAuthenticatedUser().getUsername();
		fhirTask.setName(logMessage);
		log.info(logMessage);
		fhirTask.setIntent(FhirTask.TaskIntent.ORDER);
		
		fhirTaskDao.createOrUpdate(fhirTask);
		
		return fhirTask;
	}
	
	@Async("export-fhir-data-threadPoolTaskExecutor")
	public void export(FhirTask fhirTask, String startDate, String endDate, UserContext userContext) {
		FhirTask.TaskStatus taskStatus = null;
		
		try {
			Context.openSession();
			Context.setUserContext(userContext);
			
			List<Exporter> fhirExporters = Context.getRegisteredComponents(Exporter.class);
			exportToFileService.createDirectory(fhirTask.getUuid());
			for (Exporter fhirExporter : fhirExporters) {
				List<IBaseResource> fhirRsources = fhirExporter.export(startDate, endDate);
				exportToFileService.createAndWriteToFile(fhirRsources, fhirTask.getUuid());
			}
			
			FhirTaskOutput fhirTaskOutput = new FhirTaskOutput();
			fhirTaskOutput.setName("Download Link Name");
			fhirTaskOutput.setTask(fhirTask);
			fhirTaskOutput.setValueText(fhirTask.getUuid());
			fhirTaskOutput.setType(conceptService.getConceptByName("Download URL"));
			fhirTask.setOutput(Collections.singleton(fhirTaskOutput));
		}
		catch (Exception exception) {
			taskStatus = FhirTask.TaskStatus.REJECTED;
			log.error("Exception occurred while exporting data in FHIR format ", exception);
		}
		finally {
			if (taskStatus == null) {
				taskStatus = FhirTask.TaskStatus.COMPLETED;
			}
			fhirTask.setStatus(taskStatus);
			fhirTaskDao.createOrUpdate(fhirTask);
			Context.closeSession();
		}
	}
}
