package com.axelor.apps.base.service.meta;

import com.axelor.apps.base.service.pricing.PricingMetaService;
import com.axelor.auth.db.repo.UserRepository;
import com.axelor.rpc.Response;
import com.axelor.studio.service.CustomMetaService;
import com.google.inject.Inject;

public class BaseMetaService extends CustomMetaService {

  protected PricingMetaService pricingMetaService;

  @Inject
  public BaseMetaService(UserRepository userRepo, PricingMetaService pricingMetaService) {
    super(userRepo);
    this.pricingMetaService = pricingMetaService;
  }

  @Override
  public Response findView(String model, String name, String type) {
    Response response = super.findView(model, name, type);

    response = pricingMetaService.managePricing(response, model);
    return response;
  }
}
