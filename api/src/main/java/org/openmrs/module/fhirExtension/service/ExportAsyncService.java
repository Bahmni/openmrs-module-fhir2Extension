package org.openmrs.module.fhirExtension.service;

import lombok.extern.log4j.Log4j2;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.openmrs.Concept;
import org.openmrs.api.ConceptService;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.UserContext;
import org.openmrs.module.fhir2.api.dao.FhirTaskDao;
import org.openmrs.module.fhir2.model.FhirTask;
import org.openmrs.module.fhir2.model.FhirTaskInput;
import org.openmrs.module.fhir2.model.FhirTaskOutput;
import org.openmrs.module.fhirExtension.export.Exporter;
import org.openmrs.module.fhirExtension.export.anonymise.handler.AnonymiseHandler;
import org.openmrs.module.fhirExtension.export.anonymise.impl.CorrelationCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Log4j2
@Component
@Transactional
public class ExportAsyncService {
	
	public static final String DOWNLOAD_URL = "Download URL";
	
	public static final String START_DATE_CONCEPT = "FHIR Export Start Date";
	
	public static final String END_DATE_CONCEPT = "FHIR Export End Date";
	
	public static final String ANONYMISE_CONCEPT = "FHIR Export Anonymise Flag";
	
	private FhirTaskDao fhirTaskDao;
	
	private ConceptService conceptService;
	
	private FileExportService fileExportService;
	
	private AnonymiseHandler anonymiseHandler;
	
	private CorrelationCache correlationCache;
	
	@Autowired
	public ExportAsyncService(FhirTaskDao fhirTaskDao, ConceptService conceptService, FileExportService fileExportService,
	    AnonymiseHandler anonymiseHandler, CorrelationCache correlationCache) {
		this.fhirTaskDao = fhirTaskDao;
		this.conceptService = conceptService;
		this.fileExportService = fileExportService;
		this.anonymiseHandler = anonymiseHandler;
		this.correlationCache = correlationCache;
	}
	
	@Async("export-fhir-data-threadPoolTaskExecutor")
	public void export(FhirTask fhirTask, String startDate, String endDate, UserContext userContext, String downloadUrl,
	        boolean isAnonymise) {
		FhirTask.TaskStatus taskStatus = null;
		
		try {
			Context.openSession();
			Context.setUserContext(userContext);
			List<Exporter> fhirExporters = Context.getRegisteredComponents(Exporter.class);
			initialize(fhirTask, isAnonymise);
			
			for (Exporter fhirExporter : fhirExporters) {
				List<IBaseResource> fhirResources = fhirExporter.export(startDate, endDate);
				anonymise(fhirResources, fhirExporter.getResourceType(), isAnonymise);
				fileExportService.createAndWriteToFile(fhirResources, fhirTask.getUuid());
			}
			
			fileExportService.createZipWithExportedNdjsonFiles(fhirTask.getUuid());
			FhirTaskOutput fhirTaskOutput = getFhirTaskOutput(fhirTask, downloadUrl);
			fhirTask.setOutput(Collections.singleton(fhirTaskOutput));
			Set<FhirTaskInput> fhirTaskInputs = getFhirTaskInputs(fhirTask, startDate, endDate, isAnonymise);
			fhirTask.setInput(fhirTaskInputs);
			fileExportService.deleteDirectory(fhirTask.getUuid());
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
		}
	}
	
	private void initialize(FhirTask fhirTask, boolean isAnonymise) {
		fileExportService.createDirectory(fhirTask.getUuid());
		correlationCache.reset();
		anonymiseHandler.loadAnonymiserConfig(isAnonymise);
	}
	
	private Set<FhirTaskInput> getFhirTaskInputs(FhirTask fhirTask, String startDate, String endDate, boolean isAnonymise) {
        FhirTaskInput startDateFhirTaskInput = createFHIRTaskInput(fhirTask, START_DATE_CONCEPT, startDate);
        FhirTaskInput endDateFhirTaskInput = createFHIRTaskInput(fhirTask, END_DATE_CONCEPT, endDate);
        FhirTaskInput anonymiseFhirTaskInput = createFHIRTaskInput(fhirTask, ANONYMISE_CONCEPT, Boolean.toString(isAnonymise));
        return new HashSet<>(Arrays.asList(startDateFhirTaskInput, endDateFhirTaskInput, anonymiseFhirTaskInput));
    }
	
	private FhirTaskInput createFHIRTaskInput(FhirTask fhirTask, String conceptName, String valueText) {
		Concept concept = conceptService.getConceptByName(conceptName);
		if (concept == null) {
			throw new RuntimeException("Concept with name " + conceptName + " not found");
		}
		FhirTaskInput fhirTaskInput = new FhirTaskInput();
		fhirTaskInput.setName(conceptName);
		fhirTaskInput.setTask(fhirTask);
		fhirTaskInput.setValueText(valueText);
		fhirTaskInput.setType(concept);
		return fhirTaskInput;
	}
	
	private FhirTaskOutput getFhirTaskOutput(FhirTask fhirTask, String downloadUrl) {
		FhirTaskOutput fhirTaskOutput = new FhirTaskOutput();
		fhirTaskOutput.setName("Download Link Name");
		fhirTaskOutput.setTask(fhirTask);
		fhirTaskOutput.setValueText(downloadUrl + "?file=" + fhirTask.getUuid());
		fhirTaskOutput.setType(conceptService.getConceptByName(DOWNLOAD_URL));
		return fhirTaskOutput;
	}
	
	private void anonymise(List<IBaseResource> iBaseResources, String resourceType, boolean isAnonymise) {
        if (isAnonymise) {
            iBaseResources.forEach(iBaseResource -> anonymiseHandler.anonymise(iBaseResource, resourceType));
        }
    }
}
