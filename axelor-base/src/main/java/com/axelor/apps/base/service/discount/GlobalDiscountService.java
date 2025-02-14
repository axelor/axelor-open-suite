package com.axelor.apps.base.service.discount;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.interfaces.GlobalDiscounter;
import java.math.BigDecimal;
import java.util.Map;

public interface GlobalDiscountService {
  void applyGlobalDiscountOnLines(GlobalDiscounter globalDiscounter) throws AxelorException;

  BigDecimal computeDiscountFixedEquivalence(GlobalDiscounter globalDiscounter);

  BigDecimal computeDiscountPercentageEquivalence(GlobalDiscounter globalDiscounter);

  Map<String, Map<String, Object>> setDiscountDummies(GlobalDiscounter globalDiscounter);
}
