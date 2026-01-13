/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2025 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.axelor.apps.base.service;

import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Currency;
import com.axelor.apps.base.interfaces.Currenciable;
import com.axelor.apps.base.service.app.AppBaseService;
import java.math.BigDecimal;
import java.math.RoundingMode;

public class CurrencyScaleServiceImpl implements CurrencyScaleService {

  protected static final int DEFAULT_SCALE = AppBaseService.DEFAULT_NB_DECIMAL_DIGITS;
  protected static final RoundingMode DEFAULT_ROUNDING_MODE = RoundingMode.HALF_UP;

  // Scaled value getter

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
  public BigDecimal getCompanyScaledValue(Company company, BigDecimal value) {
    Currency currency = company != null ? company.getCurrency() : null;

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

  // Currency scale getter
  @Override
  public int getScale() {
    return DEFAULT_SCALE;
  }

  @Override
  public int getScale(Currenciable currenciable) {
    Currency currency = currenciable != null ? currenciable.getCurrency() : null;
    return this.getCurrencyScale(currency);
  }

  @Override
  public int getCompanyScale(Currenciable currenciable) {
    Currency currency = currenciable != null ? currenciable.getCompanyCurrency() : null;
    return this.getCurrencyScale(currency);
  }

  @Override
  public int getCurrencyScale(Currency currency) {
    return currency != null ? currency.getNumberOfDecimals() : this.getScale();
  }

  @Override
  public int getCompanyCurrencyScale(Company company) {
    return company != null ? this.getCurrencyScale(company.getCurrency()) : this.getScale();
  }

  // Comparison methods with scale depending on Company currency o Currency
  @Override
  public boolean isGreaterThan(
      BigDecimal amount1, BigDecimal amount2, Currenciable currenciable, boolean isCompanyValue) {
    amount1 =
        isCompanyValue
            ? this.getCompanyScaledValue(currenciable, amount1)
            : this.getScaledValue(currenciable, amount1);
    amount2 =
        isCompanyValue
            ? this.getCompanyScaledValue(currenciable, amount2)
            : this.getScaledValue(currenciable, amount2);

    return amount1 != null && (amount1.compareTo(amount2) > 0);
  }

  @Override
  public boolean equals(
      BigDecimal amount1, BigDecimal amount2, Currenciable currenciable, boolean isCompanyValue) {
    amount1 =
        isCompanyValue
            ? this.getCompanyScaledValue(currenciable, amount1)
            : this.getScaledValue(currenciable, amount1);
    amount2 =
        isCompanyValue
            ? this.getCompanyScaledValue(currenciable, amount2)
            : this.getScaledValue(currenciable, amount2);

    return amount1.compareTo(amount2) == 0;
  }
}
