package org.openmrs.module.fhirExtension.validators;

import ca.uhn.fhir.rest.server.exceptions.UnprocessableEntityException;
import org.hl7.fhir.r4.model.DiagnosticReport;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.openmrs.Order;
import org.openmrs.Patient;
import org.openmrs.api.OrderService;
import org.openmrs.module.fhir2.model.FhirDiagnosticReport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DiagnosticReportBasedOnValidator {
	
	static final String INVALID_ORDER_ERROR_MESSAGE = "Given lab order is not prescribed by the doctor";
	
	@Autowired
	private OrderService orderService;
	
	public void validate(FhirDiagnosticReport fhirDiagnosticReport, DiagnosticReport diagnosticReport) {
		if (diagnosticReport.getBasedOn() != null && diagnosticReport.getBasedOn().get(0).getReference() != null)
			validateBasedOn(fhirDiagnosticReport);
	}
	
	private void validateBasedOn(FhirDiagnosticReport fhirDiagnosticReport) {
        Patient patient = fhirDiagnosticReport.getSubject();
        Integer conceptId = fhirDiagnosticReport.getCode().getId();
        List<Order> allOrders = orderService.getAllOrdersByPatient(patient);
        long matchingOrdersCount = allOrders.stream().filter((order) -> order.isActive()).filter((order) -> order.getConcept().getId() == conceptId).count();
        if(matchingOrdersCount < 1)
            throw new UnprocessableEntityException(INVALID_ORDER_ERROR_MESSAGE, createExceptionErrorOperationOutcome(INVALID_ORDER_ERROR_MESSAGE));
    }
	
	private OperationOutcome createExceptionErrorOperationOutcome(String diagnostics) {
		OperationOutcome outcome = new OperationOutcome();
		OperationOutcome.OperationOutcomeIssueComponent issue = outcome.addIssue();
		issue.setCode(OperationOutcome.IssueType.BUSINESSRULE);
		issue.setSeverity(OperationOutcome.IssueSeverity.ERROR);
		issue.setDiagnostics(diagnostics);
		return outcome;
	}
	
}
