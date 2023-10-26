package org.openmrs.module.fhirExtension.export.anonymise.impl;

import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.*;

public class IdResourceCorrelate {
	
	public static IdResourceCorrelate getInstance() {
		return SingletonHelper.INSTANCE;
	}
	
	public void correlateResource(IBaseResource iBaseResource, String resourceType, CorrelationCache correlationCache,
	        byte[] salt) {
		switch (resourceType) {
			case "patient":
				correlatePatient(iBaseResource, correlationCache, salt);
				break;
			case "condition":
				correlateCondition(iBaseResource, correlationCache, salt);
				break;
			case "medicationRequest":
				correlateMedicationRequest(iBaseResource, correlationCache, salt);
				break;
			case "serviceRequest":
				correlateProcedureOrder(iBaseResource, correlationCache, salt);
				break;
		}
	}
	
	public void correlatePatient(IBaseResource iBaseResource, CorrelationCache correlationCache, byte[] salt) {
		Patient patient = (Patient) iBaseResource;
		String currentId = patient.getId();
		patient.setId(correlationCache.readDigest(currentId, salt));
	}
	
	public void correlateCondition(IBaseResource iBaseResource, CorrelationCache correlationCache, byte[] salt) {
		Condition condition = (Condition) iBaseResource;
		correlate(condition.getSubject(), correlationCache, salt);
	}
	
	public void correlateMedicationRequest(IBaseResource iBaseResource, CorrelationCache correlationCache, byte[] salt) {
		MedicationRequest medicationRequest = (MedicationRequest) iBaseResource;
		correlate(medicationRequest.getSubject(), correlationCache, salt);
	}
	
	public void correlateProcedureOrder(IBaseResource iBaseResource, CorrelationCache correlationCache, byte[] salt) {
		ServiceRequest serviceRequest = (ServiceRequest) iBaseResource;
		correlate(serviceRequest.getSubject(), correlationCache, salt);
	}
	
	private void correlate(Reference subjectRef, CorrelationCache correlationCache, byte[] salt) {
		String patientRef = subjectRef.getReference();
		String[] patientRefTokens = patientRef.split("/");
		subjectRef.setReference(patientRefTokens[0] + "/" + correlationCache.readDigest(patientRefTokens[1], salt));
		subjectRef.setDisplay(null);
	}
	
	private static class SingletonHelper {
		
		private static final IdResourceCorrelate INSTANCE = new IdResourceCorrelate();
	}
	
}
