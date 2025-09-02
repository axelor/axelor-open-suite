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
package com.axelor.apps.budget.service.move;

import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.repo.MoveLineRepository;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.account.service.AccountCustomerService;
import com.axelor.apps.account.service.accountingsituation.AccountingSituationService;
import com.axelor.apps.account.service.reconcile.UnreconcileService;
import com.axelor.apps.bankpayment.db.repo.BankStatementLineAFB120Repository;
import com.axelor.apps.bankpayment.service.move.MoveRemoveServiceBankPaymentImpl;
import com.axelor.apps.budget.service.AppBudgetService;
import com.axelor.apps.budget.service.BudgetService;
import com.axelor.utils.service.ArchivingService;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.util.List;

public class MoveRemoveBudgetService extends MoveRemoveServiceBankPaymentImpl {

  protected BudgetService budgetService;
  protected AppBudgetService appBudgetService;

  @Inject
  public MoveRemoveBudgetService(
      MoveRepository moveRepo,
      MoveLineRepository moveLineRepo,
      ArchivingService archivingService,
      UnreconcileService unReconcileService,
      AccountingSituationService accountingSituationService,
      AccountCustomerService accountCustomerService,
      BankStatementLineAFB120Repository bankStatementLineAFB120Repository,
      BudgetService budgetService,
      AppBudgetService appBudgetService) {
    super(
        moveRepo,
        moveLineRepo,
        archivingService,
        unReconcileService,
        accountingSituationService,
        accountCustomerService,
        bankStatementLineAFB120Repository);
    this.budgetService = budgetService;
    this.appBudgetService = appBudgetService;
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
    if (appBudgetService.isApp("budget")) {
      budgetService.updateBudgetLinesFromMove(move, true);
    }

    super.deleteMove(move);
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
    move = super.archiveMove(move);
    if (appBudgetService.isApp("budget")) {
      budgetService.updateBudgetLinesFromMove(move, false);
    }
    return move;
  }

  @Override
  public List<String> getModelsToIgnoreList() {
    List<String> modelsToIgnoreList = super.getModelsToIgnoreList();

    modelsToIgnoreList.add("BudgetDistribution");

    return modelsToIgnoreList;
  }
}
