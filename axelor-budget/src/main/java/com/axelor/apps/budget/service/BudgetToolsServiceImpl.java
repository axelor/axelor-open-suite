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
import com.axelor.apps.budget.db.Budget;
import com.axelor.apps.budget.db.BudgetLevel;
import com.axelor.apps.budget.db.GlobalBudget;
import com.axelor.apps.budget.exception.BudgetExceptionMessage;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.Role;
import com.axelor.auth.db.User;
import com.axelor.i18n.I18n;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.servlet.RequestScoped;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;

@RequestScoped
public class BudgetToolsServiceImpl implements BudgetToolsService {

  protected AccountConfigService accountConfigService;
  protected BudgetService budgetService;

  @Inject
  public BudgetToolsServiceImpl(
      AccountConfigService accountConfigService, BudgetService budgetService) {
    this.accountConfigService = accountConfigService;
    this.budgetService = budgetService;
  }

  @Override
  public boolean checkBudgetKeyAndRole(Company company, User user) throws AxelorException {
    if (company != null && user != null) {
      AccountConfig accountConfig = accountConfigService.getAccountConfig(company);
      if (!accountConfig.getEnableBudgetKey()
          || CollectionUtils.isEmpty(accountConfig.getBudgetDistributionRoleSet())) {
        return true;
      }
      for (Role role : user.getRoles()) {
        if (accountConfig.getBudgetDistributionRoleSet().contains(role)) {
          return true;
        }
      }
      for (Role role : user.getGroup().getRoles()) {
        if (accountConfig.getBudgetDistributionRoleSet().contains(role)) {
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
  public GlobalBudget getGlobalBudgetUsingBudget(Budget budget) {

    if (budget == null) {
      return null;
    }

    if (budget.getGlobalBudget() != null) {
      return budget.getGlobalBudget();
    }
    if (budget.getBudgetLevel() != null) {
      return getGlobalBudgetUsingBudgetLevel(budget.getBudgetLevel());
    }

    return null;
  }

  @Override
  public GlobalBudget getGlobalBudgetUsingBudgetLevel(BudgetLevel budgetLevel) {
    if (budgetLevel == null) {
      return null;
    }

    if (budgetLevel.getGlobalBudget() != null) {
      return budgetLevel.getGlobalBudget();
    }

    return getGlobalBudgetUsingBudgetLevel(budgetLevel.getParentBudgetLevel());
  }

  @Override
  public String getBudgetExceedMessage(String budgetExceedAlert, boolean isOrder, boolean isError) {
    if (Strings.isNullOrEmpty(budgetExceedAlert)) {
      return "";
    }
    if (isOrder && isError) {
      return budgetExceedAlert;
    }
    return String.format(
        "%s %s",
        I18n.get(budgetExceedAlert), I18n.get(BudgetExceptionMessage.BUDGET_EXCEED_ERROR_ALERT));
  }

  @Override
  public boolean canAutoComputeBudgetDistribution(Company company, List<Object> list)
      throws AxelorException {
    return !CollectionUtils.isEmpty(list)
        && company != null
        && checkBudgetKeyAndRole(company, AuthUtils.getUser())
        && budgetService.checkBudgetKeyInConfig(company);
  }

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
}
