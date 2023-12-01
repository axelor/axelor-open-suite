package com.axelor.apps.base.web;

import com.axelor.apps.base.service.CountryService;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import java.util.List;

public class CountryController {

  public void recomputeAllAddress(ActionRequest request, ActionResponse response) {
    CountryService countryService = Beans.get(CountryService.class);
    Object ids = request.getContext().get("_ids");
    List<Long> countryIdsList = (List<Long>) ids;

    int updatedRecordCount = 0;
    if (ids != null) {
      updatedRecordCount = countryService.recomputeAllAddress(countryIdsList);
    }

    response.setInfo(updatedRecordCount + " records Updated");
  }
}
