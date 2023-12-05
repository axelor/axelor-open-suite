package com.axelor.apps.base.service;

import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

public interface CountryService {
  public Pair<Integer, Integer> recomputeAllAddress(List<Long> countryIdsList);
}
