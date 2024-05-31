package org.openmrs.module.fhirExtension.export.impl;

import ca.uhn.fhir.rest.api.server.IBundleProvider;
import ca.uhn.fhir.rest.param.DateRangeParam;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Condition;
import org.openmrs.module.fhir2.api.FhirConditionService;
import org.openmrs.module.fhir2.api.search.param.ConditionSearchParams;
import org.openmrs.module.fhirExtension.export.Exporter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ConditionExport implements Exporter {
	
	private FhirConditionService fhirConditionService;
	
	@Autowired
	public ConditionExport(FhirConditionService fhirConditionService) {
		this.fhirConditionService = fhirConditionService;
	}
	
	@Override
    public List<IBaseResource> export(String startDate, String endDate) {
        DateRangeParam lastUpdated = getLastUpdated(startDate, endDate);
		ConditionSearchParams conditionSearchParams=ConditionSearchParams.builder().lastUpdated(lastUpdated).build();
        IBundleProvider iBundleProvider = fhirConditionService.searchConditions(conditionSearchParams);
        return iBundleProvider.getAllResources().stream().map(this::addCategory).collect(Collectors.toList());
    }
	
	@Override
	public String getResourceType() {
		return "condition";
	}
	
	private Condition addCategory(IBaseResource baseResource) {
		Condition condition = (Condition) baseResource;
		CodeableConcept codeableConcept = new CodeableConcept();
		Coding coding = new Coding("http://terminology.hl7.org/CodeSystem/condition-category", "problem-list-item",
		        "Problem List Item");
		condition.setCategory(Collections.singletonList(codeableConcept.addCoding(coding)));
		return condition;
	}
}
