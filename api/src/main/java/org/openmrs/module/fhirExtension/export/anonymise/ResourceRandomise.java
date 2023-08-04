package org.openmrs.module.fhirExtension.export.anonymise;

import org.apache.commons.lang3.RandomStringUtils;
import org.hl7.fhir.instance.model.api.IBaseResource;

public interface ResourceRandomise {
	
	void randomise(IBaseResource iBaseResource, String fixedValue);

	default String getRandomAlphaCharacters(int length) {
		return RandomStringUtils.random(length, true, false);
	}

	default String getRandomAlphaNumericCharacters(int length) {
		return RandomStringUtils.random(length, true, true);
	}

	default String getRandomNumericCharacters(int length) {
		return RandomStringUtils.random(length, false, true);
	}
}
