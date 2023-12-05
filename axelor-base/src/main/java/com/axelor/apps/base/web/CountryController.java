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

    if (ids == null) {
      response.setInfo("Needs to select at least one country");
    }
    else{

      int updatedRecordCount = 0;
      List<Long> countryIdsList = (List<Long>) ids;
      updatedRecordCount = countryService.recomputeAllAddress(countryIdsList);

      response.setInfo(updatedRecordCount + " records updated");
    }


  }
}
