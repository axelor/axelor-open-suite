/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2024 Axelor (<http://axelor.com>).
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
