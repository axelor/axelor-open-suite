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
package com.axelor.apps.budget.service.invoice;

import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.base.service.CurrencyScaleService;
import com.axelor.apps.budget.db.BudgetDistribution;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.util.List;
import org.apache.commons.collections.CollectionUtils;

public class InvoiceToolBudgetServiceImpl implements InvoiceToolBudgetService {

  protected CurrencyScaleService currencyScaleService;

  @Inject
  public InvoiceToolBudgetServiceImpl(CurrencyScaleService currencyScaleService) {
    this.currencyScaleService = currencyScaleService;
  }

  @Override
  public void copyBudgetDistributionList(
      List<BudgetDistribution> originalBudgetDistributionList,
      InvoiceLine invoiceLine,
      BigDecimal prorata) {

    if (CollectionUtils.isEmpty(originalBudgetDistributionList)) {
      return;
    }

    for (BudgetDistribution budgetDistributionIt : originalBudgetDistributionList) {
      BudgetDistribution budgetDistribution = new BudgetDistribution();
      budgetDistribution.setBudget(budgetDistributionIt.getBudget());
      budgetDistribution.setAmount(
          currencyScaleService.getCompanyScaledValue(
              budgetDistributionIt, budgetDistributionIt.getAmount().multiply(prorata)));
      budgetDistribution.setBudgetAmountAvailable(budgetDistributionIt.getBudgetAmountAvailable());
      invoiceLine.addBudgetDistributionListItem(budgetDistribution);
    }
  }
}
