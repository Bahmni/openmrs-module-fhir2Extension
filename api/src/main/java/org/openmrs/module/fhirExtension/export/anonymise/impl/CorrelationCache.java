package org.openmrs.module.fhirExtension.export.anonymise.impl;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class CorrelationCache {
    private Map<String, String> uuidCorrelationMap = new HashMap<>();

    private OneWayHash oneWayHash;

    @Autowired
    CorrelationCache(OneWayHash oneWayHash) {
        this.oneWayHash = oneWayHash;
    }

    public String readDigest(String currentUuid, byte[] salt) {
        String hexDigest = uuidCorrelationMap.get(currentUuid);
        if (StringUtils.isNotBlank(hexDigest)) {
            return hexDigest;
        }
        return updateCacheAndReadDigest(currentUuid, salt);
    }

    private String updateCacheAndReadDigest(String currentUuid, byte[] salt) {
        String hexDigest = oneWayHash.toHexDigest(currentUuid, salt);
        uuidCorrelationMap.put(currentUuid, hexDigest);
        return hexDigest;
    }

    public void reset() {
        uuidCorrelationMap.clear();
    }

}
