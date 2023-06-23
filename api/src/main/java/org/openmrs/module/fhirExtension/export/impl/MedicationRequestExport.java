package org.openmrs.module.fhirExtension.export.impl;

import ca.uhn.fhir.rest.api.server.IBundleProvider;
import ca.uhn.fhir.rest.param.DateRangeParam;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.openmrs.module.fhir2.api.FhirMedicationRequestService;
import org.openmrs.module.fhirExtension.export.Exporter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MedicationRequestExport implements Exporter {
	
	@Autowired
	private FhirMedicationRequestService fhirMedicationRequestService;
	
	@Override
	public List<IBaseResource> export(String startDate, String endDate) {
		DateRangeParam lastUpdated = getLastUpdated(startDate, endDate);
		IBundleProvider iBundleProvider = fhirMedicationRequestService.searchForMedicationRequests(null, null, null, null,
		    null, null, null, null, lastUpdated, null, null);
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
