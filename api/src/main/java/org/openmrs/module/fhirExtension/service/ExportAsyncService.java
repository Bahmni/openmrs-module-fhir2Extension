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
import org.openmrs.module.fhirExtension.export.anonymise.handler.AnonymiseHandler;
import org.openmrs.module.fhirExtension.export.anonymise.impl.CorrelationCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Log4j2
@Component
@Transactional
public class ExportAsyncService {
	
	public static final String DOWNLOAD_URL = "Download URL";
	
	private FhirTaskDao fhirTaskDao;
	
	private ConceptService conceptService;
	
	private FileExportService fileExportService;
	
	private AnonymiseHandler anonymiseHandler;
	
	private List<Exporter> fhirExporters;
	
	private CorrelationCache correlationCache;
	
	@Autowired
	public ExportAsyncService(FhirTaskDao fhirTaskDao, ConceptService conceptService, FileExportService fileExportService,
	    AnonymiseHandler anonymiseHandler, List<Exporter> fhirExporters, CorrelationCache correlationCache) {
		this.fhirTaskDao = fhirTaskDao;
		this.conceptService = conceptService;
		this.fileExportService = fileExportService;
		this.anonymiseHandler = anonymiseHandler;
		this.fhirExporters = fhirExporters;
		this.correlationCache = correlationCache;
	}
	
	@Async("export-fhir-data-threadPoolTaskExecutor")
	public void export(FhirTask fhirTask, String startDate, String endDate, UserContext userContext, String downloadUrl,
	        boolean isAnonymise) {
		FhirTask.TaskStatus taskStatus = null;
		
		try {
			Context.openSession();
			Context.setUserContext(userContext);
			
			initialize(fhirTask, isAnonymise);

			fhirExporters.forEach( fhirExporter -> {
				List<IBaseResource> fhirResources = fhirExporter.export(startDate, endDate);
				anonymise(fhirResources, fhirExporter.getResourceType(), isAnonymise);
				fileExportService.createAndWriteToFile(fhirResources, fhirTask.getUuid());
			});

			fileExportService.createZipWithExportedNdjsonFiles(fhirTask.getUuid());
			FhirTaskOutput fhirTaskOutput = getFhirTaskOutput(fhirTask, downloadUrl);
			fhirTask.setOutput(Collections.singleton(fhirTaskOutput));
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
	
	private FhirTaskOutput getFhirTaskOutput(FhirTask fhirTask, String downloadUrl) {
		FhirTaskOutput fhirTaskOutput = new FhirTaskOutput();
		fhirTaskOutput.setName("Download Link Name");
		fhirTaskOutput.setTask(fhirTask);
		fhirTaskOutput.setValueText(downloadUrl + "?file=" + fhirTask.getUuid());
		fhirTaskOutput.setType(conceptService.getConceptByName(DOWNLOAD_URL));
		return fhirTaskOutput;
	}
	
	private void anonymise(List<IBaseResource> iBaseResources, String resourceType, boolean isAnonymise) {
		if(isAnonymise) {
			iBaseResources.forEach(iBaseResource -> anonymiseHandler.anonymise(iBaseResource, resourceType));
		}
	}
}
