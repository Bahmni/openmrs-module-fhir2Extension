package org.openmrs.module.fhirExtension.export.anonymise.impl;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@RunWith(PowerMockRunner.class)
@PowerMockIgnore("javax.management.*")
public class CorrelationCacheTest {

    @InjectMocks
    CorrelationCache correlationCache;

    @Mock
    OneWayHash oneWayHash;

    @Test
    public void shouldInvokeOneWayHashOnlyForFirstTime_whenCacheIsReadAnyNumberOfTimes() {
        when(oneWayHash.toHexDigest(anyString(), any())).thenReturn(
                "8ad82d571829420858dc4d5eac578184c45d8278c95114d8a55a076942a2bba4");
        String inputStr = "dummy";
        String saltStr = "dummySalt";
        correlationCache.readDigest(inputStr, saltStr.getBytes());
        verify(oneWayHash, times(1)).toHexDigest(anyString(), any());
        correlationCache.readDigest(inputStr, saltStr.getBytes());
        verify(oneWayHash, times(1)).toHexDigest(anyString(), any());
    }

}
