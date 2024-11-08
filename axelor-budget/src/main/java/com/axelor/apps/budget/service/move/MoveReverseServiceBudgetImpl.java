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
package com.axelor.apps.budget.service.move;

import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.repo.InvoicePaymentRepository;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.account.service.ReconcileService;
import com.axelor.apps.account.service.analytic.AnalyticLineService;
import com.axelor.apps.account.service.extract.ExtractContextMoveService;
import com.axelor.apps.account.service.move.MoveCreateService;
import com.axelor.apps.account.service.move.MoveInvoiceTermService;
import com.axelor.apps.account.service.move.MoveToolService;
import com.axelor.apps.account.service.move.MoveValidateService;
import com.axelor.apps.account.service.moveline.MoveLineCreateService;
import com.axelor.apps.account.service.payment.invoice.payment.InvoicePaymentCancelService;
import com.axelor.apps.bankpayment.db.repo.BankReconciliationLineRepository;
import com.axelor.apps.bankpayment.service.bankreconciliation.BankReconciliationService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.budget.db.BudgetDistribution;
import com.axelor.apps.budget.service.AppBudgetService;
import com.axelor.apps.budget.service.BudgetDistributionService;
import com.axelor.apps.hr.service.expense.ExpenseMoveReverseServiceImpl;
import com.axelor.apps.hr.service.expense.ExpensePaymentService;
import com.axelor.common.ObjectUtils;
import com.axelor.studio.app.service.AppService;
import com.google.inject.Inject;
import java.time.LocalDate;

public class MoveReverseServiceBudgetImpl extends ExpenseMoveReverseServiceImpl {

  protected BudgetDistributionService budgetDistributionService;
  protected AppBudgetService appBudgetService;

  @Inject
  public MoveReverseServiceBudgetImpl(
      MoveCreateService moveCreateService,
      ReconcileService reconcileService,
      MoveValidateService moveValidateService,
      MoveRepository moveRepository,
      MoveLineCreateService moveLineCreateService,
      ExtractContextMoveService extractContextMoveService,
      InvoicePaymentRepository invoicePaymentRepository,
      InvoicePaymentCancelService invoicePaymentCancelService,
      MoveToolService moveToolService,
      BankReconciliationService bankReconciliationService,
      BankReconciliationLineRepository bankReconciliationLineRepository,
      MoveInvoiceTermService moveInvoiceTermService,
      AnalyticLineService analyticLineService,
      ExpensePaymentService expensePaymentService,
      AppService appService,
      BudgetDistributionService budgetDistributionService,
      AppBudgetService appBudgetService) {
    super(
        moveCreateService,
        reconcileService,
        moveValidateService,
        moveRepository,
        moveLineCreateService,
        extractContextMoveService,
        invoicePaymentRepository,
        invoicePaymentCancelService,
        moveToolService,
        bankReconciliationService,
        bankReconciliationLineRepository,
        moveInvoiceTermService,
        analyticLineService,
        expensePaymentService,
        appService);
    this.budgetDistributionService = budgetDistributionService;
    this.appBudgetService = appBudgetService;
  }

  @Override
  protected MoveLine generateReverseMoveLine(
      Move reverseMove, MoveLine originMoveLine, LocalDate dateOfReversion, boolean isDebit)
      throws AxelorException {
    MoveLine reverseMoveLine =
        super.generateReverseMoveLine(reverseMove, originMoveLine, dateOfReversion, isDebit);

    if (appBudgetService.isApp("budget")) {
      computeReverseBudgetDistribution(reverseMoveLine, originMoveLine);
    }
    return reverseMoveLine;
  }

  protected void computeReverseBudgetDistribution(
      MoveLine reverseMoveLine, MoveLine originMoveLine) {
    if (reverseMoveLine == null
        || originMoveLine == null
        || originMoveLine.getMove() == null
        || ObjectUtils.isEmpty(originMoveLine.getBudgetDistributionList())) {
      return;
    }

    for (BudgetDistribution budgetDistribution : originMoveLine.getBudgetDistributionList()) {
      BudgetDistribution reverseBudgetDistribution =
          budgetDistributionService.createDistributionFromBudget(
              budgetDistribution.getBudget(),
              budgetDistribution.getAmount().negate(),
              originMoveLine.getMove().getDate());
      budgetDistributionService.linkBudgetDistributionWithParent(
          reverseBudgetDistribution, reverseMoveLine);
    }
  }
}
