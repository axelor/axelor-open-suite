package com.axelor.apps.base.service.pricing;

import com.axelor.rpc.Response;

public interface PricingMetaService {
  Response managePricing(Response response, String model);

  String setButtonCondition(String model);
}
