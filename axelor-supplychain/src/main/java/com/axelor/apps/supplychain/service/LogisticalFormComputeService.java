package com.axelor.apps.supplychain.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.stock.db.LogisticalForm;

public interface LogisticalFormComputeService {
  void computeLogisticalForm(LogisticalForm logisticalForm) throws AxelorException;
}
