package com.axelor.apps.account.service.invoice;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.interfaces.GlobalDiscounter;
import java.util.Map;

public interface InvoiceGlobalDiscountService {

  void applyGlobalDiscountOnLines(GlobalDiscounter globalDiscounter) throws AxelorException;

  Map<String, Map<String, Object>> setDiscountDummies(GlobalDiscounter globalDiscounter);
}
