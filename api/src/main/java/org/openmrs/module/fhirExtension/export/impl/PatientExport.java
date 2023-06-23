package org.openmrs.module.fhirExtension.export.impl;

import ca.uhn.fhir.rest.api.server.IBundleProvider;
import ca.uhn.fhir.rest.param.DateRangeParam;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.openmrs.module.fhir2.api.FhirPatientService;
import org.openmrs.module.fhir2.api.search.param.PatientSearchParams;
import org.openmrs.module.fhirExtension.export.Exporter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PatientExport implements Exporter {
	
	@Autowired
	private FhirPatientService fhirPatientService;
	
	@Override
	public List<IBaseResource> export(String startDate, String endDate) {
		DateRangeParam lastUpdated = getLastUpdated(startDate, endDate);
		PatientSearchParams patientSearchParams = new PatientSearchParams(null, null, null, null, null, null, null, null,
		        null, null, null, null, null, lastUpdated, null, null);
		IBundleProvider iBundleProvider = fhirPatientService.searchForPatients(patientSearchParams);
		return iBundleProvider.getAllResources();
	}
	
	private DateRangeParam getLastUpdated(String startDate, String endDate) {
		DateRangeParam lastUpdated = new DateRangeParam();
		if (startDate != null) {
			lastUpdated.setLowerBound(startDate);
		}
		if (endDate != null) {
			lastUpdated.setUpperBound(endDate);
		}
		return lastUpdated;
	}
}
