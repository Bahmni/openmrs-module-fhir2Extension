package org.openmrs.module.fhirExtension.export.impl;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.time.DateUtils;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Procedure;
import org.hl7.fhir.r4.model.Reference;
import org.openmrs.Order;
import org.openmrs.OrderType;
import org.openmrs.api.OrderService;
import org.openmrs.module.fhir2.api.translators.ConceptTranslator;
import org.openmrs.module.fhirExtension.export.Exporter;
import org.openmrs.parameter.OrderSearchCriteria;
import org.openmrs.parameter.OrderSearchCriteriaBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
@Log4j2
public class ProcedureExport implements Exporter {
	
	public static final String PROCEDURE_ORDER = "Procedure Order";
	public static final String DATE_FORMAT = "yyyy-MM-dd";
	
	private OrderService orderService;
	private ConceptTranslator conceptTranslator;
	
	@Autowired
	public ProcedureExport(OrderService orderService, ConceptTranslator conceptTranslator) {
		this.orderService = orderService;
		this.conceptTranslator = conceptTranslator;
	}
	
	@Override
	public List<IBaseResource> export(String startDate, String endDate) {
		List<IBaseResource> procedureResources = new ArrayList<>();
		OrderType procedureOrderType = orderService.getOrderTypeByName(PROCEDURE_ORDER);
		if (procedureOrderType == null) {
			log.error("Order Type " + PROCEDURE_ORDER + " is not available");
			return procedureResources;
		}
		OrderSearchCriteria orderSearchCriteria = getOrderSearchCriteria(procedureOrderType, startDate, endDate);
		List<Order> orders = orderService.getOrders(orderSearchCriteria);
		orders.stream().map(this::convertToFhirResource).forEach(procedureResources :: add);
		return procedureResources;
	}
	
	private Procedure convertToFhirResource(Order order) {
		Procedure procedure = new Procedure();
		procedure.setId(order.getUuid());
		CodeableConcept codeableConcept = conceptTranslator.toFhirResource(order.getConcept());
		procedure.setCode(codeableConcept);
		Reference patientReference = new Reference();
		Reference encounterReference = new Reference();
		patientReference.setReference("Patient/" + order.getPatient().getUuid());
		encounterReference.setReference("Encounter/" + order.getEncounter().getUuid());
		procedure.setSubject(patientReference);
		procedure.setEncounter(encounterReference);
		setProcedureStatus(order, procedure);
		return procedure;
	}
	
	private void setProcedureStatus(Order order, Procedure procedure) {
		if (order.getFulfillerStatus() != null) {
			switch (order.getFulfillerStatus()) {
				case RECEIVED:
					procedure.setStatus(Procedure.ProcedureStatus.PREPARATION);
					break;
				case IN_PROGRESS:
					procedure.setStatus(Procedure.ProcedureStatus.INPROGRESS);
					break;
				case COMPLETED:
					procedure.setStatus(Procedure.ProcedureStatus.COMPLETED);
					break;
				case EXCEPTION:
					procedure.setStatus(Procedure.ProcedureStatus.STOPPED);
					break;
				default:
					procedure.setStatus(Procedure.ProcedureStatus.UNKNOWN);
			}
		}
	}
	
	private OrderSearchCriteria getOrderSearchCriteria(OrderType procedureOrderType, String startDate, String endDate) {
		OrderSearchCriteriaBuilder orderSearchCriteriaBuilder = new OrderSearchCriteriaBuilder();
		orderSearchCriteriaBuilder.setOrderTypes(Collections.singletonList(procedureOrderType));
		orderSearchCriteriaBuilder.setIncludeVoided(false);
		try {
			if (startDate != null) {
				orderSearchCriteriaBuilder.setActivatedOnOrAfterDate(DateUtils.parseDate(startDate, DATE_FORMAT));
			}
			if (endDate != null) {
				orderSearchCriteriaBuilder.setActivatedOnOrBeforeDate(DateUtils.parseDate(endDate, DATE_FORMAT));
			}
		}
		catch (ParseException e) {
			log.error("Exception while parsing the date ", e);
			throw new RuntimeException();
		}
		
		return orderSearchCriteriaBuilder.build();
	}
}
