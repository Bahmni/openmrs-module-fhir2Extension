package org.openmrs.module.fhirExtension.service.impl;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.time.DateUtils;
import org.openmrs.Encounter;
import org.openmrs.EncounterType;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.Provider;
import org.openmrs.Visit;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.EncounterService;
import org.openmrs.api.ProviderService;
import org.openmrs.api.VisitService;
import org.openmrs.api.context.Context;
import org.openmrs.module.fhir2.model.FhirDiagnosticReport;
import org.openmrs.module.fhirExtension.service.EncounterMatcher;
import org.openmrs.parameter.EncounterSearchCriteria;
import org.openmrs.parameter.EncounterSearchCriteriaBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@Log4j2
@Transactional
public class BahmniEncounterSessionMatcher implements EncounterMatcher {
	
	static final String UNABLE_TO_PROCESS_DIAGNOSTIC_REPORT = "Can not process Diagnostic Report. Please check with your administrator.";
	
	static final String LOCATION_TAG_SUPPORTS_VISITS = "Visit Location";
	
	public static final String LAB_RESULT_ENC_TYPE = "LAB_RESULT";
	
	private final EncounterService encounterService;
	
	private final ProviderService providerService;
	
	private final VisitService visitService;
	
	private final AdministrationService adminService;
	
	@Autowired
	public BahmniEncounterSessionMatcher(EncounterService encounterService, ProviderService providerService,
	    VisitService visitService, @Qualifier("adminService") AdministrationService adminService) {
		this.encounterService = encounterService;
		this.providerService = providerService;
		this.visitService = visitService;
		this.adminService = adminService;
	}
	
	public Encounter findOrCreateEncounter(FhirDiagnosticReport diagnosticReport) {
		if (diagnosticReport.getEncounter() != null) {
			return diagnosticReport.getEncounter();
		}
		
		EncounterType encounterType = encounterService.getEncounterType(LAB_RESULT_ENC_TYPE);
		if (encounterType == null) {
			log.error("Encounter type LAB_RESULT must be defined to support Diagnostic Report");
			throw new RuntimeException(UNABLE_TO_PROCESS_DIAGNOSTIC_REPORT);
		}
		
		Location location = Context.getUserContext().getLocation(); //TODO if not present get from clinic
		if (location == null) {
			log.error("Logged in location for user is null. Can not identify encounter session.");
			throw new RuntimeException(UNABLE_TO_PROCESS_DIAGNOSTIC_REPORT);
		}
		
		Collection<Provider> providersForUser = Optional.ofNullable(
		    providerService.getProvidersByPerson(Context.getAuthenticatedUser().getPerson()))
		        .orElse(Collections.emptyList());
		
		Optional<Visit> activeVisit = getActiveVisit(diagnosticReport.getSubject(), null);
		if (!activeVisit.isPresent()) {
			log.error("Can not identify an active visit for the patient");
			throw new RuntimeException(UNABLE_TO_PROCESS_DIAGNOSTIC_REPORT);
		}
		
		Date currentSessionTime = new Date();
		EncounterSearchCriteria searchCriteria = new EncounterSearchCriteriaBuilder()
		        .setPatient(diagnosticReport.getSubject()).setFromDate(getEncounterSessionStartDate(currentSessionTime))
		        .setToDate(currentSessionTime).setEncounterTypes(Collections.singleton(encounterType))
		        .setProviders(providersForUser).setVisits(Collections.singleton(activeVisit.get()))
		        .createEncounterSearchCriteria();
		
		Optional<Encounter> matchingEncounter = findMatchingEncounter(searchCriteria, location);
		if (matchingEncounter.isPresent()) {
			return matchingEncounter.get();
		}
		
		Encounter encounter = new Encounter();
		encounter.setVisit(activeVisit.get());
		encounter.setPatient(diagnosticReport.getSubject());
		encounter.setEncounterType(encounterType);
		encounter.setUuid(UUID.randomUUID().toString());
		encounter.setEncounterDatetime(new Date());
		encounter.setLocation(location);
		return encounterService.saveEncounter(encounter);
	}
	
	public Optional<Encounter> findMatchingEncounter(EncounterSearchCriteria searchCriteria, Location userLocation) {
        if (userLocation == null) {
            return Optional.empty();
        }
        Location userVisitLocation = visitLocationFor(userLocation);
        List<Encounter> matchingEncounters = this.encounterService.getEncounters(searchCriteria);

        if (matchingEncounters == null) {
            return Optional.empty();
        }
        if (CollectionUtils.isEmpty(searchCriteria.getProviders())) {
            matchingEncounters = matchingEncounters.stream()
                    .filter(e -> CollectionUtils.isEmpty(e.getEncounterProviders())
                            && (e.getCreator().getId().equals(Context.getAuthenticatedUser().getId())))
                    .collect(Collectors.toList());
        }
        matchingEncounters = matchingEncounters.stream().filter(e -> visitLocationFor(userVisitLocation).equals(e.getLocation())).collect(Collectors.toList());

        if (matchingEncounters.size() > 1) {
            throw new RuntimeException("Can not identify a unique encounter session. More than one encounter matches the criteria.");
        }

        if (!matchingEncounters.isEmpty()) {
            return Optional.of(matchingEncounters.get(0));
        }
        return Optional.empty();
    }
	
	private Date getEncounterSessionStartDate(Date endDate) {
		Date startDate = DateUtils.addMinutes(endDate, getSessionDuration() * -1);
		if (!DateUtils.isSameDay(startDate, endDate)) {
			return DateUtils.truncate(endDate, Calendar.DATE);
		}
		return startDate;
	}
	
	private int getSessionDuration() {
		String configuredSessionDuration = adminService.getGlobalProperty("bahmni.encountersession.duration");
		int sessionDurationInMinutes = 60;
		if (configuredSessionDuration != null) {
			sessionDurationInMinutes = Integer.parseInt(configuredSessionDuration);
		}
		return sessionDurationInMinutes;
	}
	
	private Location visitLocationFor(Location location) {
		if (location.getParentLocation() == null) {
			return location;
		}
		return location.hasTag(LOCATION_TAG_SUPPORTS_VISITS) ? location : visitLocationFor(location.getParentLocation());
	}
	
	private Optional<Visit> getActiveVisit(Patient patient, String visitLocationUuid) {
		List<Visit> activeVisits = visitService.getActiveVisitsByPatient(patient);
		if (CollectionUtils.isEmpty(activeVisits)) {
			return Optional.empty();
		}
		if (visitLocationUuid != null) {
			return activeVisits.stream().filter(v -> {
				Location visitLocation = v.getLocation();
				return visitLocation != null && (visitLocation.getUuid()).equals(visitLocationUuid);
			}).findFirst();
		}
		return Optional.of(activeVisits.get(0));
	}
}
