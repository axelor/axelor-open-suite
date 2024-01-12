/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2023 Axelor (<http://axelor.com>).
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

import com.axelor.apps.budget.db.BudgetLine;
import com.google.inject.Inject;
import java.math.BigDecimal;

public class BudgetLineResetToolServiceImpl implements BudgetLineResetToolService {

  protected CurrencyScaleServiceBudget currencyScaleServiceBudget;

  @Inject
  public BudgetLineResetToolServiceImpl(CurrencyScaleServiceBudget currencyScaleServiceBudget) {
    this.currencyScaleServiceBudget = currencyScaleServiceBudget;
  }

  @Override
  public BudgetLine resetBudgetLine(BudgetLine entity) {

    entity.setArchived(false);
    entity.setAmountExpected(
        currencyScaleServiceBudget.getCompanyScaledValue(
            entity.getBudget(), entity.getAmountExpected()));
    entity.setAmountCommitted(BigDecimal.ZERO);
    entity.setRealizedWithNoPo(BigDecimal.ZERO);
    entity.setRealizedWithPo(BigDecimal.ZERO);
    entity.setAvailableAmount(entity.getAmountExpected());
    entity.setAmountRealized(BigDecimal.ZERO);
    entity.setFirmGap(BigDecimal.ZERO);
    entity.setAmountPaid(BigDecimal.ZERO);
    entity.setToBeCommittedAmount(BigDecimal.ZERO);

    return entity;
  }
}
