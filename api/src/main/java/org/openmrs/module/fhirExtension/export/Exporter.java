package org.openmrs.module.fhirExtension.export;

import ca.uhn.fhir.rest.param.DateRangeParam;
import org.hl7.fhir.instance.model.api.IBaseResource;
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
}
