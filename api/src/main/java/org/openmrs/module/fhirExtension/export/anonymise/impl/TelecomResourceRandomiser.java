package org.openmrs.module.fhirExtension.export.anonymise.impl;

import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.ContactPoint;
import org.hl7.fhir.r4.model.Patient;
import org.openmrs.module.fhirExtension.export.anonymise.ResourceRandomise;

import java.util.List;

public class TelecomResourceRandomiser implements ResourceRandomise {
	
	private TelecomResourceRandomiser() {
		
	}
	
	public static TelecomResourceRandomiser getInstance() {
		return TelecomResourceRandomiser.SingletonHelper.INSTANCE;
	}
	
	@Override
	public void randomise(IBaseResource iBaseResource, String fixedValue) {
		Patient patient = (Patient) iBaseResource;
		if (fixedValue != null && patient.hasTelecom()) {
			fixedTelecomHandler(fixedValue, patient);
			return;
		}
		randomTelecomHandler(patient);
	}
	
	private void randomTelecomHandler(Patient patient) {
        List<ContactPoint> contactPoints = patient.getTelecom();
        contactPoints.forEach(contactPoint -> contactPoint.setValue(getRandomNumber(contactPoint.getValue())));
    }
	
	private void fixedTelecomHandler(String fixedValue, Patient patient) {
        List<ContactPoint> contactPoints = patient.getTelecom();
        contactPoints.forEach(contactPoint -> contactPoint.setValue(fixedValue));
    }
	
	private static class SingletonHelper {
		
		private static final TelecomResourceRandomiser INSTANCE = new TelecomResourceRandomiser();
	}
}
