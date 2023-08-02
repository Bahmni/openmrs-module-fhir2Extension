package org.openmrs.module.fhirExtension.export.impl;

import ca.uhn.fhir.rest.api.server.IBundleProvider;
import ca.uhn.fhir.rest.param.DateRangeParam;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.openmrs.module.fhir2.api.FhirPatientService;
import org.openmrs.module.fhir2.api.search.param.PatientSearchParams;
import org.openmrs.module.fhirExtension.export.Exporter;
import org.openmrs.module.fhirExtension.export.anonymise.handler.AnonymiseHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PatientExport implements Exporter {
	
	private FhirPatientService fhirPatientService;
	
	private AnonymiseHandler anonymiseHandler;
	
	@Autowired
	public PatientExport(FhirPatientService fhirPatientService, AnonymiseHandler anonymiseHandler) {
		this.fhirPatientService = fhirPatientService;
		this.anonymiseHandler = anonymiseHandler;
	}
	
	@Override
	public List<IBaseResource> export(String startDate, String endDate, boolean isAnonymise) {
		DateRangeParam lastUpdated = getLastUpdated(startDate, endDate);
		PatientSearchParams patientSearchParams = new PatientSearchParams(null, null, null, null, null, null, null, null,
		        null, null, null, null, null, lastUpdated, null, null);
		IBundleProvider iBundleProvider = fhirPatientService.searchForPatients(patientSearchParams);
		return isAnonymise ? anonymise(iBundleProvider.getAllResources()) : iBundleProvider.getAllResources();
	}
	
	private List<IBaseResource> anonymise(List<IBaseResource> iBaseResources) {
		iBaseResources.forEach(iBaseResource -> anonymiseHandler.anonymise(iBaseResource, "patient"));
		return iBaseResources;
	}
}
