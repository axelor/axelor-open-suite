package com.axelor.apps.base.service;

import com.axelor.inject.Beans;
import java.util.List;

public class CountryServiceImpl implements CountryService {
  @Override
  public int recomputeAllAddress(List<Long> countryIdsList) {
    int updatedRecordCount = 0;
    AddressService addressService = Beans.get(AddressService.class);
    updatedRecordCount = addressService.computeFormattedAddressForCountries(countryIdsList);
    return updatedRecordCount;
  }
}
