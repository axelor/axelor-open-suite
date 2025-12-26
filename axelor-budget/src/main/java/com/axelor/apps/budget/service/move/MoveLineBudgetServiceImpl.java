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

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.AccountType;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.repo.AccountRepository;
import com.axelor.apps.account.db.repo.MoveLineRepository;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.CurrencyScaleService;
import com.axelor.apps.budget.db.BudgetDistribution;
import com.axelor.apps.budget.exception.BudgetExceptionMessage;
import com.axelor.apps.budget.service.AppBudgetService;
import com.axelor.apps.budget.service.BudgetDistributionService;
import com.axelor.apps.budget.service.BudgetService;
import com.axelor.apps.budget.service.BudgetToolsService;
import com.axelor.common.ObjectUtils;
import com.axelor.i18n.I18n;
import com.axelor.studio.db.AppBudget;
import com.google.inject.Inject;
import com.google.inject.servlet.RequestScoped;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RequestScoped
public class MoveLineBudgetServiceImpl implements MoveLineBudgetService {

  protected MoveLineRepository moveLineRepository;
  protected BudgetService budgetService;
  protected BudgetDistributionService budgetDistributionService;
  protected AppBudgetService appBudgetService;
  protected CurrencyScaleService currencyScaleService;
  protected BudgetToolsService budgetToolsService;
  protected MoveLineToolBudgetService moveLineToolBudgetService;

  @Inject
  public MoveLineBudgetServiceImpl(
      MoveLineRepository moveLineRepository,
      BudgetService budgetService,
      BudgetDistributionService budgetDistributionService,
      AppBudgetService appBudgetService,
      CurrencyScaleService currencyScaleService,
      BudgetToolsService budgetToolsService,
      MoveLineToolBudgetService moveLineToolBudgetService) {
    this.moveLineRepository = moveLineRepository;
    this.budgetService = budgetService;
    this.budgetDistributionService = budgetDistributionService;
    this.appBudgetService = appBudgetService;
    this.currencyScaleService = currencyScaleService;
    this.budgetToolsService = budgetToolsService;
    this.moveLineToolBudgetService = moveLineToolBudgetService;
  }

  @Override
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
      BigDecimal totalAmount = BigDecimal.ZERO;
      for (BudgetDistribution budgetDistribution : moveLine.getBudgetDistributionList()) {
        if (currencyScaleService
                .getCompanyScaledValue(budgetDistribution, budgetDistribution.getAmount().abs())
                .compareTo(
                    currencyScaleService.getCompanyScaledValue(
                        budgetDistribution, moveLine.getCredit().add(moveLine.getDebit())))
            > 0) {
          throw new AxelorException(
              TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
              I18n.get(BudgetExceptionMessage.BUDGET_DISTRIBUTION_LINE_SUM_GREATER_MOVE),
              budgetDistribution.getBudget().getCode(),
              moveLine.getAccount().getCode());
        }
        totalAmount = totalAmount.add(budgetDistribution.getAmount());
      }
      if (totalAmount.compareTo(moveLine.getCredit().add(moveLine.getDebit())) > 0) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
            I18n.get(BudgetExceptionMessage.BUDGET_DISTRIBUTION_LINE_SUM_LINES_GREATER_MOVE),
            moveLine.getAccount().getCode());
      }
    }
  }

  @Override
  public String getBudgetDomain(Move move, MoveLine moveLine) throws AxelorException {
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

    return budgetDistributionService.getBudgetDomain(
        company, date, technicalTypeSelect, moveLine.getAccount(), new HashSet<>());
  }

  @Override
  public void negateAmount(MoveLine moveLine, Move move) {
    if (moveLine == null
        || moveLine.getAccount() == null
        || ObjectUtils.isEmpty(moveLine.getBudgetDistributionList())) {
      return;
    }

    Account account = moveLine.getAccount();
    if ((AccountRepository.COMMON_POSITION_CREDIT == account.getCommonPosition()
            && moveLine.getDebit().signum() != 0)
        || (AccountRepository.COMMON_POSITION_DEBIT == account.getCommonPosition()
            && moveLine.getCredit().signum() != 0)) {

      for (BudgetDistribution budgetDistribution : moveLine.getBudgetDistributionList()) {
        if (budgetDistribution.getAmount().compareTo(BigDecimal.ZERO) > 0) {
          budgetDistribution.setAmount(budgetDistribution.getAmount().negate());
        }
      }
    }
  }

  @Override
  public void manageMonoBudget(Move move) {
    if (Optional.of(appBudgetService.getAppBudget())
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
        createBudgetDistributionOnMonoBudget(moveLine);
      }
    }
  }

  @Override
  public void changeBudgetDistribution(MoveLine moveLine) {
    if (moveLine == null
        || Optional.of(appBudgetService.getAppBudget())
            .map(AppBudget::getManageMultiBudget)
            .orElse(true)) {
      return;
    }
    List<BudgetDistribution> oldBudgetDistributionList =
        moveLineToolBudgetService.copyBudgetDistributionList(moveLine);
    moveLine.setOldBudgetDistributionList(oldBudgetDistributionList);

    if (moveLine.getBudget() == null) {
      if (ObjectUtils.notEmpty(moveLine.getBudgetDistributionList())) {
        removeBudgetDistributionList(moveLine);
      }
    } else {
      if (ObjectUtils.notEmpty(moveLine.getBudgetDistributionList())) {
        changeBudgetDistributionOnMonoBudget(moveLine);
      } else {
        createBudgetDistributionOnMonoBudget(moveLine);
      }
    }

    moveLine.setBudgetRemainingAmountToAllocate(
        budgetToolsService.getBudgetRemainingAmountToAllocate(
            moveLine.getBudgetDistributionList(), moveLine.getDebit().max(moveLine.getCredit())));
  }

  protected void removeBudgetDistributionList(MoveLine moveLine) {
    if (ObjectUtils.isEmpty(moveLine.getBudgetDistributionList())) {
      return;
    }

    moveLine.clearBudgetDistributionList();
  }

  protected void createBudgetDistributionOnMonoBudget(MoveLine moveLine) {
    LocalDate date =
        Optional.of(moveLine).map(MoveLine::getMove).map(Move::getDate).orElse(moveLine.getDate());
    BudgetDistribution budgetDistribution =
        budgetDistributionService.createDistributionFromBudget(
            moveLine.getBudget(), moveLine.getCredit().add(moveLine.getDebit()), date);
    budgetDistributionService.linkBudgetDistributionWithParent(budgetDistribution, moveLine);
    moveLine.setBudgetRemainingAmountToAllocate(BigDecimal.ZERO);
  }

  protected void changeBudgetDistributionOnMonoBudget(MoveLine moveLine) {
    if (ObjectUtils.isEmpty(moveLine.getBudgetDistributionList()) || moveLine.getBudget() == null) {
      return;
    }

    List<BudgetDistribution> budgetDistributionList = moveLine.getBudgetDistributionList();
    if (budgetDistributionList.size() == 1) {
      BudgetDistribution budgetDistribution = budgetDistributionList.get(0);
      budgetDistribution.setBudget(moveLine.getBudget());
    } else {
      removeBudgetDistributionList(moveLine);
      createBudgetDistributionOnMonoBudget(moveLine);
    }
  }
}
