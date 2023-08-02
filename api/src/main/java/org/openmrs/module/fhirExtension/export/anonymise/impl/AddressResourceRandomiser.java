package org.openmrs.module.fhirExtension.export.anonymise.impl;

import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Address;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.Patient;
import org.openmrs.module.fhirExtension.export.anonymise.ResourceRandomise;

import java.util.List;

public class AddressResourceRandomiser implements ResourceRandomise {
    private AddressResourceRandomiser() {

    }
    @Override
    public void randomise(IBaseResource iBaseResource, String fixedValue) {
        Patient patient = (Patient) iBaseResource;
        if(fixedValue != null && patient.hasAddress()) {
            List<Address> addressList = patient.getAddress();
            addressList.forEach(address -> handleAddress(address, fixedValue));
        }
    }
    private void handleAddress(Address address, String fixedValue) {
            address.setCity(fixedValue);
            address.setState(fixedValue);
            address.setCountry(fixedValue);
            if (address.hasExtension()) {
               address.getExtension().forEach(extension -> handleAddressExtension(extension, fixedValue));
            }
    }
    private void handleAddressExtension(Extension extension, String fixedValue) {
        if (extension.hasExtension()) {
            handleAddressExtension(extension, fixedValue);
        }
//        extension.setValue();
    }
    private static class SingletonHelper {

        private static final AddressResourceRandomiser INSTANCE = new AddressResourceRandomiser();
    }

    public static AddressResourceRandomiser getInstance() {
        return AddressResourceRandomiser.SingletonHelper.INSTANCE;
    }
}
