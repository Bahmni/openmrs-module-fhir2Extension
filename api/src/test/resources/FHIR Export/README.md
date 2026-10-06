FHIR Export
-----------
Export patient data in [NDJSON](http://ndjson.org/) format per resource type.

| OpenMRS                                  | FHIR Resource Type |
|------------------------------------------|--------------------|
| Patient                                  | Patient            |
| Condition                                | Condition          |  
| Diagnosis                                | Condition          |
| Procedure Orders (Order Type: Procedure) | Service Request    |
| Procedure Record Template  (Form)        | Procedure          |
| Medication Request                       | Medication Request |   

- REST endpoint (HTTP POST): `<HOST>/openmrs/ws/rest/v1/fhirexport?anonymise=true&startDate=<yyyy-mm-dd>&endDate=<yyyy-mm-dd>`.
- `anonymise` is required and must be `true` or `false`. `startDate` and `endDate` are optional; when provided, dates must use `yyyy-mm-dd`. If omitted, the export uses an unbounded date range.
- This end point gives FHIR task as response. 
- Exporting patient data is asynchronous job and the corresponding FHIR task is updated after completion of job.
- Privilege Required : `Export Patient Data`
- Non-anonymised exports additionally require `Export Non Anonymised Patient Data`. Requests with a missing or invalid `anonymise` value, invalid date, or start date after end date return HTTP 400; requests without the required privilege return HTTP 403.
- File export asynchronous job creates zip file in the directory specified in the global property `fhir.export.files.directory`

Sample Outputs
--------------
POST API Call : `localhost/openmrs/ws/rest/v1/fhirexport?anonymise=true&startDate=2023-07-01&endDate=2023-07-31`

Response :
```
{
    "status": "ACCEPTED",
    "taskId": "e7d9576d-3559-4c36-adb6-ac9e27d94958",
    "link": "http://localhost/openmrs/ws/fhir2/R4/Task/e7d9576d-3559-4c36-adb6-ac9e27d94958"
}
```

Response of FHIR Task

```
{
    "resourceType": "Task",
    "id": "8b38873b-4620-41c2-9dd5-058df267370b",
    "meta": {
        "lastUpdated": "2023-07-04T05:11:53.000+00:00"
    },
    "text": {
        "status": "generated",
        "div": "<div xmlns=\"http://www.w3.org/1999/xhtml\"><table class=\"hapiPropertyTable\"><tbody><tr><td>Id:</td><td>8b38873b-4620-41c2-9dd5-058df267370b</td></tr><tr><td>Identifier:</td><td><div>8b38873b-4620-41c2-9dd5-058df267370b</div></td></tr><tr><td>Status:</td><td>COMPLETED</td></tr><tr><td>Intent:</td><td>ORDER</td></tr><tr><td>Authored On:</td><td>04/07/2023</td></tr><tr><td>Last Modified:</td><td>04/07/2023</td></tr></tbody></table></div>"
    },
    "identifier": [
        {
            "system": "http://fhir.openmrs.org/ext/task/identifier",
            "value": "8b38873b-4620-41c2-9dd5-058df267370b"
        }
    ],
    "status": "completed",
    "intent": "order",
    "authoredOn": "2023-07-04T05:11:53+00:00",
    "lastModified": "2023-07-04T05:14:53+00:00",
    "output": [
        {
            "type": {
                "coding": [
                    {
                        "code": "c0bbaf27-3ddc-4aee-ab7d-eea0693f4d60",
                        "display": "Download URL"
                    }
                ],
                "text": "Download URL"
            },
            "valueString": "http://localhost/openmrs/ws/rest/v1/fhirExtension/export?file=8b38873b-4620-41c2-9dd5-058df267370b"
        }
    ]
}
```

Sample NDJSON files
-------------------
- [Patient](samples/Patient.ndjson)
- [Condition](samples/Condition.ndjson)
- [MedicationRequest](samples/MedicationRequest.ndjson)
- [ServiceRequest](samples/ServiceRequest.ndjson)
- [Procedure](samples/Procedure.ndjson)