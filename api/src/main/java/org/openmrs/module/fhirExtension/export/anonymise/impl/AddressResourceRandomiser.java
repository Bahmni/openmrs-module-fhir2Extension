package org.openmrs.module.fhirExtension.export.anonymise.impl;

import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Address;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.StringType;
import org.openmrs.module.fhirExtension.export.anonymise.ResourceRandomise;

import java.util.List;

public class AddressResourceRandomiser implements ResourceRandomise {
	
	private AddressResourceRandomiser() {
		
	}
	
	public static AddressResourceRandomiser getInstance() {
		return AddressResourceRandomiser.SingletonHelper.INSTANCE;
	}
	
	@Override
    public void randomise(IBaseResource iBaseResource, String fixedValue) {
        Patient patient = (Patient) iBaseResource;
        if (fixedValue != null && patient.hasAddress()) {
            List<Address> addressList = patient.getAddress();
            addressList.forEach(address -> fixedAddressHandler(address, fixedValue));
            return;
        }
        randomHandler(patient);
    }
	
	private void randomHandler(Patient patient) {
        List<Address> addressList = patient.getAddress();
        addressList.forEach(this::randomAddressHandler);
    }
	
	private void randomAddressHandler(Address address) {
        address.setDistrict(getRandomAlphabets(address.getDistrict()));
        address.setCity(getRandomAlphabets(address.getCity()));
        address.setState(getRandomAlphabets(address.getState()));
        address.setCountry(getRandomAlphabets(address.getCountry()));

        if (address.hasExtension()) {
            address.getExtension().forEach(this::randomAddressExtensionHandler);
        }
    }
	
	private void randomAddressExtensionHandler(Extension extension) {
        if (extension.hasExtension()) {
            extension.getExtension().forEach(this::randomAddressExtensionHandler);
        }
        if (extension.hasValue()) {
            extension.setValue(new StringType(getRandomAlphaNumeric(extension.getValue().primitiveValue())));
        }
    }
	
	private void fixedAddressHandler(Address address, String fixedValue) {
        address.setDistrict(fixedValue);
        address.setCity(fixedValue);
        address.setState(fixedValue);
        address.setCountry(fixedValue);
        if (address.hasExtension()) {
            address.getExtension().forEach(extension -> fixedAddressExtensionHandler(extension, fixedValue));
        }
    }
	
	private void fixedAddressExtensionHandler(Extension extension, String fixedValue) {
        if (extension.hasExtension()) {
            extension.getExtension().forEach(childExtension -> fixedAddressExtensionHandler(childExtension, fixedValue));
        }
        if (extension.hasValue()) {
            extension.setValue(new StringType(fixedValue));
        }
    }
	
	private static class SingletonHelper {
		
		private static final AddressResourceRandomiser INSTANCE = new AddressResourceRandomiser();
	}
}
