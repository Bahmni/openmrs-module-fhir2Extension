package org.openmrs.module.fhirExtension.service;

import org.openmrs.api.context.UserContext;
import org.openmrs.module.fhir2.model.FhirTask;

public interface ExportAsyncService {
	
	void export(FhirTask fhirTask, String startDate, String endDate, UserContext userContext, boolean isAnonymise);
}
