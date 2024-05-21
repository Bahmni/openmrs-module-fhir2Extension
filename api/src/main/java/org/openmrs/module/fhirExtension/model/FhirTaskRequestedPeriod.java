package org.openmrs.module.fhirExtension.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.openmrs.module.fhir2.model.FhirTask;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

/**
 * FHIR Task.requestedPeriod - https://build.fhir.org/task-definitions.html#Task.requestedPeriod
 * Indicates the start and/or end of the period of time when completion of the task is desired to
 * take place.
 */

//TODO: Need to migrate this data to FhirTask itself & remove this entity once openmrs starts supporting R5 fhir version

@Data
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@Entity
@Table(name = "fhir_task_requested_period")
public class FhirTaskRequestedPeriod implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	@Id
	@OneToOne
	@JoinColumn(name = "task_id", referencedColumnName = "task_id")
	private FhirTask task;
	
	@Column(name = "requested_start_time")
	private Date requestedStartTime;
	
	@Column(name = "requested_end_time")
	private Date requestedEndTime;
	
}
