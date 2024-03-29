package com.axelor.apps.intervention.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.intervention.db.CustomerRequest;
import com.axelor.apps.intervention.db.Intervention;
import com.axelor.apps.sale.db.SaleOrder;

public interface CustomerRequestService {
  void takeIntoAccount(CustomerRequest request);

  Intervention createAnIntervention(CustomerRequest request) throws AxelorException;

  void inProgress(CustomerRequest request);

  SaleOrder generateSaleOrder(CustomerRequest clientRequest) throws AxelorException;
}
