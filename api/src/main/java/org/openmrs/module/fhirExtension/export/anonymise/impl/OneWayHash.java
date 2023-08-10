package org.openmrs.module.fhirExtension.export.anonymise.impl;

import org.springframework.stereotype.Component;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

@Component
public class OneWayHash {
	
	private static final String ALGORITHM = "SHA-256";
	
	private static final Charset UTF_8 = StandardCharsets.UTF_8;
	
	public String toHexDigest(String inputStr, byte[] salt) {
		MessageDigest messageDigest = null;
		try {
			messageDigest = MessageDigest.getInstance(ALGORITHM);
		}
		catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
		messageDigest.update(salt);
		byte[] shaInBytes = messageDigest.digest(inputStr.getBytes(UTF_8));
		messageDigest.reset();
		return bytesToHex(shaInBytes);
	}
	
	private String bytesToHex(byte[] bytes) {
		StringBuilder sb = new StringBuilder();
		for (byte b : bytes) {
			sb.append(String.format("%02x", b));
		}
		return sb.toString();
	}
	
}
