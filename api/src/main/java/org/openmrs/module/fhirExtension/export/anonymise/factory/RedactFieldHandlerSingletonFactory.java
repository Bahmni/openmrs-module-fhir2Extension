package org.openmrs.module.fhirExtension.export.anonymise.factory;

import org.openmrs.module.fhirExtension.export.anonymise.ResourceRedact;
import org.openmrs.module.fhirExtension.export.anonymise.impl.AddressResourceRedact;
import org.openmrs.module.fhirExtension.export.anonymise.impl.AuthoredOnResourceRedact;
import org.openmrs.module.fhirExtension.export.anonymise.impl.DosageInstructionsResourceRedact;
import org.openmrs.module.fhirExtension.export.anonymise.impl.EncounterResourceRedact;
import org.openmrs.module.fhirExtension.export.anonymise.impl.IdentifierResourceRedact;
import org.openmrs.module.fhirExtension.export.anonymise.impl.NameResourceRedact;
import org.openmrs.module.fhirExtension.export.anonymise.impl.PriorityResourceRedact;
import org.openmrs.module.fhirExtension.export.anonymise.impl.RecordedDateResourceRedact;
import org.openmrs.module.fhirExtension.export.anonymise.impl.RecorderResourceRedact;
import org.openmrs.module.fhirExtension.export.anonymise.impl.RequesterResourceRedact;
import org.openmrs.module.fhirExtension.export.anonymise.impl.TelecomResourceRedact;

public class RedactFieldHandlerSingletonFactory {
	
	public static ResourceRedact getInstance(String fieldName) {
		switch (fieldName) {
			case "identifier":
				return IdentifierResourceRedact.getInstance();
			case "address":
				return AddressResourceRedact.getInstance();
			case "name":
				return NameResourceRedact.getInstance();
			case "telecom":
				return TelecomResourceRedact.getInstance();
			case "encounter":
				return EncounterResourceRedact.getInstance();
			case "recorder":
				return RecorderResourceRedact.getInstance();
			case "recordedDate":
				return RecordedDateResourceRedact.getInstance();
			case "priority":
				return PriorityResourceRedact.getInstance();
			case "authoredOn":
				return AuthoredOnResourceRedact.getInstance();
			case "requester":
				return RequesterResourceRedact.getInstance();
			case "dosageInstruction":
				return DosageInstructionsResourceRedact.getInstance();
			default:
				throw new IllegalArgumentException("Unknown fieldName " + fieldName);
		}
	}
}
