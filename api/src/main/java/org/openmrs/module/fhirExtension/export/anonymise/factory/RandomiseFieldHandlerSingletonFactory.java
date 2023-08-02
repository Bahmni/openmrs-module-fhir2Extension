package org.openmrs.module.fhirExtension.export.anonymise.factory;

import org.openmrs.module.fhirExtension.export.anonymise.ResourceRandomise;
import org.openmrs.module.fhirExtension.export.anonymise.impl.BirthDateResourceRandomiser;
import org.openmrs.module.fhirExtension.export.anonymise.impl.DeceasedDateTimeResourceRandomiser;

public class RandomiseFieldHandlerSingletonFactory {
	
	public static ResourceRandomise getInstance(String fieldName) {
		switch (fieldName) {
			case "birthDate":
				return BirthDateResourceRandomiser.getInstance();
			case "deceasedDateTime":
				return DeceasedDateTimeResourceRandomiser.getInstance();
			default:
				throw new IllegalArgumentException("Unknown fieldName " + fieldName);
		}
	}
}
