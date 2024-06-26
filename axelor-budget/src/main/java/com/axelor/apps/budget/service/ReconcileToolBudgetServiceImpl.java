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
package com.axelor.apps.budget.service;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import java.math.BigDecimal;
import java.math.RoundingMode;
import org.apache.commons.collections.CollectionUtils;

public class ReconcileToolBudgetServiceImpl implements ReconcileToolBudgetService {

  private final int CALCULATION_SCALE = 10;

  @Override
  public BigDecimal computeReconcileRatio(Invoice invoice, Move move, BigDecimal amount) {
    BigDecimal ratio = BigDecimal.ZERO;
    BigDecimal totalAmount = BigDecimal.ZERO;
    if (invoice != null) {
      totalAmount = invoice.getCompanyInTaxTotal();
    } else if (!CollectionUtils.isEmpty(move.getMoveLineList())) {
      totalAmount =
          move.getMoveLineList().stream()
              .map(MoveLine::getDebit)
              .reduce(BigDecimal::add)
              .orElse(BigDecimal.ZERO);
    }

    if (totalAmount.signum() > 0) {
      ratio = amount.divide(totalAmount, CALCULATION_SCALE, RoundingMode.HALF_UP);
    }

    return ratio;
  }
}
