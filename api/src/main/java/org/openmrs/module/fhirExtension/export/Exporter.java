package org.openmrs.module.fhirExtension.export;

import org.hl7.fhir.instance.model.api.IBaseResource;
import org.springframework.beans.factory.config.BeanPostProcessor;

import java.util.List;

public interface Exporter extends BeanPostProcessor {
	
	List<IBaseResource> export(String startDate, String endDate);
}
