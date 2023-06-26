package org.openmrs.module.fhirExtension.export.impl;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.time.DateUtils;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Condition;
import org.hl7.fhir.r4.model.DateTimeType;
import org.hl7.fhir.r4.model.Reference;
import org.openmrs.Concept;
import org.openmrs.ConditionClinicalStatus;
import org.openmrs.Obs;
import org.openmrs.api.ConceptService;
import org.openmrs.api.ObsService;
import org.openmrs.module.fhir2.api.translators.ConceptTranslator;
import org.openmrs.module.fhir2.api.translators.ConditionClinicalStatusTranslator;
import org.openmrs.module.fhirExtension.export.Exporter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Component
@Log4j2
public class DiagnosisExport implements Exporter {
	
	private static final String VISIT_DIAGNOSES = "Visit Diagnoses";
	private static final String CODED_DIAGNOSIS = "Coded Diagnosis";
	private static final String BAHMNI_DIAGNOSIS_STATUS = "Bahmni Diagnosis Status";
	public static final String DATE_FORMAT = "yyyy-MM-dd";
	
	private ConceptTranslator conceptTranslator;
	private ObsService obsService;
	private ConceptService conceptService;
	private ConditionClinicalStatusTranslator conditionClinicalStatusTranslator;
	
	@Autowired
	public DiagnosisExport(ConceptTranslator conceptTranslator,
	    ConditionClinicalStatusTranslator conditionClinicalStatusTranslator, ConceptService conceptService,
	    ObsService obsService) {
		this.conceptTranslator = conceptTranslator;
		this.conditionClinicalStatusTranslator = conditionClinicalStatusTranslator;
		this.conceptService = conceptService;
		this.obsService = obsService;
	}
	
	@Override
	public List<IBaseResource> export(String startDateStr, String endDateStr) {
		List<IBaseResource> fhirResources = new ArrayList<>();

		Date startDate = getFormattedDate(startDateStr);
		Date endDate = getFormattedDate(endDateStr);
		Concept visitDiagnosesConcept = conceptService.getConceptByName(VISIT_DIAGNOSES);
		List<Obs> visitDiagnosesObs = obsService.getObservations(null, null, Arrays.asList(visitDiagnosesConcept), null, null,
		    null, null, null, null, startDate, endDate, false);

		visitDiagnosesObs.stream().filter(this::isCodedDiagnosis)
				                  .map(this::getDiagnosisAsFhirCondition)
				                  .forEach(fhirResources :: add);
		
		return fhirResources;
	}
	
	private Condition getDiagnosisAsFhirCondition(Obs visitDiagnosisObsGroup) {
		Condition condition = new Condition();
		Reference patientReference = new Reference();
		Reference encounterReference = new Reference();
		Obs codedDiagnosisObs = getObsFor(visitDiagnosisObsGroup, CODED_DIAGNOSIS);
		CodeableConcept clinicalStatus = getClinicalStatus(visitDiagnosisObsGroup);
		patientReference.setReference("Patient/" + codedDiagnosisObs.getPerson().getUuid());
		encounterReference.setReference("Encounter/" + codedDiagnosisObs.getEncounter().getUuid());
		CodeableConcept codeableConcept = conceptTranslator.toFhirResource(codedDiagnosisObs.getValueCoded());
		condition.setId(codedDiagnosisObs.getUuid());
		condition.setClinicalStatus(clinicalStatus);
		condition.setOnset(new DateTimeType().setValue(codedDiagnosisObs.getObsDatetime()));
		condition.setCode(codeableConcept);
		condition.setSubject(patientReference);
		condition.setEncounter(encounterReference);
		condition.getMeta().setLastUpdated(codedDiagnosisObs.getObsDatetime());
		return condition;
	}
	
	private Date getFormattedDate(String dateStr) {
		Date date = null;
		if (dateStr == null)
			return null;
		try {
			date = DateUtils.parseDate(dateStr, DATE_FORMAT);
		} catch (ParseException e) {
			log.error("Exception while parsing the date ", e);
			throw new RuntimeException(e);
		}
		return date;
	}
	
	private boolean isActiveDiagnosis(Obs visitDiagnosisObsGroup) {
		Obs codedDiagnosisStatusObs = getObsFor(visitDiagnosisObsGroup, BAHMNI_DIAGNOSIS_STATUS);
		return codedDiagnosisStatusObs == null;
	}
	
	private boolean isCodedDiagnosis(Obs visitDiagnosisObsGroup) {
		Obs codedDiagnosisObs = getObsFor(visitDiagnosisObsGroup, CODED_DIAGNOSIS);
		return codedDiagnosisObs != null;
	}
	
	private Obs getObsFor(Obs visitDiagnosisObsGroup, String conceptName) {
		Optional<Obs> optionalObs = visitDiagnosisObsGroup.getGroupMembers()
				.stream()
				.filter(obs -> obs.getConcept().getName().getName().equals(conceptName))
				.findFirst();
		if (optionalObs.isPresent()) {
			return optionalObs.get();
		}
		return null;
	}
	
	private CodeableConcept getClinicalStatus(Obs visitDiagnosisObsGroup) {
		boolean isActiveDiagnosis = isActiveDiagnosis(visitDiagnosisObsGroup);
		ConditionClinicalStatus conditionClinicalStatus = ConditionClinicalStatus.INACTIVE;
		if (isActiveDiagnosis) {
			conditionClinicalStatus = ConditionClinicalStatus.ACTIVE;
		}
		return conditionClinicalStatusTranslator.toFhirResource(conditionClinicalStatus);
	}
}
