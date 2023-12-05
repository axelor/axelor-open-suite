package com.axelor.apps.base.service;

import java.util.List;

public interface CountryService {
  public int recomputeAllAddress(List<Long> countryIdsList);
}
