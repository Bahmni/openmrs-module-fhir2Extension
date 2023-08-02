package org.openmrs.module.fhirExtension.export.anonymise.impl;

import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.DateTimeType;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Type;
import org.openmrs.module.fhirExtension.export.anonymise.ResourceRandomise;

import java.util.Calendar;

public class DeceasedDateTimeResourceRandomiser implements ResourceRandomise {
	
	private DeceasedDateTimeResourceRandomiser() {
		
	}
	
	@Override
	public void randomise(IBaseResource iBaseResource) {
		Patient patient = (Patient) iBaseResource;
		Type deceasedType = patient.getDeceased();
		if (deceasedType instanceof DateTimeType) {
			DateTimeType deceasedDateTimeType = patient.getDeceasedDateTimeType();
			deceasedDateTimeType.setDay(1);
			deceasedDateTimeType.setMonth(Calendar.JANUARY);
		}
	}
	
	private static class SingletonHelper {
		private static final DeceasedDateTimeResourceRandomiser INSTANCE = new DeceasedDateTimeResourceRandomiser();
	}
	
	public static DeceasedDateTimeResourceRandomiser getInstance() {
		return DeceasedDateTimeResourceRandomiser.SingletonHelper.INSTANCE;
	}
}
