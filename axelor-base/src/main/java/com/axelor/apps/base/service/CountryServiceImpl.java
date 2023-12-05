package com.axelor.apps.base.service;

import com.axelor.inject.Beans;
import com.google.inject.Inject;

import java.util.List;

public class CountryServiceImpl implements CountryService {

  private AddressService addressService;

  @Inject
  public CountryServiceImpl(AddressService addressService){
    this.addressService = addressService;
  }

  @Override
  public int recomputeAllAddress(List<Long> countryIdsList) {
    int updatedRecordCount = 0;
    updatedRecordCount = addressService.computeFormattedAddressForCountries(countryIdsList);
    return updatedRecordCount;
  }
}
