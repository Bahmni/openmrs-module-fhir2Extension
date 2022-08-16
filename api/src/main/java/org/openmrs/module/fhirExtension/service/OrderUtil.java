package org.openmrs.module.fhirExtension.service;

import org.hl7.fhir.r4.model.DiagnosticReport;
import org.openmrs.Obs;
import org.openmrs.Order;
import org.openmrs.Patient;
import org.openmrs.api.OrderService;
import org.openmrs.module.fhir2.model.FhirDiagnosticReport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Component
public class OrderUtil {
	
	private final OrderService orderService;
	
	@Autowired
	public OrderUtil(OrderService orderService) {
		this.orderService = orderService;
	}
	
	public Order getOrder(DiagnosticReport diagnosticReport, FhirDiagnosticReport fhirDiagnosticReport) {
		return getOrderDetails(diagnosticReport, fhirDiagnosticReport);
	}
	
	public void updateOrder(DiagnosticReport diagnosticReport, FhirDiagnosticReport fhirDiagnosticReport) {
		Order currentOrder = getOrderDetails(diagnosticReport, fhirDiagnosticReport);
		if (currentOrder != null)
			currentOrder.setFulfillerStatus(Order.FulfillerStatus.COMPLETED);
	}
	
	public void updateObsWithOrder(Set<Obs> results, Order order) {
		results.forEach(obs -> {
			obs.setOrder(order);
			if (obs.hasGroupMembers()) {
				updateObsWithOrder(obs.getGroupMembers(), order);
			}
		});
	}
	
	private Order getOrderDetails(DiagnosticReport diagnosticReport, FhirDiagnosticReport fhirDiagnosticReport) {
		Order currentOrder = null;
		if (!diagnosticReport.getBasedOn().isEmpty()) {
			String orderUuid = diagnosticReport.getBasedOn().get(0).getIdentifier().getValue();
			currentOrder = orderService.getOrderByUuid(orderUuid);
		} else {
			Patient patient = fhirDiagnosticReport.getSubject();
			Integer conceptId = fhirDiagnosticReport.getCode().getId();
			List<Order> allOrders = orderService.getAllOrdersByPatient(patient);
			Optional<Order> optionalOrder = allOrders.stream()
					.filter(order -> !Order.FulfillerStatus.COMPLETED.equals(order.getFulfillerStatus()))
					.filter(order -> !order.getVoided())
					.filter((order) -> order.getConcept().getId().equals(conceptId))
					.findFirst();
			if (optionalOrder.isPresent()) {
				currentOrder = optionalOrder.get();
			}
		}
		return currentOrder;
	}
}
