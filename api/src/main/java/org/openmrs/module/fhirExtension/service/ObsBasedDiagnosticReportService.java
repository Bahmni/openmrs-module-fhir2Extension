package org.openmrs.module.fhirExtension.service;

import ca.uhn.fhir.model.api.Include;
import ca.uhn.fhir.rest.api.SortSpec;
import ca.uhn.fhir.rest.api.server.IBundleProvider;
import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.ReferenceAndListParam;
import ca.uhn.fhir.rest.param.TokenAndListParam;
import lombok.extern.log4j.Log4j2;
import org.hl7.fhir.r4.model.DiagnosticReport;
import org.openmrs.CareSetting;
import org.openmrs.Encounter;
import org.openmrs.Obs;
import org.openmrs.Order;
import org.openmrs.OrderType;
import org.openmrs.Patient;
import org.openmrs.api.ObsService;
import org.openmrs.api.OrderService;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.FhirDiagnosticReportService;
import org.openmrs.module.fhir2.api.dao.FhirDao;
import org.openmrs.module.fhir2.api.dao.FhirDiagnosticReportDao;
import org.openmrs.module.fhir2.api.impl.BaseFhirService;
import org.openmrs.module.fhir2.api.search.SearchQuery;
import org.openmrs.module.fhir2.api.search.SearchQueryInclude;
import org.openmrs.module.fhir2.api.search.param.SearchParameterMap;
import org.openmrs.module.fhir2.api.translators.OpenmrsFhirTranslator;
import org.openmrs.module.fhir2.model.FhirDiagnosticReport;
import org.openmrs.module.fhirExtension.translators.ObsBasedDiagnosticReportTranslator;
import org.openmrs.module.fhirExtension.validators.DiagnosticReportObsValidator;
import org.openmrs.module.fhirExtension.validators.DiagnosticReportRequestValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Primary
@Component
@Log4j2
@Transactional
public class ObsBasedDiagnosticReportService extends BaseFhirService<DiagnosticReport, FhirDiagnosticReport> implements FhirDiagnosticReportService {
	
	static final String SAVE_OBS_MESSAGE = "Created when saving a Fhir Diagnostic Report";
	
	static final String ORDER_TYPE_NAME = "Lab Order";
	
	@Autowired
	private FhirDiagnosticReportDao fhirDiagnosticReportDao;
	
	@Autowired
	private ObsBasedDiagnosticReportTranslator obsBasedDiagnosticReportTranslator;
	
	@Autowired
	private ObsService obsService;
	
	@Autowired
	private DiagnosticReportObsValidator diagnosticReportObsValidator;
	
	@Autowired
	private DiagnosticReportRequestValidator diagnosticReportRequestValidator;
	
	@Autowired
	private OrderService orderService;
	
	@Autowired
	private SearchQuery<FhirDiagnosticReport, DiagnosticReport, FhirDiagnosticReportDao, ObsBasedDiagnosticReportTranslator, SearchQueryInclude<DiagnosticReport>> searchQuery;
	
	@Autowired
	private SearchQueryInclude<DiagnosticReport> searchQueryInclude;
	
	@Autowired
	private EncounterMatcher encounterMatcher;
	
	@Override
	public DiagnosticReport create(@Nonnull DiagnosticReport diagnosticReport) {
		try {
			diagnosticReportRequestValidator.validate(diagnosticReport);
			FhirDiagnosticReport fhirDiagnosticReport = obsBasedDiagnosticReportTranslator.toOpenmrsType(diagnosticReport);
			diagnosticReportObsValidator.validate(fhirDiagnosticReport);
			
			Order order = getOrder(diagnosticReport, fhirDiagnosticReport);
			Encounter encounter = encounterMatcher.findOrCreateEncounter(fhirDiagnosticReport);
			Set<Obs> reportObs = createReportObs(fhirDiagnosticReport, order, encounter);
			
			fhirDiagnosticReport.setResults(reportObs);
			
			FhirDiagnosticReport createdFhirDiagnosticReport = fhirDiagnosticReportDao.createOrUpdate(fhirDiagnosticReport);
			updateFulFillerStatus(order);
			return obsBasedDiagnosticReportTranslator.toFhirResource(createdFhirDiagnosticReport);
		}
		catch (Exception exception) {
			log.error("Exception while saving diagnostic report: " + exception.getMessage());
			throw exception;
		}
	}
	
	private Set<Obs> createReportObs(FhirDiagnosticReport fhirDiagnosticReport, Order order, Encounter encounter) {
		String SAVE_OBS_MESSAGE = "Created when saving a Fhir Diagnostic Report";

		Set<Obs> diagnosticObs = fhirDiagnosticReport.getResults();
		updateObsWithOrderAndEncounter(diagnosticObs, order, encounter);
		return diagnosticObs.stream()
				.map(obs -> obsService.saveObs(obs, SAVE_OBS_MESSAGE))
				.collect(Collectors.toSet());
	}
	
	@Override
	protected FhirDao<FhirDiagnosticReport> getDao() {
		return fhirDiagnosticReportDao;
	}
	
	@Override
	protected OpenmrsFhirTranslator<FhirDiagnosticReport, DiagnosticReport> getTranslator() {
		return obsBasedDiagnosticReportTranslator;
	}
	
	@Override
	public IBundleProvider searchForDiagnosticReports(ReferenceAndListParam encounterReference,
	        ReferenceAndListParam patientReference, DateRangeParam issueDate, TokenAndListParam code,
	        ReferenceAndListParam result, TokenAndListParam id, DateRangeParam lastUpdated, SortSpec sort,
	        HashSet<Include> includes) {
		SearchParameterMap theParams = new SearchParameterMap()
		        .addParameter(FhirConstants.ENCOUNTER_REFERENCE_SEARCH_HANDLER, encounterReference)
		        .addParameter(FhirConstants.PATIENT_REFERENCE_SEARCH_HANDLER, patientReference)
		        .addParameter(FhirConstants.DATE_RANGE_SEARCH_HANDLER, issueDate)
		        .addParameter(FhirConstants.CODED_SEARCH_HANDLER, code)
		        .addParameter(FhirConstants.RESULT_SEARCH_HANDLER, result)
		        .addParameter(FhirConstants.COMMON_SEARCH_HANDLER, FhirConstants.ID_PROPERTY, id)
		        .addParameter(FhirConstants.COMMON_SEARCH_HANDLER, FhirConstants.LAST_UPDATED_PROPERTY, lastUpdated)
		        .addParameter(FhirConstants.INCLUDE_SEARCH_HANDLER, includes).setSortSpec(sort);
		return searchQuery.getQueryResults(theParams, fhirDiagnosticReportDao, obsBasedDiagnosticReportTranslator,
		    searchQueryInclude);
	}
	
	private Order getOrder(DiagnosticReport diagnosticReport, FhirDiagnosticReport fhirDiagnosticReport) {

		if (!diagnosticReport.getBasedOn().isEmpty()) {
			String orderUuid = diagnosticReport.getBasedOn().get(0).getIdentifier().getValue();
			return orderService.getOrderByUuid(orderUuid);
		} else {
			String careSettingTypeName = CareSetting.CareSettingType.OUTPATIENT.toString();
			CareSetting careSetting = orderService.getCareSettingByName(careSettingTypeName);
			OrderType orderType = orderService.getOrderTypeByName(ORDER_TYPE_NAME);
			Patient patient = fhirDiagnosticReport.getSubject();
			Integer conceptId = fhirDiagnosticReport.getCode().getId();
			List<Order> allOrders = orderService.getOrders(patient, careSetting, orderType, false);
			Optional<Order> order = allOrders.stream()
					.filter(o -> !Order.FulfillerStatus.COMPLETED.equals(o.getFulfillerStatus()))
					.filter(o -> o.getConcept().getId().equals(conceptId))
					.findFirst();
			return order.orElse(null);
		}
	}
	
	private void updateFulFillerStatus(Order order) {
		if (order != null)
			order.setFulfillerStatus(Order.FulfillerStatus.COMPLETED);
	}
	
	private void updateObsWithOrderAndEncounter(Set<Obs> diagnosticObs, Order order, Encounter encounter) {
		diagnosticObs.forEach(obs -> {
			obs.setOrder(order);
			obs.setEncounter(encounter);
			if (obs.hasGroupMembers()) {
				updateObsWithOrderAndEncounter(obs.getGroupMembers(), order, encounter);
			}
		});
	}
}
