package com.axelor.apps.stock.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.stock.db.LogisticalForm;
import com.axelor.apps.stock.db.StockLocation;
import com.axelor.apps.stock.db.repo.LogisticalFormRepository;
import com.axelor.apps.stock.exception.StockExceptionMessage;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.time.LocalDate;
import java.util.Optional;

public class LogisticalFormCreateServiceImpl implements LogisticalFormCreateService {

  protected final AppBaseService appBaseService;
  protected final LogisticalFormService logisticalFormService;
  protected final LogisticalFormRepository logisticalFormRepository;

  @Inject
  public LogisticalFormCreateServiceImpl(
      AppBaseService appBaseService,
      LogisticalFormService logisticalFormService,
      LogisticalFormRepository logisticalFormRepository) {
    this.appBaseService = appBaseService;
    this.logisticalFormService = logisticalFormService;
    this.logisticalFormRepository = logisticalFormRepository;
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
    logisticalForm.setAccountSelectionToCarrierSelect(LogisticalFormRepository.ACCOUNT_COMPANY);
    if (carrierPartner != null && !carrierPartner.getIsCarrier()) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          StockExceptionMessage.LOGISTICAL_FORM_PARTNER_NOT_A_CARRIER);
    }
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
}
