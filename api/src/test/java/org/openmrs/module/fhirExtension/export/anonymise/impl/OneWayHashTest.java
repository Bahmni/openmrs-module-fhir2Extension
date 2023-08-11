package org.openmrs.module.fhirExtension.export.anonymise.impl;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.junit.jupiter.api.Assertions.assertEquals;

@RunWith(PowerMockRunner.class)
@PowerMockIgnore("javax.management.*")
public class OneWayHashTest {
	
	@Test
	public void shouldCreateValidOneWayHash_whenInputIsPassed() {
		OneWayHash oneWayHash = new OneWayHash();
		String inputStr = "dummy";
		String saltStr = "dummySalt";
		assertEquals("8ad82d571829420858dc4d5eac578184c45d8278c95114d8a55a076942a2bba4",
		    oneWayHash.toHexDigest(inputStr, saltStr.getBytes()));
	}
}
