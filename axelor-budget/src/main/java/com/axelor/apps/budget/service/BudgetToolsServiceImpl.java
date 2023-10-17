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
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.budget.db.Budget;
import com.axelor.apps.budget.db.BudgetLevel;
import com.axelor.apps.budget.db.GlobalBudget;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.Role;
import com.axelor.auth.db.User;
import com.axelor.common.ObjectUtils;
import com.google.inject.Inject;
import com.google.inject.servlet.RequestScoped;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.collections.CollectionUtils;

@RequestScoped
public class BudgetToolsServiceImpl implements BudgetToolsService {

  protected AccountConfigService accountConfigService;

  @Inject
  public BudgetToolsServiceImpl(AccountConfigService accountConfigService) {
    this.accountConfigService = accountConfigService;
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
  public Map<String, BigDecimal> buildMapWithAmounts(
      List<Budget> budgetList, List<BudgetLevel> budgetLevelList) {
    Map<String, BigDecimal> amountByField = new HashMap<>();
    amountByField.put("totalAmountExpected", BigDecimal.ZERO);
    amountByField.put("totalAmountCommitted", BigDecimal.ZERO);
    amountByField.put("totalAmountRealized", BigDecimal.ZERO);
    amountByField.put("realizedWithPo", BigDecimal.ZERO);
    amountByField.put("realizedWithNoPo", BigDecimal.ZERO);
    amountByField.put("totalAmountPaid", BigDecimal.ZERO);
    amountByField.put("totalFirmGap", BigDecimal.ZERO);
    amountByField.put("simulatedAmount", BigDecimal.ZERO);
    if (!ObjectUtils.isEmpty(budgetLevelList)) {
      for (BudgetLevel budgetLevelObj : budgetLevelList) {
        amountByField.replace(
            "totalAmountExpected",
            amountByField.get("totalAmountExpected").add(budgetLevelObj.getTotalAmountExpected()));
        amountByField.replace(
            "totalAmountCommitted",
            amountByField
                .get("totalAmountCommitted")
                .add(budgetLevelObj.getTotalAmountCommitted()));
        amountByField.replace(
            "totalAmountRealized",
            amountByField.get("totalAmountRealized").add(budgetLevelObj.getTotalAmountCommitted()));
        amountByField.replace(
            "realizedWithPo",
            amountByField.get("realizedWithPo").add(budgetLevelObj.getRealizedWithPo()));
        amountByField.replace(
            "realizedWithNoPo",
            amountByField.get("realizedWithNoPo").add(budgetLevelObj.getRealizedWithNoPo()));
        amountByField.replace(
            "totalAmountPaid",
            amountByField.get("totalAmountPaid").add(budgetLevelObj.getTotalAmountPaid()));
        amountByField.replace(
            "totalFirmGap",
            amountByField.get("totalFirmGap").add(budgetLevelObj.getTotalFirmGap()));
        amountByField.replace(
            "simulatedAmount",
            amountByField.get("simulatedAmount").add(budgetLevelObj.getSimulatedAmount()));
      }
    } else if (!ObjectUtils.isEmpty(budgetList)) {
      for (Budget budget : budgetList) {
        amountByField.replace(
            "totalAmountExpected",
            amountByField.get("totalAmountExpected").add(budget.getTotalAmountExpected()));
        amountByField.replace(
            "totalAmountCommitted",
            amountByField.get("totalAmountCommitted").add(budget.getTotalAmountCommitted()));
        amountByField.replace(
            "totalAmountRealized",
            amountByField.get("totalAmountRealized").add(budget.getTotalAmountCommitted()));
        amountByField.replace(
            "realizedWithPo", amountByField.get("realizedWithPo").add(budget.getRealizedWithPo()));
        amountByField.replace(
            "realizedWithNoPo",
            amountByField.get("realizedWithNoPo").add(budget.getRealizedWithNoPo()));
        amountByField.replace(
            "totalAmountPaid",
            amountByField.get("totalAmountPaid").add(budget.getTotalAmountPaid()));
        amountByField.replace(
            "totalFirmGap", amountByField.get("totalFirmGap").add(budget.getTotalFirmGap()));
        amountByField.replace(
            "simulatedAmount",
            amountByField.get("simulatedAmount").add(budget.getSimulatedAmount()));
      }
    }
    return amountByField;
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
}
