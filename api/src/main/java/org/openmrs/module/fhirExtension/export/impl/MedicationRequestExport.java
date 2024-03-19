package org.openmrs.module.fhirExtension.export.impl;

import ca.uhn.fhir.rest.api.server.IBundleProvider;
import ca.uhn.fhir.rest.param.DateRangeParam;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.MedicationRequest;
import org.openmrs.Drug;
import org.openmrs.DrugOrder;
import org.openmrs.DrugReferenceMap;
import org.openmrs.Order;
import org.openmrs.api.OrderService;
import org.openmrs.module.fhir2.api.FhirConceptSourceService;
import org.openmrs.module.fhir2.api.FhirMedicationRequestService;
import org.openmrs.module.fhir2.api.translators.MedicationTranslator;
import org.openmrs.module.fhirExtension.export.Exporter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class MedicationRequestExport implements Exporter {
	
	private final FhirMedicationRequestService fhirMedicationRequestService;
	
	private final MedicationTranslator medicationTranslator;
	
	private FhirConceptSourceService fhirConceptSourceService;
	
	private final OrderService orderService;
	
	String CODING_SYSTEM_FOR_OPENMRS_CONCEPT = "https://fhir.openmrs.org";
	
	@Autowired
	public MedicationRequestExport(FhirMedicationRequestService fhirMedicationRequestService,
	    MedicationTranslator medicationTranslator, FhirConceptSourceService fhirConceptSourceService,
	    OrderService orderService) {
		this.fhirMedicationRequestService = fhirMedicationRequestService;
		this.medicationTranslator = medicationTranslator;
		this.fhirConceptSourceService = fhirConceptSourceService;
		this.orderService = orderService;
	}
	
	@Override
	public String getResourceType() {
		return "medicationRequest";
	}
	
	@Override
	public List<IBaseResource> export(String startDate, String endDate) {
		DateRangeParam lastUpdated = getLastUpdated(startDate, endDate);
		IBundleProvider iBundleProvider = fhirMedicationRequestService.searchForMedicationRequests(null, null, null, null,
		    null, null, null, null, lastUpdated, null, null);
        return iBundleProvider.getAllResources().stream().map(this::addMedicationInfo).collect(Collectors.toList());
	}
	
	private MedicationRequest addMedicationInfo(IBaseResource iBaseResource) {
		MedicationRequest medicationRequest = (MedicationRequest) iBaseResource;
		String orderUuid = medicationRequest.getId();
		CodeableConcept codeableConcept = getCodeableConceptForMedicationRequest(orderService.getOrderByUuid(orderUuid));
		medicationRequest.setMedication(codeableConcept);
		return medicationRequest;
	}
	
	private CodeableConcept getCodeableConceptForMedicationRequest(Order order) {
		CodeableConcept codeableConcept = new CodeableConcept();

		Drug drug = ((DrugOrder) order).getDrug();
		Set<DrugReferenceMap> drugReferenceMaps = drug.getDrugReferenceMaps();
		if (!drugReferenceMaps.isEmpty()) {
			drugReferenceMaps.stream().forEach(drugReferenceMap -> {
				Coding coding = new Coding();
				coding.setCode(drugReferenceMap.getConceptReferenceTerm().getCode());
				coding.setDisplay(drug.getDisplayName());
				coding.setSystem(fhirConceptSourceService.getUrlForConceptSource(drugReferenceMap.getConceptReferenceTerm().getConceptSource()));
				codeableConcept.addCoding(coding);
			});
			codeableConcept.setText(drug.getDisplayName());
		}

		Coding bahmniCoding = new Coding();
		bahmniCoding.setCode(drug.getUuid());
		bahmniCoding.setDisplay(drug.getDisplayName());
		bahmniCoding.setSystem(CODING_SYSTEM_FOR_OPENMRS_CONCEPT);
		codeableConcept.addCoding(bahmniCoding);

		return codeableConcept;
	}
}
