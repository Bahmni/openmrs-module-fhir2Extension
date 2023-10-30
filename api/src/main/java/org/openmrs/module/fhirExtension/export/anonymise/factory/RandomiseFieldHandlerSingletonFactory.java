package org.openmrs.module.fhirExtension.export.anonymise.factory;

import org.openmrs.module.fhirExtension.export.anonymise.ResourceRandomise;
import org.openmrs.module.fhirExtension.export.anonymise.impl.AddressResourceRandomiser;
import org.openmrs.module.fhirExtension.export.anonymise.impl.BirthDateResourceRandomiser;
import org.openmrs.module.fhirExtension.export.anonymise.impl.DeceasedDateTimeResourceRandomiser;
import org.openmrs.module.fhirExtension.export.anonymise.impl.TelecomResourceRandomiser;

public class RandomiseFieldHandlerSingletonFactory {
	
	public static ResourceRandomise getInstance(String fieldName) {
		switch (fieldName) {
			case "birthDate":
				return BirthDateResourceRandomiser.getInstance();
			case "deceasedDateTime":
				return DeceasedDateTimeResourceRandomiser.getInstance();
			case "telecom":
				return TelecomResourceRandomiser.getInstance();
			case "address":
				return AddressResourceRandomiser.getInstance();
			default:
				throw new IllegalArgumentException("Unknown fieldName " + fieldName);
		}
	}
}
