package org.openmrs.module.fhirExtension.export.anonymise.impl;

import ca.uhn.fhir.model.api.TemporalPrecisionEnum;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.DateType;
import org.hl7.fhir.r4.model.Patient;
import org.openmrs.module.fhirExtension.export.anonymise.ResourceRandomise;

import java.util.Calendar;

public class BirthDateResourceRandomiser implements ResourceRandomise {
	
	private BirthDateResourceRandomiser() {
		
	}
	
	@Override
	public void randomise(IBaseResource iBaseResource) {
		Patient patient = (Patient) iBaseResource;
		DateType dateElement = patient.getBirthDateElement();
		if (dateElement != null) {
			dateElement.setDay(1);
			dateElement.setMonth(Calendar.JANUARY);
			dateElement.setPrecision(TemporalPrecisionEnum.DAY);
		}
	}
	
	private static class SingletonHelper {
		
		private static final BirthDateResourceRandomiser INSTANCE = new BirthDateResourceRandomiser();
	}
	
	public static BirthDateResourceRandomiser getInstance() {
		return BirthDateResourceRandomiser.SingletonHelper.INSTANCE;
	}
}
