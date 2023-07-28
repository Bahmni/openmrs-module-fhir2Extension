package org.openmrs.module.fhirExtension.export.anonymise.factory;

import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.openmrs.module.fhirExtension.export.anonymise.ResourceRedact;
import org.openmrs.module.fhirExtension.export.anonymise.impl.IdentifierResourceRedact;
import org.openmrs.module.fhirExtension.export.anonymise.impl.NameResourceRedact;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
@RunWith(PowerMockRunner.class)
@PowerMockIgnore("javax.management.*")

class RedactFieldHandlerSingletonFactoryTest {


    @Test
    public void shouldReturnRedactHandlerWhenFieldNameIsPassed() {
        String fieldName_1 = "identifier";
        String fieldName_2 = "name";
        ResourceRedact redact_1  = RedactFieldHandlerSingletonFactory.getInstance(fieldName_1);
        ResourceRedact redact_2  = RedactFieldHandlerSingletonFactory.getInstance(fieldName_2);
        assertEquals(redact_1.getClass(), IdentifierResourceRedact.class);
        assertEquals(redact_2.getClass(), NameResourceRedact.class);
    }

    @Test
    public void shouldThrowExceptionWhenInvalidFieldNameIsPassed() {
        String invalidFieldName = "invalidField";
        assertThrows(IllegalArgumentException.class, () -> RedactFieldHandlerSingletonFactory.getInstance(invalidFieldName) );
    }

}