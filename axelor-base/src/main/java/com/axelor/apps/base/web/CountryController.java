package com.axelor.apps.base.web;

import com.axelor.apps.base.service.CountryService;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;

import java.lang.invoke.MethodHandles;
import java.util.List;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CountryController {
  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  public void recomputeAllAddress(ActionRequest request, ActionResponse response) {
    CountryService countryService = Beans.get(CountryService.class);
    Object ids = request.getContext().get("_ids");

    if (ids == null) {
      response.setInfo("Needs to select at least one country");
    } else {
      List<Long> countryIdsList = (List<Long>) ids;
      Pair<Integer, Integer> pair = countryService.recomputeAllAddress(countryIdsList);

      int updatedRecordCount = pair.getLeft();
      int exceptionCount = pair.getRight();
      response.setInfo(
          updatedRecordCount
              + " records updated\n"
              + exceptionCount
              + " records generated exceptions!");
    }
  }
}
