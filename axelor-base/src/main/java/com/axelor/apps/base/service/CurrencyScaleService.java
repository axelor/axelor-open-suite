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
