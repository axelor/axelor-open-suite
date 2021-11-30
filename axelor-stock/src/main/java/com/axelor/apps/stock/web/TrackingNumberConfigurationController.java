package com.axelor.apps.stock.web;

import com.axelor.apps.stock.db.TrackingNumberConfiguration;
import com.axelor.apps.stock.service.TrackingNumberConfigurationService;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Singleton;

@Singleton
public class TrackingNumberConfigurationController {

  public void checkSequenceAndBarcodeTypeConfigConsistency(
      ActionRequest request, ActionResponse response) {
    try {
      TrackingNumberConfiguration config =
          request.getContext().asType(TrackingNumberConfiguration.class);
      Beans.get(TrackingNumberConfigurationService.class)
          .checkSequenceAndBarcodeTypeConfigConsistency(config);
    } catch (Exception e) {
      response.setError(e.getLocalizedMessage());
    }
  }
}
