package com.axelor.apps.base.service;

import java.math.BigDecimal;

public interface CurrencyScaleService {

  BigDecimal getScaledValue(BigDecimal value);

  BigDecimal getScaledValue(BigDecimal value, int customizedScale);

  int getScale();
}
