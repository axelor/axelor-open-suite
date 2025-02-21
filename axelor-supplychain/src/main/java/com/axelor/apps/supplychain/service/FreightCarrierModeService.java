package com.axelor.apps.supplychain.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.stock.db.FreightCarrierMode;
import java.util.List;

public interface FreightCarrierModeService {
  void computeFreightCarrierMode(List<FreightCarrierMode> freightCarrierModeList, Long saleOrderId)
      throws AxelorException;
}
