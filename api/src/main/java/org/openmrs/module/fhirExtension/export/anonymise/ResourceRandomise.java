package org.openmrs.module.fhirExtension.export.anonymise;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.instance.model.api.IBaseResource;

public interface ResourceRandomise {
	
	void randomise(IBaseResource iBaseResource, String fixedValue);

	default String getRandomAlphabets(String inputStr) {
		return StringUtils.isNotBlank(inputStr) ?  RandomStringUtils.random(inputStr.length(), true, false) : inputStr;
	}

	default String getRandomAlphaNumeric(String inputStr) {
		return  StringUtils.isNotBlank(inputStr) ? RandomStringUtils.random(inputStr.length(), true, true) : inputStr;
	}

	default String getRandomNumber(String inputStr) {
		return  StringUtils.isNotBlank(inputStr) ? RandomStringUtils.random(inputStr.length(), false, true) : inputStr;
	}
}
