/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2026 Axelor (<http://axelor.com>).
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
package com.axelor.apps.account.service.analytic;

import com.axelor.apps.account.db.AnalyticMoveLine;
import com.axelor.apps.base.service.CurrencyScaleService;
import jakarta.inject.Inject;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class AnalyticMoveLineComputeServiceImpl implements AnalyticMoveLineComputeService {

  protected CurrencyScaleService currencyScaleService;

  @Inject
  public AnalyticMoveLineComputeServiceImpl(CurrencyScaleService currencyScaleService) {
    this.currencyScaleService = currencyScaleService;
  }

  @Override
  public BigDecimal computePercentage(AnalyticMoveLine analyticMoveLine) {
    return this.computePercentage(analyticMoveLine, analyticMoveLine.getOriginalPieceAmount());
  }

  @Override
  public BigDecimal computePercentage(
      AnalyticMoveLine analyticMoveLine, BigDecimal analyticLineAmount) {

    if (analyticLineAmount.signum() > 0) {
      return analyticMoveLine
          .getAmount()
          .multiply(new BigDecimal(100))
          .divide(
              analyticLineAmount,
              currencyScaleService.getScale(analyticMoveLine),
              RoundingMode.HALF_UP);
    }
    return BigDecimal.ZERO;
  }
}
