package com.axelor.apps.base.service;

import com.google.inject.Inject;
import java.util.List;
import org.apache.commons.lang3.tuple.Pair;

public class CountryServiceImpl implements CountryService {

  private AddressService addressService;

  @Inject
  public CountryServiceImpl(AddressService addressService) {
    this.addressService = addressService;
  }

  @Override
  public Pair<Integer, Integer> recomputeAllAddress(List<Long> countryIdsList) {
    int updatedRecordCount = 0;
    Pair<Integer, Integer> pair =
        addressService.computeFormattedAddressForCountries(countryIdsList);
    return pair;
  }
}
