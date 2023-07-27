package org.openmrs.module.fhirExtension.export.anonymise.factory;

import org.openmrs.module.fhirExtension.export.anonymise.ResourceRedact;
import org.openmrs.module.fhirExtension.export.anonymise.impl.AddressResourceRedact;
import org.openmrs.module.fhirExtension.export.anonymise.impl.BirthDateResourceRedact;
import org.openmrs.module.fhirExtension.export.anonymise.impl.DeceasedDateTimeResourceRedact;
import org.openmrs.module.fhirExtension.export.anonymise.impl.IdentifierResourceRedact;
import org.openmrs.module.fhirExtension.export.anonymise.impl.NameResourceRedact;
import org.openmrs.module.fhirExtension.export.anonymise.impl.TelecomResourceRedact;

public class FieldHandlerSingletonFactory {
	
	public static ResourceRedact getInstance(String fieldName) {
		if (fieldName == null || fieldName.isEmpty())
			return null;
		switch (fieldName) {
			case "identifier":
				return IdentifierResourceRedact.getInstance();
			case "birthDate":
				return BirthDateResourceRedact.getInstance();
			case "deceasedDateTime":
				return DeceasedDateTimeResourceRedact.getInstance();
			case "address":
				return AddressResourceRedact.getInstance();
			case "name":
				return NameResourceRedact.getInstance();
			case "telecom":
				return TelecomResourceRedact.getInstance();
			default:
				throw new IllegalArgumentException("Unknown fieldName " + fieldName);
		}
	}
}
