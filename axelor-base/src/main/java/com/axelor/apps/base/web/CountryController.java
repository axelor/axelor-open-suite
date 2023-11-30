package com.axelor.apps.base.web;

import com.axelor.apps.base.db.Country;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import java.lang.invoke.MethodHandles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CountryController {
  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  public void recomputeAllAddress(ActionRequest request, ActionResponse response) {

    Country country = request.getContext().asType(Country.class);
  }
}
