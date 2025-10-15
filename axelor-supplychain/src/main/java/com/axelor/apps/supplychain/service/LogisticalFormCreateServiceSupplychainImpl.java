package com.axelor.apps.supplychain.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.stock.db.StockLocation;
import com.axelor.apps.stock.db.repo.LogisticalFormRepository;
import com.axelor.apps.stock.service.LogisticalFormCreateServiceImpl;
import com.axelor.apps.stock.service.LogisticalFormService;
import com.axelor.apps.stock.service.config.StockConfigService;
import com.axelor.apps.supplychain.exception.SupplychainExceptionMessage;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;

public class LogisticalFormCreateServiceSupplychainImpl extends LogisticalFormCreateServiceImpl {

  @Inject
  public LogisticalFormCreateServiceSupplychainImpl(
      AppBaseService appBaseService,
      LogisticalFormService logisticalFormService,
      LogisticalFormRepository logisticalFormRepository,
      StockConfigService stockConfigService) {
    super(appBaseService, logisticalFormService, logisticalFormRepository, stockConfigService);
  }

  protected void checkFields(
      Partner carrierPartner,
      Partner deliverToCustomerPartner,
      boolean isMultiClientEnabled,
      Company company,
      StockLocation stockLocation)
      throws AxelorException {
    super.checkFields(
        carrierPartner, deliverToCustomerPartner, isMultiClientEnabled, company, stockLocation);
    if (stockLocation != null && !stockLocation.getUsableOnSaleOrder()) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(
              SupplychainExceptionMessage.LOGISTICAL_FORM_STOCK_LOCATION_MUST_BE_USABLE_ON_SO));
    }
  }
}
