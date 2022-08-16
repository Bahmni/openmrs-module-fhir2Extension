package org.openmrs.module.fhirExtension.service;

import org.hl7.fhir.r4.model.DiagnosticReport;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Reference;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.openmrs.Concept;
import org.openmrs.Obs;
import org.openmrs.Order;
import org.openmrs.Patient;
import org.openmrs.api.OrderService;
import org.openmrs.module.fhir2.model.FhirDiagnosticReport;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static java.util.stream.Collectors.toSet;
import static java.util.stream.Stream.of;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.junit.Assert.assertEquals;
import static org.openmrs.module.fhirExtension.translators.impl.DiagnosticReportObsLabResultTranslatorImpl.*;

@RunWith(MockitoJUnitRunner.class)
public class OrderUtilTest {
	
	private static final String REPORT_URL = "/100/uploadReport.pdf";
	
	private static final String REPORT_NAME = "bloodTest.pdf";
	
	private static final String LAB_TEST_NOTES = "Report is normal";
	
	@Mock
	private OrderService orderService;
	
	@InjectMocks
	private OrderUtil orderUtil;
	
	@Test
	public void shouldUpdateOrderFulfillerStatusWhenPendingLabOrderIsUploaded() {
		String orderUuid = "123";
		Identifier identifier = new Identifier();
		identifier.setValue(orderUuid);
		DiagnosticReport diagnosticReport = new DiagnosticReport();
		FhirDiagnosticReport fhirDiagnosticReport = new FhirDiagnosticReport();
		Reference reference = new Reference("ServiceRequest");
		reference.setDisplay("Platelet Count");
		reference.setIdentifier(identifier);
		List<Reference> basedOn = Collections.singletonList(reference);
		diagnosticReport.setBasedOn(basedOn);
		Order order = new Order();
		order.setUuid(orderUuid);
		
		when(orderService.getOrderByUuid(orderUuid)).thenReturn(order);
		
		orderUtil.updateOrder(diagnosticReport, fhirDiagnosticReport);
		
		assertEquals(Order.FulfillerStatus.COMPLETED, order.getFulfillerStatus());
	}
	
	@Test
	public void shouldUpdateOrderFulfillerStatusWhenUploadingReportAgainstAPendingLabOrder() {
		DiagnosticReport diagnosticReport = new DiagnosticReport();
		FhirDiagnosticReport fhirDiagnosticReport = new FhirDiagnosticReport();
		Patient patient = new Patient(123);
		Concept concept = new Concept(12);
		fhirDiagnosticReport.setSubject(patient);
		fhirDiagnosticReport.setCode(concept);
		Order order1 = new Order();
		order1.setUuid("uuid1");
		order1.setConcept(concept);
		Order order2 = new Order();
		order2.setUuid("uuid2");
		
		when(orderService.getAllOrdersByPatient(patient)).thenReturn(Arrays.asList(order1, order2));
		
		orderUtil.updateOrder(diagnosticReport, fhirDiagnosticReport);
		
		assertEquals(Order.FulfillerStatus.COMPLETED, order1.getFulfillerStatus());
	}
	
	@Test
	public void shouldUpdateObsWithGivenOrderWhenUploadingReportAgainstAPendingLabOrder() {
		DiagnosticReport diagnosticReport = new DiagnosticReport();
		FhirDiagnosticReport fhirDiagnosticReport = new FhirDiagnosticReport();
		Patient patient = new Patient(123);
		Concept concept = new Concept(12);
		fhirDiagnosticReport.setSubject(patient);
		fhirDiagnosticReport.setCode(concept);
		Order order = new Order();
		order.setUuid("uuid1");
		order.setConcept(concept);
		order.setOrderId(1);
		
		when(orderService.getAllOrdersByPatient(patient)).thenReturn(Arrays.asList(order));
		Order currentOrder = orderUtil.getOrder(diagnosticReport, fhirDiagnosticReport);
		
		Obs topLevelObs = new Obs();
		Obs labObs = new Obs();
		labObs.setGroupMembers(of(childObs(LAB_REPORT_CONCEPT, REPORT_URL), childObs(LAB_RESULT_CONCEPT, REPORT_NAME),
		    childObs(LAB_NOTES_CONCEPT, LAB_TEST_NOTES)).collect(toSet()));
		topLevelObs.addGroupMember(labObs);
		
		orderUtil.updateObsWithOrder(Collections.singleton(topLevelObs), currentOrder);
		
		assertEquals(topLevelObs.getOrder(), currentOrder);
		Set<Obs> obsGroupMembersSecondLevel = topLevelObs.getGroupMembers();
		assertEquals(obsGroupMembersSecondLevel.iterator().next().getOrder(), currentOrder);
		
		Obs obsModelThirdLevel = obsGroupMembersSecondLevel.iterator().next();
		Set<Obs> obsGroupMembersThirdLevel = obsModelThirdLevel.getGroupMembers();
		Obs reportObs = fetchObs(obsGroupMembersThirdLevel, "LAB_REPORT");
		assertEquals(reportObs.getOrder(), currentOrder);
		Obs resultObs = fetchObs(obsGroupMembersThirdLevel, "LAB_RESULT");
		assertEquals(resultObs.getOrder(), currentOrder);
		Obs notesObs = fetchObs(obsGroupMembersThirdLevel, "LAB_NOTES");
		assertEquals(notesObs.getOrder(), currentOrder);
	}
	
	private Obs childObs(String concept, String value) {
		Obs obs = new Obs();
		obs.setConcept(newMockConcept(concept));
		obs.setValueText(value);
		return obs;
	}
	
	private Concept newMockConcept(String conceptName) {
		Concept concept = mock(Concept.class);
		when(concept.getDisplayString()).thenReturn(conceptName);
		return concept;
	}
	
	private Obs fetchObs(Set<Obs> obsSet, String conceptName) {
		return obsSet.stream().filter(obs -> obs.getConcept().getDisplayString().equals(conceptName)).findAny().get();
	}
}
