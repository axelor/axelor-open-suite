package com.axelor.apps.stock.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.stock.db.LogisticalForm;
import com.axelor.apps.stock.db.StockLocation;
import java.time.LocalDate;

public interface LogisticalFormCreateService {

  LogisticalForm createLogisticalForm(
      Partner carrierPartner,
      Partner deliverToCustomerPartner,
      StockLocation stockLocation,
      LocalDate collectionDate,
      String internalDeliveryComment,
      String externalDeliveryComment)
      throws AxelorException;
}
