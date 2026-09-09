package org.openmrs.module.fhirExtension.validators;

import ca.uhn.fhir.rest.server.exceptions.UnprocessableEntityException;
import org.hl7.fhir.r4.model.Attachment;
import org.hl7.fhir.r4.model.DiagnosticReport;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.Reference;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Collections;

import static org.openmrs.module.fhirExtension.validators.DiagnosticReportRequestValidator.RESULT_OR_ATTACHMENT_NOT_PRESENT_ERROR_MESSAGE;
import static org.openmrs.module.fhirExtension.validators.DiagnosticReportRequestValidator.RESOURCE_NOT_PRESENT_FOR_GIVEN_REFERENCE_ERROR_MESSAGE;

public class DiagnosticReportRequestValidatorTest {
	
	private final DiagnosticReportRequestValidator diagnosticReportRequestValidator = new DiagnosticReportRequestValidator();
	
	@Rule
	public ExpectedException thrown = ExpectedException.none();
	
	@Test
	public void shouldThrowExpectedExceptionWhenNeitherResultNorAttachmentIsPresent() {
		DiagnosticReport diagnosticReport = new DiagnosticReport();
		
		thrown.expect(UnprocessableEntityException.class);
		thrown.expectMessage(RESULT_OR_ATTACHMENT_NOT_PRESENT_ERROR_MESSAGE);
		
		diagnosticReportRequestValidator.validate(diagnosticReport);
	}
	
	@Test
	public void shouldNotThrowExceptionWhenResultIsPresent() {
		DiagnosticReport diagnosticReport = new DiagnosticReport();
		Observation observation = new Observation();
		observation.setId("test");
		Reference reference = new Reference("#test");
		reference.setType("Observation");
		reference.setResource(observation);
		diagnosticReport.setResult(Collections.singletonList(reference));
		
		diagnosticReportRequestValidator.validate(diagnosticReport);
	}
	
	@Test
	public void shouldNotThrowExceptionWhenAttachmentsIsPresent() {
		DiagnosticReport diagnosticReport = new DiagnosticReport();
		diagnosticReport.setPresentedForm(Collections.singletonList(new Attachment()));
		
		diagnosticReportRequestValidator.validate(diagnosticReport);
	}
	
	@Test
	public void shouldThrowExpectedExceptionWhenReferencesDoesNotHaveRespectiveResources() {
		DiagnosticReport diagnosticReport = new DiagnosticReport();
		Observation observation = new Observation();
		observation.setId("test");
		Reference reference = new Reference();
		reference.setType("Observation");
		diagnosticReport.setResult(Collections.singletonList(reference));
		diagnosticReport.setPresentedForm(Collections.singletonList(new Attachment()));
		
		thrown.expect(UnprocessableEntityException.class);
		thrown.expectMessage(RESOURCE_NOT_PRESENT_FOR_GIVEN_REFERENCE_ERROR_MESSAGE);
		
		diagnosticReportRequestValidator.validate(diagnosticReport);
	}
	
	@Test
	public void shouldNotThrowExpectedExceptionWhenStringReferencesIsProvided() {
		DiagnosticReport diagnosticReport = new DiagnosticReport();
		Reference reference = new Reference("Observation/some-test-uuid");
		reference.setType("Observation");
		diagnosticReport.setResult(Collections.singletonList(reference));
		diagnosticReport.setPresentedForm(Collections.singletonList(new Attachment()));
		
		diagnosticReportRequestValidator.validate(diagnosticReport);
	}
	
	@Test
	public void shouldNotThrowExceptionWhenReferencesHaveRespectiveResources() {
		DiagnosticReport diagnosticReport = new DiagnosticReport();
		Observation observation = new Observation();
		observation.setId("test");
		Reference reference = new Reference("#test");
		reference.setType("Observation");
		reference.setResource(observation);
		diagnosticReport.setResult(Collections.singletonList(reference));
		diagnosticReport.setPresentedForm(Collections.singletonList(new Attachment()));
		
		diagnosticReportRequestValidator.validate(diagnosticReport);
	}
}
