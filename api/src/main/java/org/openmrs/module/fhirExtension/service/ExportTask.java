package org.openmrs.module.fhirExtension.service;

import org.openmrs.annotation.Authorized;
import org.openmrs.module.fhir2.model.FhirTask;

public interface ExportTask {
	
	public static final String DOWNLOAD_URL = "Download URL";
	
	public static final String USER_NAME_CONCEPT = "FHIR Export User Name";
	
	public static final String START_DATE_CONCEPT = "FHIR Export Start Date";
	
	public static final String END_DATE_CONCEPT = "FHIR Export End Date";
	
	public static final String ANONYMISE_CONCEPT = "FHIR Export Anonymise Flag";
	
	@Authorized(value = { "Export Patient Data" })
	FhirTask getInitialTaskResponse(String startDate, String endDate, String downloadUrl, boolean isAnonymise);
	
	@Authorized(value = { "Export Patient Data" })
	String validateParams(String startDate, String endDate);
}
