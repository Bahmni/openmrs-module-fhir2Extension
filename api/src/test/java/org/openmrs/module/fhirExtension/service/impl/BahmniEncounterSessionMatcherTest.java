package org.openmrs.module.fhirExtension.service.impl;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.openmrs.Encounter;
import org.openmrs.EncounterType;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.Person;
import org.openmrs.Provider;
import org.openmrs.User;
import org.openmrs.Visit;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.EncounterService;
import org.openmrs.api.ProviderService;
import org.openmrs.api.VisitService;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.UserContext;
import org.openmrs.module.fhir2.model.FhirDiagnosticReport;
import org.openmrs.module.fhirExtension.service.EncounterMatcher;
import org.openmrs.parameter.EncounterSearchCriteria;
import org.openmrs.parameter.EncounterSearchCriteriaBuilder;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class BahmniEncounterSessionMatcherTest {
	
	@Mock
	EncounterService encounterService;
	
	@Mock
	ProviderService providerService;
	
	@Mock
	VisitService visitService;
	
	@Mock
	AdministrationService administrationService;
	
	@Test
	public void shouldFindNoMatchingEncounter() {
		EncounterMatcher encounterMatcher = new BahmniEncounterSessionMatcher(encounterService, providerService,
		        visitService, administrationService);
		Patient aPatient = new Patient();
		Date fromDate = Date.from(LocalDateTime.now().minusHours(1).atZone(ZoneId.systemDefault()).toInstant());
		Date endDate = new Date();
		EncounterType labResultsEncType = new EncounterType();
		List<Provider> providersForUser = Collections.singletonList(new Provider());
		Visit visit = new Visit();
		Location userLocation = new Location();
		
		EncounterSearchCriteria searchCriteria = new EncounterSearchCriteriaBuilder().setPatient(aPatient)
		        .setFromDate(fromDate).setToDate(endDate).setEncounterTypes(Collections.singleton(labResultsEncType))
		        .setProviders(providersForUser).setVisits(Collections.singleton(visit)).createEncounterSearchCriteria();
		Optional<Encounter> matchingEncounter = encounterMatcher.findMatchingEncounter(searchCriteria, userLocation);
		
		assertEquals(matchingEncounter.isPresent(), false);
	}
	
	@Test
	public void shouldFindMatchingEncounter() {
		EncounterMatcher encounterMatcher = new BahmniEncounterSessionMatcher(encounterService, providerService,
		        visitService, administrationService);
		Patient aPatient = new Patient();
		
		Date fromDate = Date.from(LocalDateTime.now().minusHours(1).atZone(ZoneId.systemDefault()).toInstant());
		Date endDate = new Date();
		EncounterType labResultsEncType = new EncounterType();
		List<Provider> providersForUser = Collections.singletonList(new Provider());
		Visit visit = new Visit();
		Location userLocation = new Location();
		
		EncounterSearchCriteria searchCriteria = new EncounterSearchCriteriaBuilder().setPatient(aPatient)
		        .setFromDate(fromDate).setToDate(endDate).setEncounterTypes(Collections.singleton(labResultsEncType))
		        .setProviders(providersForUser).setVisits(Collections.singleton(visit)).createEncounterSearchCriteria();
		Encounter encounter = new Encounter();
		encounter.setLocation(userLocation);
		when(encounterService.getEncounters(searchCriteria)).thenReturn(Collections.singletonList(encounter));
		Optional<Encounter> matchingEncounter = encounterMatcher.findMatchingEncounter(searchCriteria, userLocation);
		
		assertEquals(matchingEncounter.isPresent(), true);
	}
	
	@Test
	public void shouldCreateEncounter() {
		EncounterMatcher encounterMatcher = new BahmniEncounterSessionMatcher(encounterService, providerService,
		        visitService, administrationService);
		Patient aPatient = new Patient();
		
		Date fromDate = Date.from(LocalDateTime.now().minusHours(1).atZone(ZoneId.systemDefault()).toInstant());
		Date endDate = new Date();
		EncounterType labResultsEncType = new EncounterType();
		List<Provider> providersForUser = Collections.singletonList(new Provider());
		Visit visit = new Visit();
		Location userLocation = new Location();
		
		EncounterSearchCriteria searchCriteria = new EncounterSearchCriteriaBuilder().setPatient(aPatient)
		        .setFromDate(fromDate).setToDate(endDate).setEncounterTypes(Collections.singleton(labResultsEncType))
		        .setProviders(providersForUser).setVisits(Collections.singleton(visit)).createEncounterSearchCriteria();
		
		when(encounterService.getEncounterType(BahmniEncounterSessionMatcher.LAB_RESULT_ENC_TYPE)).thenReturn(
		    labResultsEncType);
		
		FhirDiagnosticReport diagnosticReport = new FhirDiagnosticReport();
		diagnosticReport.setSubject(aPatient);
		
		User authenticatedUser = new User();
		Person person = new Person();
		authenticatedUser.setPerson(person);
		UserContext mockUserContext = mock(UserContext.class);
		when(mockUserContext.getAuthenticatedUser()).thenReturn(authenticatedUser);
		when(mockUserContext.getLocation()).thenReturn(new Location());
		Context.setUserContext(mockUserContext);
		
		Visit activeVisit = new Visit();
		visit.setPatient(aPatient);
		visit.setLocation(userLocation);
		when(visitService.getActiveVisitsByPatient(aPatient)).thenReturn(Collections.singletonList(visit));
		
		Encounter encounter = new Encounter();
		encounter.setLocation(userLocation);
		when(encounterService.saveEncounter(any())).thenReturn(encounter);
		when(administrationService.getGlobalProperty("bahmni.encountersession.duration")).thenReturn("60");
		
		//		Provider provider = new Provider();
		//		provider.setPerson(person);
		//		when(providerService.getProvidersByPerson(person)).thenReturn(Collections.singletonList(provider));
		
		Encounter enc = encounterMatcher.findOrCreateEncounter(diagnosticReport);
		
		assertEquals(enc, encounter);
	}
	
}
