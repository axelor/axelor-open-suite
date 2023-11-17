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

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.AccountType;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.repo.MoveLineRepository;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.budget.db.BudgetDistribution;
import com.axelor.apps.budget.exception.BudgetExceptionMessage;
import com.axelor.apps.budget.service.AppBudgetService;
import com.axelor.apps.budget.service.BudgetDistributionService;
import com.axelor.apps.budget.service.BudgetService;
import com.axelor.common.ObjectUtils;
import com.axelor.i18n.I18n;
import com.axelor.studio.db.AppBudget;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import com.google.inject.servlet.RequestScoped;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RequestScoped
public class MoveLineBudgetServiceImpl implements MoveLineBudgetService {

  protected MoveLineRepository moveLineRepository;
  protected BudgetService budgetService;
  protected BudgetDistributionService budgetDistributionService;
  protected AppBudgetService appBudgetService;

  @Inject
  public MoveLineBudgetServiceImpl(
      MoveLineRepository moveLineRepository,
      BudgetService budgetService,
      BudgetDistributionService budgetDistributionService,
      AppBudgetService appBudgetService) {
    this.moveLineRepository = moveLineRepository;
    this.budgetService = budgetService;
    this.budgetDistributionService = budgetDistributionService;
    this.appBudgetService = appBudgetService;
  }

  @Override
  @Transactional
  public String computeBudgetDistribution(Move move, MoveLine moveLine) throws AxelorException {
    if (move == null || moveLine == null) {
      return "";
    }
    moveLine.clearBudgetDistributionList();
    String alertMessage =
        budgetDistributionService.createBudgetDistribution(
            moveLine.getAnalyticMoveLineList(),
            moveLine.getAccount(),
            move.getCompany(),
            move.getDate(),
            moveLine.getCredit().add(moveLine.getDebit()),
            moveLine.getName(),
            moveLine);
    return alertMessage;
  }

  @Override
  public void checkAmountForMoveLine(MoveLine moveLine) throws AxelorException {
    if (moveLine.getBudgetDistributionList() != null
        && !moveLine.getBudgetDistributionList().isEmpty()) {
      for (BudgetDistribution budgetDistribution : moveLine.getBudgetDistributionList()) {
        if (budgetDistribution.getAmount().compareTo(moveLine.getCredit().add(moveLine.getDebit()))
            > 0) {
          throw new AxelorException(
              TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
              I18n.get(BudgetExceptionMessage.BUDGET_DISTRIBUTION_LINE_SUM_GREATER_MOVE),
              budgetDistribution.getBudget().getCode(),
              moveLine.getAccount().getCode());
        }
      }
    }
  }

  @Override
  public String getBudgetDomain(Move move, MoveLine moveLine) {
    Company company = null;
    LocalDate date = null;
    if (move != null) {
      if (move.getCompany() != null) {
        company = move.getCompany();
      }
      if (move.getDate() != null) {
        date = move.getDate();
      }
    }
    String technicalTypeSelect =
        Optional.of(moveLine)
            .map(MoveLine::getAccount)
            .map(Account::getAccountType)
            .map(AccountType::getTechnicalTypeSelect)
            .orElse(null);

    return budgetDistributionService.getBudgetDomain(company, date, technicalTypeSelect);
  }

  @Override
  public void manageMonoBudget(Move move) {
    if (move.getStatusSelect() != MoveRepository.STATUS_ACCOUNTED
        || Optional.of(appBudgetService.getAppBudget())
            .map(AppBudget::getManageMultiBudget)
            .orElse(true)) {
      return;
    }

    List<MoveLine> moveLineList =
        move.getMoveLineList().stream()
            .filter(
                ml -> ml.getBudget() != null && ObjectUtils.isEmpty(ml.getBudgetDistributionList()))
            .collect(Collectors.toList());
    if (!ObjectUtils.isEmpty(moveLineList)) {
      for (MoveLine moveLine : moveLineList) {
        BudgetDistribution budgetDistribution =
            budgetDistributionService.createDistributionFromBudget(
                moveLine.getBudget(),
                moveLine.getCredit().add(moveLine.getDebit()),
                move.getDate());
        budgetDistributionService.linkBudgetDistributionWithParent(budgetDistribution, moveLine);
        moveLine.setBudgetDistributionSumAmount(moveLine.getCredit().add(moveLine.getDebit()));
      }
    }
  }
}
