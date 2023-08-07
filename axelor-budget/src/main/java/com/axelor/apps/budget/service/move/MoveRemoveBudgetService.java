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
package com.axelor.apps.budget.service.move;

import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.repo.MoveLineRepository;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.account.exception.AccountExceptionMessage;
import com.axelor.apps.account.service.AccountCustomerService;
import com.axelor.apps.account.service.AccountingSituationService;
import com.axelor.apps.account.service.ReconcileService;
import com.axelor.apps.bankpayment.db.repo.BankStatementLineAFB120Repository;
import com.axelor.apps.bankpayment.exception.BankPaymentExceptionMessage;
import com.axelor.apps.bankpayment.service.app.AppBankPaymentService;
import com.axelor.apps.bankpayment.service.move.MoveRemoveServiceBankPaymentImpl;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.budget.service.BudgetService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.utils.service.ArchivingToolService;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public class MoveRemoveBudgetService extends MoveRemoveServiceBankPaymentImpl {

  protected BudgetService budgetService;

  @Inject
  public MoveRemoveBudgetService(
      MoveRepository moveRepo,
      MoveLineRepository moveLineRepo,
      ArchivingToolService archivingToolService,
      ReconcileService reconcileService,
      AccountingSituationService accountingSituationService,
      AccountCustomerService accountCustomerService,
      BankStatementLineAFB120Repository bankStatementLineAFB120Repository,
      BudgetService budgetService) {
    super(
        moveRepo,
        moveLineRepo,
        archivingToolService,
        reconcileService,
        accountingSituationService,
        accountCustomerService,
        bankStatementLineAFB120Repository);
    this.budgetService = budgetService;
  }

  /**
   * Update budget amounts to reset these imputations after archiving and remove the move
   *
   * @param move
   * @throws Exception
   */
  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void deleteMove(Move move) {
    budgetService.updateBudgetLinesFromMove(move, true);
    moveRepo.remove(move);
  }

  /**
   * Archive the move and movelines then update the budget amounts to reset these imputations
   *
   * @param move
   * @return Move
   */
  @Override
  @Transactional(rollbackOn = {Exception.class})
  public Move archiveMove(Move move) {
    move.setArchived(true);
    for (MoveLine moveLine : move.getMoveLineList()) {
      moveLine.setArchived(true);
    }
    budgetService.updateBudgetLinesFromMove(move, false);
    return move;
  }

  /**
   * Throw an error for each link with this moveline, excluding move, reconcile, invoiceterm,
   * analytic moveline, tax payment moveline and budget distribution
   *
   * @param moveLine
   * @return String
   * @throws AxelorException
   */
  @Override
  public String checkMoveLineBeforeRemove(MoveLine moveLine) throws AxelorException {
    if (Beans.get(AppBankPaymentService.class).isApp("bank-payment")) {
      super.removeMoveLineFromBankStatements(moveLine);
    }
    String errorMessage = "";
    Map<String, String> objectsLinkToMoveLineMap =
        archivingToolService.getObjectLinkTo(moveLine, moveLine.getId());
    for (Map.Entry<String, String> entry : objectsLinkToMoveLineMap.entrySet()) {
      String modelName = entry.getKey();
      List<String> modelsToIgnore =
          Lists.newArrayList(
              "Move",
              "Reconcile",
              "InvoiceTerm",
              "AnalyticMoveLine",
              "TaxPaymentMoveLine",
              "BudgetDistribution");
      if (!modelsToIgnore.contains(modelName)
          && moveLine.getMove().getStatusSelect() == MoveRepository.STATUS_DAYBOOK) {
        errorMessage +=
            String.format(
                I18n.get(AccountExceptionMessage.MOVE_LINE_ARCHIVE_NOT_OK_BECAUSE_OF_LINK_WITH),
                moveLine.getName(),
                modelName);
      } else if (!modelsToIgnore.contains(modelName)
          && (moveLine.getMove().getStatusSelect() == MoveRepository.STATUS_NEW
              || moveLine.getMove().getStatusSelect() == MoveRepository.STATUS_SIMULATED)) {
        errorMessage +=
            String.format(
                I18n.get(AccountExceptionMessage.MOVE_LINE_REMOVE_NOT_OK_BECAUSE_OF_LINK_WITH),
                moveLine.getName(),
                modelName);
      }
    }
    if (Beans.get(AppBankPaymentService.class).isApp("bank-payment")
        && moveLine.getBankReconciledAmount().compareTo(BigDecimal.ZERO) > 0) {
      errorMessage +=
          String.format(
              I18n.get(
                  BankPaymentExceptionMessage
                      .MOVE_LINE_ARCHIVE_NOT_OK_BECAUSE_OF_BANK_RECONCILIATION_AMOUNT),
              moveLine.getName());
    }
    return errorMessage;
  }
}
