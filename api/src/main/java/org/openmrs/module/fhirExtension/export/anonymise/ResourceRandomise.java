package org.openmrs.module.fhirExtension.export.anonymise;

import org.hl7.fhir.instance.model.api.IBaseResource;

public interface ResourceRandomise {
	void randomise(IBaseResource iBaseResource, String fixedValue);
}
