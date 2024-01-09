package com.axelor.apps.budget.service.move;

import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.repo.InvoicePaymentRepository;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.account.service.ReconcileService;
import com.axelor.apps.account.service.extract.ExtractContextMoveService;
import com.axelor.apps.account.service.move.MoveCreateService;
import com.axelor.apps.account.service.move.MoveToolService;
import com.axelor.apps.account.service.move.MoveValidateService;
import com.axelor.apps.account.service.moveline.MoveLineCreateService;
import com.axelor.apps.account.service.payment.invoice.payment.InvoicePaymentCancelService;
import com.axelor.apps.bankpayment.db.repo.BankReconciliationLineRepository;
import com.axelor.apps.bankpayment.service.bankreconciliation.BankReconciliationService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.budget.db.BudgetDistribution;
import com.axelor.apps.budget.service.BudgetDistributionService;
import com.axelor.apps.hr.service.expense.ExpenseMoveReverseServiceImpl;
import com.axelor.apps.hr.service.expense.ExpensePaymentService;
import com.axelor.common.ObjectUtils;
import com.axelor.studio.app.service.AppService;
import com.google.inject.Inject;
import java.time.LocalDate;

public class MoveReverseServiceBudgetImpl extends ExpenseMoveReverseServiceImpl {

  protected BudgetDistributionService budgetDistributionService;

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
      ExpensePaymentService expensePaymentService,
      AppService appService,
      BudgetDistributionService budgetDistributionService) {
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
        expensePaymentService,
        appService);
    this.budgetDistributionService = budgetDistributionService;
  }

  @Override
  protected MoveLine generateReverseMoveLine(
      Move reverseMove, MoveLine originMoveLine, LocalDate dateOfReversion, boolean isDebit)
      throws AxelorException {
    MoveLine reverseMoveLine =
        super.generateReverseMoveLine(reverseMove, originMoveLine, dateOfReversion, isDebit);

    computeReverseBudgetDistribution(reverseMoveLine, originMoveLine);
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
