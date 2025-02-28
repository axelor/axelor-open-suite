package com.axelor.apps.sale.service.saleorder;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.interfaces.GlobalDiscounter;
import java.util.Map;

public interface SaleOrderGlobalDiscountService {

  void applyGlobalDiscountOnLines(GlobalDiscounter globalDiscounter) throws AxelorException;

  Map<String, Map<String, Object>> setDiscountDummies(GlobalDiscounter globalDiscounter);
}
