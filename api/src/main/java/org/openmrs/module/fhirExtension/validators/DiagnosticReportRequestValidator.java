package org.openmrs.module.fhirExtension.validators;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.rest.server.exceptions.UnprocessableEntityException;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.DiagnosticReport;
import org.openmrs.Order;
import org.openmrs.api.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static org.openmrs.module.fhir2.api.util.FhirUtils.createExceptionErrorOperationOutcome;

@Component
public class DiagnosticReportRequestValidator {
	
	static final String INVALID_ORDER_ERROR_MESSAGE = "Given lab order is not prescribed by the doctor";
	
	static final String RESULT_OR_ATTACHMENT_NOT_PRESENT_ERROR_MESSAGE = "Given lab order does not have any test results or attachments";
	
	static final String RESOURCE_NOT_PRESENT_FOR_GIVEN_REFERENCE_ERROR_MESSAGE = "Given references does not have any matching resource";
	
	@Autowired
	private OrderService orderService;
	
	public void validate(DiagnosticReport diagnosticReport) {
		if (diagnosticReport.getBasedOn().size() > 0 && diagnosticReport.getBasedOn().get(0).getReference() != null)
			validateBasedOn(diagnosticReport);
		validateEitherResultOrAttachmentIsPresent(diagnosticReport);
		validateReferencesHaveRespectiveResources(diagnosticReport);
	}
	
	private void validateBasedOn(DiagnosticReport diagnosticReport) {
		String orderUuid = diagnosticReport.getBasedOn().get(0).getIdentifier().getValue();
		Order order = orderService.getOrderByUuid(orderUuid);
		if (order == null || Order.FulfillerStatus.COMPLETED.equals(order.getFulfillerStatus()) || order.getVoided()
		        || !order.getConcept().getUuid().equals(diagnosticReport.getCode().getCoding().get(0).getCode()))
			throw new UnprocessableEntityException(INVALID_ORDER_ERROR_MESSAGE,
			        createExceptionErrorOperationOutcome(INVALID_ORDER_ERROR_MESSAGE));
	}
	
	private void validateEitherResultOrAttachmentIsPresent(DiagnosticReport diagnosticReport) {
		if (diagnosticReport.getPresentedForm().size() == 0 && diagnosticReport.getResult().size() == 0)
			throw new UnprocessableEntityException(RESULT_OR_ATTACHMENT_NOT_PRESENT_ERROR_MESSAGE,
			        createExceptionErrorOperationOutcome(RESULT_OR_ATTACHMENT_NOT_PRESENT_ERROR_MESSAGE));
	}
	
	private void validateReferencesHaveRespectiveResources(DiagnosticReport diagnosticReport) {
		if (diagnosticReport.getResult().size() != 0) {
			diagnosticReport.getResult().forEach(reference -> {
				IBaseResource resource = reference.getResource();
				if (resource == null)
					if (reference.getReference() == null) {
						throw new UnprocessableEntityException(
							RESOURCE_NOT_PRESENT_FOR_GIVEN_REFERENCE_ERROR_MESSAGE,
							createExceptionErrorOperationOutcome(
									RESOURCE_NOT_PRESENT_FOR_GIVEN_REFERENCE_ERROR_MESSAGE));
					}
			});
		}
	}
}
