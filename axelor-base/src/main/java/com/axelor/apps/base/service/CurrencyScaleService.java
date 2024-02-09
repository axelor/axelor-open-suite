package com.axelor.apps.base.service;

import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Currency;
import com.axelor.apps.base.interfaces.Currenciable;
import java.math.BigDecimal;

public interface CurrencyScaleService {

  BigDecimal getScaledValue(Currenciable currenciable, BigDecimal value);

  BigDecimal getCompanyScaledValue(Currenciable currenciable, BigDecimal value);

  BigDecimal getCompanyScaledValue(Company company, BigDecimal value);

  BigDecimal getScaledValue(BigDecimal value);

  BigDecimal getScaledValue(BigDecimal value, int customizedScale);

  int getScale();

  int getScale(Currenciable currenciable);

  int getCompanyScale(Currenciable currenciable);

  int getCurrencyScale(Currency currency);

  int getCompanyCurrencyScale(Company company);

  boolean isGreaterThan(
      BigDecimal amount1, BigDecimal amount2, Currenciable currenciable, boolean isCompanyValue);

  boolean equals(
      BigDecimal amount1, BigDecimal amount2, Currenciable currenciable, boolean isCompanyValue);
}
