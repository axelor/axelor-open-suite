package com.axelor.apps.base.service;

import com.axelor.apps.base.service.app.AppBaseService;
import java.math.BigDecimal;
import java.math.RoundingMode;

public class CurrencyScaleServiceImpl implements CurrencyScaleService {

  protected static final int DEFAULT_SCALE = AppBaseService.DEFAULT_NB_DECIMAL_DIGITS;
  protected static final RoundingMode DEFAULT_ROUNDING_MODE = RoundingMode.HALF_UP;

  @Override
  public BigDecimal getScaledValue(BigDecimal value) {
    return value == null ? null : value.setScale(DEFAULT_SCALE, DEFAULT_ROUNDING_MODE);
  }

  @Override
  public BigDecimal getScaledValue(BigDecimal value, int customizedScale) {
    int scale = DEFAULT_SCALE;

    if (customizedScale >= 0) {
      scale = customizedScale;
    }

    return value == null ? null : value.setScale(scale, DEFAULT_ROUNDING_MODE);
  }

  @Override
  public int getScale() {
    return DEFAULT_SCALE;
  }
}
