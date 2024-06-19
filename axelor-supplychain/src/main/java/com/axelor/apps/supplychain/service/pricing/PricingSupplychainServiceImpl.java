package com.axelor.apps.supplychain.service.pricing;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.repo.PricingRepository;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.base.service.pricing.PricingGenericService;
import com.axelor.db.Model;
import com.axelor.studio.db.AppBase;
import com.google.inject.Inject;

public class PricingSupplychainServiceImpl implements PricingSupplychainService {

  protected PricingGenericService pricingGenericService;
  protected AppBaseService appBaseService;

  @Inject
  public PricingSupplychainServiceImpl(
      PricingGenericService pricingGenericService, AppBaseService appBaseService) {
    this.pricingGenericService = pricingGenericService;
    this.appBaseService = appBaseService;
  }

  @Override
  public Model computeFiscalPositionPricing(Model model, Company company) throws AxelorException {
    AppBase appBase = appBaseService.getAppBase();
    if (appBase != null
        && appBase.getEnablePricingScale()
        && appBase.getUsePricingForFiscalPosition()) {
      pricingGenericService.computePricingProcess(
          company, model, PricingRepository.PRICING_TYPE_SELECT_FISCAL_POSITION_PRICING);
    }

    return model;
  }
}
