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
package com.axelor.apps.budget.service.invoice;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.budget.db.BudgetDistribution;
import com.axelor.apps.budget.exception.BudgetExceptionMessage;
import com.axelor.apps.budget.service.BudgetToolsService;
import com.axelor.common.ObjectUtils;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import java.math.BigDecimal;

public class InvoiceLineToolBudgetServiceImpl implements InvoiceLineToolBudgetService {

  protected BudgetToolsService budgetToolsService;

  @Inject
  public InvoiceLineToolBudgetServiceImpl(BudgetToolsService budgetToolsService) {
    this.budgetToolsService = budgetToolsService;
  }

  @Override
  public void checkAmountForInvoiceLine(InvoiceLine invoiceLine) throws AxelorException {
    if (invoiceLine.getBudgetDistributionList() != null
        && !invoiceLine.getBudgetDistributionList().isEmpty()) {
      BigDecimal amountSum = BigDecimal.ZERO;
      for (BudgetDistribution budgetDistribution : invoiceLine.getBudgetDistributionList()) {
        if (budgetDistribution.getAmount().abs().compareTo(invoiceLine.getCompanyExTaxTotal())
            > 0) {
          throw new AxelorException(
              TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
              I18n.get(BudgetExceptionMessage.BUDGET_DISTRIBUTION_LINE_SUM_GREATER_INVOICE),
              budgetDistribution.getBudget().getCode(),
              invoiceLine.getProduct().getCode());
        } else {
          amountSum = amountSum.add(budgetDistribution.getAmount());
        }
      }
      if (amountSum.compareTo(invoiceLine.getCompanyExTaxTotal()) > 0) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
            I18n.get(BudgetExceptionMessage.BUDGET_DISTRIBUTION_LINE_SUM_LINES_GREATER_INVOICE),
            invoiceLine.getProduct().getCode());
      }
    }
  }

  @Override
  public void negateAmount(InvoiceLine invoiceLine, Invoice invoice) {
    if (invoiceLine == null || ObjectUtils.isEmpty(invoiceLine.getBudgetDistributionList())) {
      return;
    }
    for (BudgetDistribution budgetDistribution : invoiceLine.getBudgetDistributionList()) {
      if (budgetDistribution.getAmount().compareTo(BigDecimal.ZERO) > 0) {
        budgetDistribution.setAmount(budgetDistribution.getAmount().negate());
      }
    }
    invoiceLine.setBudgetRemainingAmountToAllocate(
        budgetToolsService.getBudgetRemainingAmountToAllocate(
            invoiceLine.getBudgetDistributionList(), invoiceLine.getCompanyExTaxTotal()));
  }
}
