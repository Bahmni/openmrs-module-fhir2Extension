package org.openmrs.module.fhirExtension.export.anonymise.impl;

import org.junit.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
