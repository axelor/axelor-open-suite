package com.axelor.apps.base.service;

import com.axelor.apps.base.db.Currency;
import com.axelor.apps.base.interfaces.Currenciable;
import com.axelor.apps.base.service.app.AppBaseService;
import java.math.BigDecimal;
import java.math.RoundingMode;

public class CurrencyScaleServiceImpl implements CurrencyScaleService {

  protected static final int DEFAULT_SCALE = AppBaseService.DEFAULT_NB_DECIMAL_DIGITS;
  protected static final RoundingMode DEFAULT_ROUNDING_MODE = RoundingMode.HALF_UP;

  @Override
  public BigDecimal getScaledValue(Currenciable currenciable, BigDecimal value) {
    Currency currency = currenciable != null ? currenciable.getCurrency() : null;

    return this.getScaledValue(value, this.getCurrencyScale(currency));
  }

  @Override
  public BigDecimal getCompanyScaledValue(Currenciable currenciable, BigDecimal value) {
    Currency currency = currenciable != null ? currenciable.getCompanyCurrency() : null;

    return this.getScaledValue(value, this.getCurrencyScale(currency));
  }

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

  protected int getCurrencyScale(Currency currency) {
    return currency != null ? currency.getNumberOfDecimals() : this.getScale();
  }
}
