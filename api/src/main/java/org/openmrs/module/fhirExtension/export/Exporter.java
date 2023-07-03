package org.openmrs.module.fhirExtension.export;

import ca.uhn.fhir.rest.param.DateRangeParam;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Reference;
import org.springframework.beans.factory.config.BeanPostProcessor;

import java.util.List;

public interface Exporter extends BeanPostProcessor {
	
	List<IBaseResource> export(String startDate, String endDate);

	 default DateRangeParam getLastUpdated(String startDate, String endDate) {
		DateRangeParam lastUpdated = new DateRangeParam();
		if (startDate != null) {
			lastUpdated.setLowerBound(startDate);
		}
		if (endDate != null) {
			lastUpdated.setUpperBound(endDate);
		}
		return lastUpdated;
	}

	default Reference getSubjectReference(String uuid) {
		Reference patientReference = new Reference();
		patientReference.setReference("Patient/" + uuid);
		return patientReference;
	}

	default Reference getEncounterReference(String uuid) {
		Reference encounterReference = new Reference();
		encounterReference.setReference("Encounter/" + uuid);
		return encounterReference;
	}
}
