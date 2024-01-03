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

import com.axelor.apps.account.db.AccountConfig;
import com.axelor.apps.account.db.AnalyticAxis;
import com.axelor.apps.account.db.AnalyticAxisByCompany;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.budget.db.Budget;
import com.axelor.apps.budget.db.BudgetLevel;
import com.axelor.apps.budget.db.BudgetLine;
import com.axelor.apps.budget.db.GlobalBudget;
import com.axelor.apps.budget.db.repo.BudgetLevelRepository;
import com.axelor.apps.budget.db.repo.GlobalBudgetRepository;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.Role;
import com.axelor.auth.db.User;
import com.axelor.utils.date.LocalDateUtils;
import com.google.inject.Inject;
import com.google.inject.servlet.RequestScoped;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;

@RequestScoped
public class BudgetToolsServiceImpl implements BudgetToolsService {

  protected AccountConfigService accountConfigService;
  protected AppBudgetService appBudgetService;
  protected AppBaseService appBaseService;

  @Inject
  public BudgetToolsServiceImpl(
      AccountConfigService accountConfigService,
      AppBudgetService appBudgetService,
      AppBaseService appBaseService) {
    this.accountConfigService = accountConfigService;
    this.appBudgetService = appBudgetService;
    this.appBaseService = appBaseService;
  }

  @Override
  public boolean checkBudgetKeyAndRole(Company company, User user) throws AxelorException {
    if (company != null && user != null) {
      AccountConfig accountConfig = accountConfigService.getAccountConfig(company);
      if (!accountConfig.getEnableBudgetKey()
          || CollectionUtils.isEmpty(accountConfig.getBudgetDistributionRoleList())) {
        return true;
      }
      for (Role role : user.getRoles()) {
        if (accountConfig.getBudgetDistributionRoleList().contains(role)) {
          return true;
        }
      }
      for (Role role : user.getGroup().getRoles()) {
        if (accountConfig.getBudgetDistributionRoleList().contains(role)) {
          return true;
        }
      }
    }
    return false;
  }

  @Override
  public boolean checkBudgetKeyAndRoleForMove(Move move) throws AxelorException {
    if (move != null) {
      return !(checkBudgetKeyAndRole(move.getCompany(), AuthUtils.getUser()))
          || move.getStatusSelect() == MoveRepository.STATUS_ACCOUNTED
          || move.getStatusSelect() == MoveRepository.STATUS_CANCELED;
    }
    return false;
  }

  @Override
  public List<AnalyticAxis> getAuthorizedAnalyticAxis(Company company) throws AxelorException {
    if (company == null) {
      return new ArrayList<>();
    }
    AccountConfig accountConfig = accountConfigService.getAccountConfig(company);
    if (accountConfig == null
        || CollectionUtils.isEmpty(accountConfig.getAnalyticAxisByCompanyList())) {
      return new ArrayList<>();
    }

    return accountConfig.getAnalyticAxisByCompanyList().stream()
        .filter(AnalyticAxisByCompany::getIncludeInBudgetKey)
        .map(AnalyticAxisByCompany::getAnalyticAxis)
        .collect(Collectors.toList());
  }

  @Override
  public BigDecimal getAvailableAmountOnBudget(Budget budget, LocalDate date) {
    if (budget == null) {
      return BigDecimal.ZERO;
    }
    Integer budgetControlLevel = getBudgetControlLevel(budget);
    if (budgetControlLevel == null) {
      return budget.getAvailableAmount();
    }
    GlobalBudget globalBudget = getGlobalBudgetUsingBudget(budget);

    switch (budgetControlLevel) {
      case BudgetLevelRepository.BUDGET_LEVEL_AVAILABLE_AMOUNT_BUDGET_LINE:
        if (date == null && globalBudget != null && globalBudget.getCompany() != null) {
          date = appBaseService.getTodayDate(globalBudget.getCompany());
        }

        for (BudgetLine budgetLine : budget.getBudgetLineList()) {
          if (LocalDateUtils.isBetween(budgetLine.getFromDate(), budgetLine.getToDate(), date)) {
            return budgetLine.getAvailableAmount();
          }
        }
        break;
      case BudgetLevelRepository.BUDGET_LEVEL_AVAILABLE_AMOUNT_BUDGET:
        return budget.getAvailableAmount();
      case BudgetLevelRepository.BUDGET_LEVEL_AVAILABLE_AMOUNT_BUDGET_SECTION:
        return Optional.of(budget)
            .map(Budget::getBudgetLevel)
            .map(BudgetLevel::getTotalAmountAvailable)
            .orElse(BigDecimal.ZERO);
      case BudgetLevelRepository.BUDGET_LEVEL_AVAILABLE_AMOUNT_BUDGET_GROUP:
        return Optional.of(budget)
            .map(Budget::getBudgetLevel)
            .map(BudgetLevel::getParentBudgetLevel)
            .map(BudgetLevel::getTotalAmountAvailable)
            .orElse(BigDecimal.ZERO);
      default:
        return Optional.of(globalBudget)
            .map(GlobalBudget::getTotalAmountAvailable)
            .orElse(BigDecimal.ZERO);
    }
    return BigDecimal.ZERO;
  }

  @Override
  public Integer getBudgetControlLevel(Budget budget) {

    if (appBudgetService.getAppBudget() == null
        || !appBudgetService.getAppBudget().getCheckAvailableBudget()) {
      return null;
    }
    GlobalBudget globalBudget = getGlobalBudgetUsingBudget(budget);
    if (globalBudget != null
        && globalBudget.getCheckAvailableSelect() != null
        && globalBudget.getCheckAvailableSelect()
            != GlobalBudgetRepository.BUDGET_LEVEL_AVAILABLE_AMOUNT_DEFAULT_VALUE) {
      return globalBudget.getCheckAvailableSelect();
    } else {
      return appBudgetService.getAppBudget().getCheckAvailableBudget()
          ? BudgetLevelRepository.BUDGET_LEVEL_AVAILABLE_AMOUNT_BUDGET_LINE
          : null;
    }
  }

  protected GlobalBudget getGlobalBudgetUsingBudget(Budget budget) {
    return Optional.ofNullable(budget.getBudgetLevel())
        .map(BudgetLevel::getParentBudgetLevel)
        .map(BudgetLevel::getGlobalBudget)
        .orElse(null);
  }
}
