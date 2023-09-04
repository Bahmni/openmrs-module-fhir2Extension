package org.openmrs.module.fhirExtension.domain.observation;

import lombok.Builder;
import lombok.Getter;
import org.openmrs.Concept;
import org.openmrs.ConceptDatatype;
import org.openmrs.Obs;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;

@Builder
@Getter
public class LabResult {
	
	private Concept concept;
	
	private String labReportUrl;
	
	private String labReportFileName;
	
	private String labReportNotes;
	
	private Map<Concept, Object> labResultValue;
	
	private Map<Concept, Obs.Interpretation> interpretationOfLabResultValue;
	
	private BiFunction<Concept, Object, Obs> obsFactory;
	
	public boolean isPanel() {
		return !concept.getSetMembers().isEmpty();
	}
	
	public List<Concept> getAllTests() {
		return this.concept.getSetMembers();
	}
	
	public Obs newObs(Concept testConcept) {
		return obsFactory.apply(testConcept, null);
	}
	
	public Optional<Obs> newValueObs(Concept obsConcept, Object value) {
		if (value != null) {
			return Optional.of(obsFactory.apply(obsConcept, value));
		}
		return Optional.empty();
	}
	
	public boolean isLabReportPresent() {
		return labReportFileName != null || labReportUrl != null;
	}
	
	public static LabResultBuilder builder() {
		return new LabResult.LabResultBuilder();
	}
	
	public static class LabResultBuilder {
		
		private String labReportUrl;
		
		private String labReportNotes;
		
		private String labReportFileName;
		
		public LabResultBuilder labReportUrl(Optional<Obs> obs) {
			obs.ifPresent(labReportUrlObs -> this.labReportUrl = labReportUrlObs.getValueText());
			return this;
		}
		
		public LabResultBuilder labReportUrl(String labReportUrl) {
			this.labReportUrl = labReportUrl;
			return this;
		}
		
		public LabResultBuilder labReportNotes(Optional<Obs> obs) {
			obs.ifPresent(labReportNotesObs -> this.labReportNotes = labReportNotesObs.getValueText());
			return this;
		}
		
		public LabResultBuilder labReportNotes(String labReportNotes) {
			this.labReportNotes = labReportNotes;
			return this;
		}
		
		public LabResultBuilder labReportFileName(Optional<Obs> obs) {
			obs.ifPresent(labReportFileNameObs -> this.labReportFileName = labReportFileNameObs.getValueText());
			return this;
		}
		
		public LabResultBuilder labReportFileName(String labReportFileName) {
			this.labReportFileName = labReportFileName;
			return this;
		}
		
		public LabResultBuilder setLabResultValue(List<Obs> obsList) {
			HashMap<Concept, Object> labResultMap = new HashMap<>();
			HashMap<Concept,Obs.Interpretation> interpretationLabResultMap = new HashMap<>();

			for (Obs obs : obsList) {
				String datatype = obs.getConcept().getDatatype().getHl7Abbreviation();
				if (datatype.equals(ConceptDatatype.NUMERIC))
					labResultMap.put(obs.getConcept(), obs.getValueNumeric().toString());
				else if (datatype.equals(ConceptDatatype.CODED)) {
					labResultMap.put(obs.getConcept(), obs.getValueCoded());
				} else if (datatype.equals(ConceptDatatype.BOOLEAN)) {
					labResultMap.put(obs.getConcept(), obs.getValueBoolean());
				} else if (datatype.equals(ConceptDatatype.DATETIME)) {
					labResultMap.put(obs.getConcept(), obs.getValueDatetime());
				} else if (datatype.equals(ConceptDatatype.DATE)) {
					labResultMap.put(obs.getConcept(), obs.getValueDate());
				} else if (datatype.equals(ConceptDatatype.TIME)) {
					labResultMap.put(obs.getConcept(), obs.getValueTime());
				} else
					labResultMap.put(obs.getConcept(), obs.getValueText());
				if (obs.getInterpretation() != null) {
					Obs.Interpretation interpretation = obs.getInterpretation();
					interpretationLabResultMap.put(obs.getConcept(),interpretation);
				}
			}
			this.labResultValue = labResultMap;
			this.interpretationOfLabResultValue = interpretationLabResultMap;
			return this;
		}
	}
}
