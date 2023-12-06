package com.axelor.apps.base.service;

import java.util.List;
import org.apache.commons.lang3.tuple.Pair;

public interface CountryService {
  public Pair<Integer, Integer> recomputeAllAddress(List<Long> countryIdsList);
}
