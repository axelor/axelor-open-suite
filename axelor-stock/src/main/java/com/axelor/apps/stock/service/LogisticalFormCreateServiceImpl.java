package com.axelor.apps.stock.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.stock.db.LogisticalForm;
import com.axelor.apps.stock.db.StockConfig;
import com.axelor.apps.stock.db.StockLocation;
import com.axelor.apps.stock.db.repo.LogisticalFormRepository;
import com.axelor.apps.stock.db.repo.StockLocationRepository;
import com.axelor.apps.stock.exception.StockExceptionMessage;
import com.axelor.apps.stock.service.config.StockConfigService;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.time.LocalDate;
import java.util.Optional;

public class LogisticalFormCreateServiceImpl implements LogisticalFormCreateService {

  protected final AppBaseService appBaseService;
  protected final LogisticalFormService logisticalFormService;
  protected final LogisticalFormRepository logisticalFormRepository;
  protected final StockConfigService stockConfigService;

  @Inject
  public LogisticalFormCreateServiceImpl(
      AppBaseService appBaseService,
      LogisticalFormService logisticalFormService,
      LogisticalFormRepository logisticalFormRepository,
      StockConfigService stockConfigService) {
    this.appBaseService = appBaseService;
    this.logisticalFormService = logisticalFormService;
    this.logisticalFormRepository = logisticalFormRepository;
    this.stockConfigService = stockConfigService;
  }

  @Transactional(rollbackOn = {Exception.class})
  @Override
  public LogisticalForm createLogisticalForm(
      Partner carrierPartner,
      Partner deliverToCustomerPartner,
      StockLocation stockLocation,
      LocalDate collectionDate,
      String internalDeliveryComment,
      String externalDeliveryComment)
      throws AxelorException {
    LogisticalForm logisticalForm = new LogisticalForm();

    Company company =
        Optional.ofNullable(AuthUtils.getUser()).map(User::getActiveCompany).orElse(null);
    StockConfig stockConfig = stockConfigService.getStockConfig(company);
    boolean isMultiClientEnabled = stockConfig.getIsLogisticalFormMultiClientsEnabled();
    logisticalForm.setAccountSelectionToCarrierSelect(LogisticalFormRepository.ACCOUNT_COMPANY);
    checkFields(
        carrierPartner, deliverToCustomerPartner, isMultiClientEnabled, company, stockLocation);
    logisticalForm.setCarrierPartner(carrierPartner);
    logisticalForm.setDeliverToCustomerPartner(deliverToCustomerPartner);
    logisticalForm.setCustomerAccountNumberToCarrier(
        logisticalFormService.getCustomerAccountNumberToCarrier(logisticalForm).orElse(null));
    logisticalForm.setStockLocation(stockLocation);
    logisticalForm.setCollectionDate(
        Optional.ofNullable(collectionDate).orElse(appBaseService.getTodayDate(company)));
    logisticalForm.setInternalDeliveryComment(internalDeliveryComment);
    logisticalForm.setExternalDeliveryComment(externalDeliveryComment);
    logisticalForm.setCompany(company);
    return logisticalFormRepository.save(logisticalForm);
  }

  protected void checkFields(
      Partner carrierPartner,
      Partner deliverToCustomerPartner,
      boolean isMultiClientEnabled,
      Company company,
      StockLocation stockLocation)
      throws AxelorException {
    if (carrierPartner != null && !carrierPartner.getIsCarrier()) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(StockExceptionMessage.LOGISTICAL_FORM_PARTNER_NOT_A_CARRIER));
    }
    if (isMultiClientEnabled && deliverToCustomerPartner == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_MISSING_FIELD,
          I18n.get(StockExceptionMessage.LOGISTICAL_FORM_MISSING_DELIVER_TO_PARTNER_CUSTOMER));
    }
    if (deliverToCustomerPartner != null
        && (!deliverToCustomerPartner.getIsCustomer()
            || !deliverToCustomerPartner.getCompanySet().contains(company))) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(StockExceptionMessage.LOGISTICAL_FORM_DELIVER_PARTNER_NOT_A_CUSTOMER));
    }
    checkStockLocation(company, stockLocation);
  }

  protected void checkStockLocation(Company company, StockLocation stockLocation)
      throws AxelorException {
    if (stockLocation != null
        && (!stockLocation.getCompany().equals(company)
            || stockLocation.getTypeSelect() == StockLocationRepository.TYPE_VIRTUAL)) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(StockExceptionMessage.LOGISTICAL_FORM_STOCK_LOCATION_MUST_BE_VIRTUAL));
    }
  }
}
