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
import org.openmrs.EncounterProvider;
import org.openmrs.EncounterRole;
import org.openmrs.EncounterType;
import org.openmrs.Location;
import org.openmrs.Obs;
import org.openmrs.Order;
import org.openmrs.OrderType;
import org.openmrs.Patient;
import org.openmrs.Provider;
import org.openmrs.User;
import org.openmrs.Visit;
import org.openmrs.api.ObsService;
import org.openmrs.api.OrderService;
import org.openmrs.api.context.Context;
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
import org.springframework.util.CollectionUtils;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Primary
@Component
@Log4j2
@Transactional
public class ObsBasedDiagnosticReportService extends BaseFhirService<DiagnosticReport, FhirDiagnosticReport> implements FhirDiagnosticReportService {
	
	static final String SAVE_OBS_MESSAGE = "Created when saving a Fhir Diagnostic Report";
	
	static final String ORDER_TYPE_NAME = "Lab Order";
	
	public static final String LAB_RESULTS_ENCOUNTER_ROLE = "Supporting services";
	
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
	
	@Override
	public DiagnosticReport create(@Nonnull DiagnosticReport diagnosticReport) {
		try {
			diagnosticReportRequestValidator.validate(diagnosticReport);
			FhirDiagnosticReport fhirDiagnosticReport = obsBasedDiagnosticReportTranslator.toOpenmrsType(diagnosticReport);
			diagnosticReportObsValidator.validate(fhirDiagnosticReport);
			
			Order order = getOrder(diagnosticReport, fhirDiagnosticReport);
			Encounter encounter = createNewEncounter(fhirDiagnosticReport);
			fhirDiagnosticReport.setEncounter(encounter);
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
	
	static final String UNABLE_TO_PROCESS_DIAGNOSTIC_REPORT = "Can not process Diagnostic Report. Please check with your administrator.";
	
	private Encounter createNewEncounter(FhirDiagnosticReport fhirDiagnosticReport) {
		if (fhirDiagnosticReport.getEncounter() != null) {
			log.info("Diagnostic Report was submitted with an existing encounter reference. This will be overwritten by a new encounter");
		}
		
		EncounterType encounterType = Context.getEncounterService().getEncounterType("LAB_RESULT");
		if (encounterType == null) {
			log.error("Encounter type LAB_RESULT must be defined to support Diagnostic Report");
			throw new RuntimeException(UNABLE_TO_PROCESS_DIAGNOSTIC_REPORT);
		}
		
		Location location = Context.getUserContext().getLocation(); //TODO if not present get from clinic
		if (location == null) {
			log.error("Logged in location for user is null. Can not identify encounter session.");
			throw new RuntimeException(UNABLE_TO_PROCESS_DIAGNOSTIC_REPORT);
		}
		
		Optional<Visit> activeVisit = getActiveVisit(fhirDiagnosticReport.getSubject());
		if (!activeVisit.isPresent()) {
			log.error("Can not identify an active visit for the patient");
			throw new RuntimeException(UNABLE_TO_PROCESS_DIAGNOSTIC_REPORT);
		}
		
		return Context.getEncounterService().saveEncounter(
		    newEncounterInstance(fhirDiagnosticReport.getSubject(), encounterType, location, activeVisit.get(),
		        Context.getAuthenticatedUser()));
	}
	
	private Encounter newEncounterInstance(Patient patient, EncounterType encounterType, Location location, Visit activeVisit, User user) {
		Collection<Provider> providersForUser = Optional.ofNullable(Context.getProviderService().getProvidersByPerson(user.getPerson())).orElse(Collections.emptyList());
		Encounter encounter = new Encounter();
		encounter.setVisit(activeVisit);
		encounter.setPatient(patient);
		encounter.setEncounterType(encounterType);
		encounter.setUuid(UUID.randomUUID().toString());
		encounter.setEncounterDatetime(new Date());
		encounter.setLocation(location);
		EncounterRole encounterRole = getEncounterRoleForLabResults();
		Set<EncounterProvider> encounterProviders = providersForUser.stream().map(prov -> {
			EncounterProvider encounterProvider = new EncounterProvider();
			encounterProvider.setEncounter(encounter);
			encounterProvider.setProvider(prov);
			encounterProvider.setEncounterRole(encounterRole);
			return encounterProvider;
		}).collect(Collectors.toSet());
		encounter.setEncounterProviders(encounterProviders);
		encounter.setCreator(user);
		return encounter;
	}
	
	EncounterRole getEncounterRoleForLabResults() {
		return Optional.ofNullable(Context.getEncounterService().getEncounterRoleByName(LAB_RESULTS_ENCOUNTER_ROLE)).orElse(
		    Context.getEncounterService().getEncounterRoleByUuid(EncounterRole.UNKNOWN_ENCOUNTER_ROLE_UUID));
	}
	
	private Optional<Visit> getActiveVisit(Patient patient) {
		List<Visit> activeVisits = Context.getVisitService().getActiveVisitsByPatient(patient);
		if (CollectionUtils.isEmpty(activeVisits)) {
			return Optional.empty();
		}
		return Optional.of(activeVisits.get(0));
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
