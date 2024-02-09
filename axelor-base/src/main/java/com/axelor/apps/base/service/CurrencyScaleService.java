package com.axelor.apps.base.service;

import com.axelor.apps.base.interfaces.Currenciable;
import java.math.BigDecimal;

public interface CurrencyScaleService {

  BigDecimal getScaledValue(Currenciable currenciable, BigDecimal value);

  BigDecimal getCompanyScaledValue(Currenciable currenciable, BigDecimal value);

  BigDecimal getScaledValue(BigDecimal value);

  BigDecimal getScaledValue(BigDecimal value, int customizedScale);

  int getScale();
}
