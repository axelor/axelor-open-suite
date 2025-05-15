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
package com.axelor.apps.budget.service;

import com.axelor.apps.account.db.AccountConfig;
import com.axelor.apps.account.db.AnalyticAxis;
import com.axelor.apps.account.db.AnalyticAxisByCompany;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.service.CurrencyScaleService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.base.service.user.UserRoleToolService;
import com.axelor.apps.budget.db.Budget;
import com.axelor.apps.budget.db.BudgetDistribution;
import com.axelor.apps.budget.db.BudgetLevel;
import com.axelor.apps.budget.db.BudgetLine;
import com.axelor.apps.budget.db.BudgetStructure;
import com.axelor.apps.budget.db.GlobalBudget;
import com.axelor.apps.budget.db.repo.GlobalBudgetRepository;
import com.axelor.apps.budget.exception.BudgetExceptionMessage;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.axelor.common.ObjectUtils;
import com.axelor.db.Model;
import com.axelor.i18n.I18n;
import com.axelor.utils.helpers.date.LocalDateHelper;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.servlet.RequestScoped;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;

@RequestScoped
public class BudgetToolsServiceImpl implements BudgetToolsService {

  protected AccountConfigService accountConfigService;
  protected AppBudgetService appBudgetService;
  protected AppBaseService appBaseService;
  protected CurrencyScaleService currencyScaleService;

  @Inject
  public BudgetToolsServiceImpl(
      AccountConfigService accountConfigService,
      AppBudgetService appBudgetService,
      AppBaseService appBaseService,
      CurrencyScaleService currencyScaleService) {
    this.accountConfigService = accountConfigService;
    this.appBudgetService = appBudgetService;
    this.appBaseService = appBaseService;
    this.currencyScaleService = currencyScaleService;
  }

  @Override
  public boolean checkBudgetKeyAndRole(Company company, User user) throws AxelorException {
    if (company != null && user != null) {
      AccountConfig accountConfig = accountConfigService.getAccountConfig(company);

      return !accountConfig.getEnableBudgetKey()
          || UserRoleToolService.checkUserRolesPermissionIncludingEmpty(
              user, accountConfig.getBudgetDistributionRoleSet());
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
  public BudgetStructure getBudgetStructureUsingBudget(Budget budget) {

    if (budget == null) {
      return null;
    }

    if (budget.getBudgetStructure() != null) {
      return budget.getBudgetStructure();
    }
    if (budget.getBudgetLevel() != null) {
      return getBudgetStructureUsingBudgetLevel(budget.getBudgetLevel());
    }

    return null;
  }

  @Override
  public BudgetStructure getBudgetStructureUsingBudgetLevel(BudgetLevel budgetLevel) {
    if (budgetLevel == null) {
      return null;
    }

    if (budgetLevel.getBudgetStructure() != null) {
      return budgetLevel.getBudgetStructure();
    }

    return getBudgetStructureUsingBudgetLevel(budgetLevel.getParentBudgetLevel());
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
  public boolean canAutoComputeBudgetDistribution(Company company, List<? extends Model> list)
      throws AxelorException {
    return !CollectionUtils.isEmpty(list)
        && company != null
        && checkBudgetKeyAndRole(company, AuthUtils.getUser())
        && checkBudgetKeyInConfig(company);
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

  @Override
  public boolean checkBudgetKeyInConfig(Company company) throws AxelorException {
    return (company != null
        && accountConfigService.getAccountConfig(company) != null
        && accountConfigService.getAccountConfig(company).getEnableBudgetKey());
  }

  public BigDecimal getAvailableAmountOnBudget(Budget budget, LocalDate date) {
    if (budget == null) {
      return BigDecimal.ZERO;
    }
    Integer budgetControlLevel = getBudgetControlLevel(budget);
    if (budgetControlLevel == null) {
      return currencyScaleService.getCompanyScaledValue(budget, budget.getAvailableAmount());
    }
    GlobalBudget globalBudget = getGlobalBudgetUsingBudget(budget);

    switch (budgetControlLevel) {
      case GlobalBudgetRepository.GLOBAL_BUDGET_AVAILABLE_AMOUNT_BUDGET_LINE:
        if (date == null && globalBudget != null && globalBudget.getCompany() != null) {
          date = appBaseService.getTodayDate(globalBudget.getCompany());
        }

        for (BudgetLine budgetLine : budget.getBudgetLineList()) {
          if (LocalDateHelper.isBetween(budgetLine.getFromDate(), budgetLine.getToDate(), date)) {
            return currencyScaleService.getCompanyScaledValue(
                budget, budgetLine.getAvailableAmount());
          }
        }
        break;
      case GlobalBudgetRepository.GLOBAL_BUDGET_AVAILABLE_AMOUNT_BUDGET:
        return currencyScaleService.getCompanyScaledValue(budget, budget.getAvailableAmount());
      default:
        return currencyScaleService.getCompanyScaledValue(
            budget,
            Optional.of(globalBudget)
                .map(GlobalBudget::getTotalAmountAvailable)
                .orElse(BigDecimal.ZERO));
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
            != GlobalBudgetRepository.GLOBAL_BUDGET_AVAILABLE_AMOUNT_DEFAULT_VALUE) {
      return globalBudget.getCheckAvailableSelect();
    } else {
      return appBudgetService.getAppBudget().getCheckAvailableBudget()
          ? GlobalBudgetRepository.GLOBAL_BUDGET_AVAILABLE_AMOUNT_BUDGET_LINE
          : null;
    }
  }

  @Override
  public void fillAmountPerBudgetMap(
      Budget budget, BigDecimal amount, Map<Budget, BigDecimal> amountPerBudgetMap) {
    if (budget == null) {
      return;
    }

    if (!amountPerBudgetMap.containsKey(budget)) {
      amountPerBudgetMap.put(budget, amount);
    } else {
      BigDecimal oldAmount = amountPerBudgetMap.get(budget);
      amountPerBudgetMap.remove(budget);
      amountPerBudgetMap.put(budget, oldAmount.add(amount));
    }
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
            currencyScaleService.getCompanyScaledValue(
                budgetLevelObj,
                amountByField
                    .get("totalAmountExpected")
                    .add(budgetLevelObj.getTotalAmountExpected())));
        amountByField.replace(
            "totalAmountCommitted",
            currencyScaleService.getCompanyScaledValue(
                budgetLevelObj,
                amountByField
                    .get("totalAmountCommitted")
                    .add(budgetLevelObj.getTotalAmountCommitted())));
        amountByField.replace(
            "totalAmountRealized",
            currencyScaleService.getCompanyScaledValue(
                budgetLevelObj,
                amountByField
                    .get("totalAmountRealized")
                    .add(budgetLevelObj.getTotalAmountCommitted())));
        amountByField.replace(
            "realizedWithPo",
            currencyScaleService.getCompanyScaledValue(
                budgetLevelObj,
                amountByField.get("realizedWithPo").add(budgetLevelObj.getRealizedWithPo())));
        amountByField.replace(
            "realizedWithNoPo",
            currencyScaleService.getCompanyScaledValue(
                budgetLevelObj,
                amountByField.get("realizedWithNoPo").add(budgetLevelObj.getRealizedWithNoPo())));
        amountByField.replace(
            "totalAmountPaid",
            currencyScaleService.getCompanyScaledValue(
                budgetLevelObj,
                amountByField.get("totalAmountPaid").add(budgetLevelObj.getTotalAmountPaid())));
        amountByField.replace(
            "totalFirmGap",
            currencyScaleService.getCompanyScaledValue(
                budgetLevelObj,
                amountByField.get("totalFirmGap").add(budgetLevelObj.getTotalFirmGap())));
        amountByField.replace(
            "simulatedAmount",
            currencyScaleService.getCompanyScaledValue(
                budgetLevelObj,
                amountByField.get("simulatedAmount").add(budgetLevelObj.getSimulatedAmount())));
      }
    } else if (!ObjectUtils.isEmpty(budgetList)) {
      for (Budget budget : budgetList) {
        amountByField.replace(
            "totalAmountExpected",
            currencyScaleService.getCompanyScaledValue(
                budget,
                amountByField.get("totalAmountExpected").add(budget.getTotalAmountExpected())));
        amountByField.replace(
            "totalAmountCommitted",
            currencyScaleService.getCompanyScaledValue(
                budget,
                amountByField.get("totalAmountCommitted").add(budget.getTotalAmountCommitted())));
        amountByField.replace(
            "totalAmountRealized",
            currencyScaleService.getCompanyScaledValue(
                budget,
                amountByField.get("totalAmountRealized").add(budget.getTotalAmountCommitted())));
        amountByField.replace(
            "realizedWithPo",
            currencyScaleService.getCompanyScaledValue(
                budget, amountByField.get("realizedWithPo").add(budget.getRealizedWithPo())));
        amountByField.replace(
            "realizedWithNoPo",
            currencyScaleService.getCompanyScaledValue(
                budget, amountByField.get("realizedWithNoPo").add(budget.getRealizedWithNoPo())));
        amountByField.replace(
            "totalAmountPaid",
            currencyScaleService.getCompanyScaledValue(
                budget, amountByField.get("totalAmountPaid").add(budget.getTotalAmountPaid())));
        amountByField.replace(
            "totalFirmGap",
            currencyScaleService.getCompanyScaledValue(
                budget, amountByField.get("totalFirmGap").add(budget.getTotalFirmGap())));
        amountByField.replace(
            "simulatedAmount",
            currencyScaleService.getCompanyScaledValue(
                budget, amountByField.get("simulatedAmount").add(budget.getSimulatedAmount())));
      }
    }
    return amountByField;
  }

  @Override
  public BigDecimal getBudgetRemainingAmountToAllocate(
      List<BudgetDistribution> budgetDistributionList, BigDecimal maxAmount) {
    if (ObjectUtils.isEmpty(budgetDistributionList)) {
      return maxAmount;
    }

    Company company =
        budgetDistributionList.stream()
            .map(BudgetDistribution::getBudget)
            .map(Budget::getGlobalBudget)
            .map(GlobalBudget::getCompany)
            .findFirst()
            .orElse(null);

    BigDecimal imputedAmount =
        budgetDistributionList.stream()
            .map(BudgetDistribution::getAmount)
            .reduce(BigDecimal::add)
            .orElse(BigDecimal.ZERO);
    return BigDecimal.ZERO.max(
        currencyScaleService.getCompanyScaledValue(
            company, maxAmount.subtract(imputedAmount.abs())));
  }
}
