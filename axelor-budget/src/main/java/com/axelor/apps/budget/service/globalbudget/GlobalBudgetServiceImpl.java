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
package com.axelor.apps.budget.service.globalbudget;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Year;
import com.axelor.apps.budget.db.Budget;
import com.axelor.apps.budget.db.BudgetGenerator;
import com.axelor.apps.budget.db.BudgetLevel;
import com.axelor.apps.budget.db.BudgetLine;
import com.axelor.apps.budget.db.BudgetScenarioLine;
import com.axelor.apps.budget.db.BudgetScenarioVariable;
import com.axelor.apps.budget.db.BudgetStructure;
import com.axelor.apps.budget.db.BudgetVersion;
import com.axelor.apps.budget.db.GlobalBudget;
import com.axelor.apps.budget.db.VersionExpectedAmountsLine;
import com.axelor.apps.budget.db.repo.BudgetLevelManagementRepository;
import com.axelor.apps.budget.db.repo.BudgetRepository;
import com.axelor.apps.budget.db.repo.BudgetVersionRepository;
import com.axelor.apps.budget.db.repo.GlobalBudgetRepository;
import com.axelor.apps.budget.service.BudgetLevelService;
import com.axelor.apps.budget.service.BudgetScenarioLineService;
import com.axelor.apps.budget.service.BudgetScenarioService;
import com.axelor.apps.budget.service.BudgetService;
import com.axelor.apps.budget.service.BudgetToolsService;
import com.axelor.auth.AuthUtils;
import com.axelor.common.ObjectUtils;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class GlobalBudgetServiceImpl implements GlobalBudgetService {

  protected BudgetLevelService budgetLevelService;
  protected GlobalBudgetRepository globalBudgetRepository;
  protected BudgetService budgetService;
  protected BudgetRepository budgetRepository;
  protected BudgetLevelManagementRepository budgetLevelManagementRepository;
  protected BudgetVersionRepository budgetVersionRepo;
  protected BudgetScenarioService budgetScenarioService;
  protected BudgetToolsService budgetToolsService;
  protected GlobalBudgetToolsService globalBudgetToolsService;
  protected BudgetScenarioLineService budgetScenarioLineService;

  @Inject
  public GlobalBudgetServiceImpl(
      BudgetLevelService budgetLevelService,
      GlobalBudgetRepository globalBudgetRepository,
      BudgetService budgetService,
      BudgetRepository budgetRepository,
      BudgetLevelManagementRepository budgetLevelManagementRepository,
      BudgetVersionRepository budgetVersionRepo,
      BudgetScenarioService budgetScenarioService,
      BudgetToolsService budgetToolsService,
      GlobalBudgetToolsService globalBudgetToolsService,
      BudgetScenarioLineService budgetScenarioLineService) {
    this.budgetLevelService = budgetLevelService;
    this.globalBudgetRepository = globalBudgetRepository;
    this.budgetService = budgetService;
    this.budgetRepository = budgetRepository;
    this.budgetLevelManagementRepository = budgetLevelManagementRepository;
    this.budgetVersionRepo = budgetVersionRepo;
    this.budgetScenarioService = budgetScenarioService;
    this.budgetToolsService = budgetToolsService;
    this.globalBudgetToolsService = globalBudgetToolsService;
    this.budgetScenarioLineService = budgetScenarioLineService;
  }

  @Override
  public void validateDates(GlobalBudget globalBudget) throws AxelorException {
    if (!ObjectUtils.isEmpty(globalBudget.getBudgetLevelList())) {
      for (BudgetLevel budgetLevelChild : globalBudget.getBudgetLevelList()) {
        budgetLevelService.validateDates(budgetLevelChild);
      }
    } else if (!ObjectUtils.isEmpty(globalBudget.getBudgetList())) {
      for (Budget budget : globalBudget.getBudgetList()) {
        budgetService.checkDatesOnBudget(budget);
      }
    }
  }

  @Override
  public void computeBudgetLevelTotals(Budget budget) {

    budgetService.computeAvailableFields(budget);

    budgetLevelService.computeBudgetLevelTotals(budget);

    GlobalBudget globalBudget = budget.getGlobalBudget();

    if (globalBudget == null) {
      globalBudget = budgetToolsService.getGlobalBudgetUsingBudget(budget);
    }

    if (globalBudget != null) {
      computeTotals(globalBudget);
    }
  }

  @Override
  public void computeTotals(GlobalBudget globalBudget) {
    List<BudgetLevel> budgetLevelList = globalBudget.getBudgetLevelList();
    List<Budget> budgetList = globalBudget.getBudgetList();
    Map<String, BigDecimal> amountByField =
        budgetToolsService.buildMapWithAmounts(budgetList, budgetLevelList);
    globalBudget.setTotalAmountExpected(amountByField.get("totalAmountExpected"));
    globalBudget.setTotalAmountCommitted(amountByField.get("totalAmountCommitted"));
    globalBudget.setTotalAmountPaid(amountByField.get("totalAmountPaid"));
    globalBudget.setTotalAmountRealized(amountByField.get("totalAmountRealized"));
    globalBudget.setRealizedWithNoPo(amountByField.get("realizedWithNoPo"));
    globalBudget.setRealizedWithPo(amountByField.get("realizedWithPo"));
    globalBudget.setTotalAmountAvailable(
        (amountByField
                .get("totalAmountExpected")
                .subtract(amountByField.get("realizedWithPo"))
                .subtract(amountByField.get("realizedWithNoPo")))
            .max(BigDecimal.ZERO));
    globalBudget.setTotalFirmGap(amountByField.get("totalFirmGap"));
    globalBudget.setSimulatedAmount(amountByField.get("simulatedAmount"));
    globalBudget.setAvailableAmountWithSimulated(
        (globalBudget.getTotalAmountAvailable().subtract(amountByField.get("simulatedAmount")))
            .max(BigDecimal.ZERO));
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public GlobalBudget changeBudgetVersion(
      GlobalBudget globalBudget, BudgetVersion budgetVersion, boolean needRecomputeBudgetLine)
      throws AxelorException {
    List<Budget> budgets = globalBudget.getBudgetList();
    List<VersionExpectedAmountsLine> versionExpectedAmountsLineList =
        budgetVersion.getVersionExpectedAmountsLineList();
    if (globalBudget.getActiveVersion() != null) {
      BudgetVersion oldBudgetVersion = globalBudget.getActiveVersion();
      oldBudgetVersion.setIsActive(false);
      budgetVersionRepo.save(oldBudgetVersion);
    }

    for (Budget budget : budgets) {
      VersionExpectedAmountsLine versionExpectedAmountsLine =
          versionExpectedAmountsLineList.stream()
              .filter(version -> version.getBudget().equals(budget))
              .findFirst()
              .orElse(null);
      if (versionExpectedAmountsLine != null) {
        budget.setActiveVersionExpectedAmountsLine(versionExpectedAmountsLine);
        budget.setAmountForGeneration(versionExpectedAmountsLine.getExpectedAmount());
        budget.setTotalAmountExpected(versionExpectedAmountsLine.getExpectedAmount());
        if (needRecomputeBudgetLine) {
          budget.setPeriodDurationSelect(BudgetRepository.BUDGET_PERIOD_SELECT_ONE_TIME);
          budget.clearBudgetLineList();
          List<BudgetLine> budgetLineList = budgetService.generatePeriods(budget);
          if (!ObjectUtils.isEmpty(budgetLineList) && budgetLineList.size() == 1) {
            BudgetLine budgetLine = budgetLineList.get(0);
            recomputeImputedAmountsOnBudgetLine(budgetLine, budget);
          }
        }

        budgetRepository.save(budget);
      }
    }

    globalBudget.setBudgetList(budgets);
    globalBudget.setActiveVersion(budgetVersion);
    budgetVersion.setIsActive(true);
    globalBudget = globalBudgetRepository.save(globalBudget);

    return globalBudget;
  }

  protected void recomputeImputedAmountsOnBudgetLine(BudgetLine budgetLine, Budget budget) {
    budgetLine.setAmountCommitted(budget.getTotalAmountCommitted());
    budgetLine.setAmountRealized(budget.getTotalAmountRealized());
    budgetLine.setRealizedWithNoPo(budget.getRealizedWithNoPo());
    budgetLine.setRealizedWithPo(budget.getRealizedWithPo());
    budgetLine.setFirmGap(budget.getTotalFirmGap());
    budgetLine.setAmountPaid(budget.getTotalAmountPaid());
  }

  @Override
  public void updateGlobalBudgetDates(GlobalBudget globalBudget) throws AxelorException {
    if (globalBudget == null
        || globalBudget.getFromDate() == null
        || globalBudget.getToDate() == null) {
      return;
    }

    if (!ObjectUtils.isEmpty(globalBudget.getBudgetLevelList())) {
      budgetLevelService.getUpdatedBudgetLevelList(
          globalBudget.getBudgetLevelList(), globalBudget.getFromDate(), globalBudget.getToDate());
    } else if (!ObjectUtils.isEmpty(globalBudget.getBudgetList())) {
      budgetLevelService.getUpdatedBudgetList(
          globalBudget.getBudgetList(), globalBudget.getFromDate(), globalBudget.getToDate());
    }
  }

  @Override
  public void generateBudgetKey(GlobalBudget globalBudget) throws AxelorException {
    List<Budget> budgetList = globalBudgetToolsService.getAllBudgets(globalBudget);
    if (ObjectUtils.isEmpty(budgetList)) {
      return;
    }

    Company company = globalBudget.getCompany();
    for (Budget budget : budgetList) {
      budgetService.createBudgetKey(budget, company);
      if (budget.getGlobalBudget() == null) {
        globalBudget.addBudgetListItem(budget);
      }
    }
  }

  @Override
  @Transactional
  public GlobalBudget generateGlobalBudget(BudgetGenerator budgetGenerator, Year year)
      throws AxelorException {

    GlobalBudget globalBudget = copyGlobalBudgetTemplate(budgetGenerator, year);

    fillGlobalBudgetWithLevels(budgetGenerator, globalBudget, year);

    globalBudgetRepository.save(globalBudget);
    return globalBudget;
  }

  protected GlobalBudget copyGlobalBudgetTemplate(BudgetGenerator budgetGenerator, Year year) {
    GlobalBudget globalBudget =
        new GlobalBudget(budgetGenerator.getCode(), budgetGenerator.getName());
    globalBudget.setFromDate(year.getFromDate());
    globalBudget.setToDate(year.getToDate());
    globalBudget.setCompany(budgetGenerator.getBudgetStructure().getCompany());
    globalBudget.setBudgetTypeSelect(budgetGenerator.getBudgetStructure().getBudgetTypeSelect());

    globalBudget.setStatusSelect(GlobalBudgetRepository.GLOBAL_BUDGET_STATUS_SELECT_DRAFT);

    if (AuthUtils.getUser() != null) {
      if (globalBudget.getCompanyDepartment() == null) {
        globalBudget.setCompanyDepartment(AuthUtils.getUser().getCompanyDepartment());
      }
      globalBudget.setBudgetManager(AuthUtils.getUser());
    }
    return globalBudget;
  }

  protected void fillGlobalBudgetWithLevels(
      BudgetGenerator budgetGenerator, GlobalBudget globalBudget, Year year)
      throws AxelorException {
    if (budgetGenerator.getBudgetStructure() == null) {
      return;
    }

    BudgetStructure budgetStructure = budgetGenerator.getBudgetStructure();

    int yearNumber = 0;
    if (!ObjectUtils.isEmpty(budgetGenerator.getYearSet())
        && budgetGenerator.getYearSet().contains(year)
        && budgetGenerator.getBudgetScenario() != null
        && !ObjectUtils.isEmpty(budgetGenerator.getBudgetScenario().getYearSet())) {
      yearNumber =
          budgetGenerator.getYearSet().stream()
                  .filter(y -> budgetGenerator.getBudgetScenario().getYearSet().contains(y))
                  .sorted(Comparator.comparing(Year::getFromDate))
                  .collect(Collectors.toList())
                  .indexOf(year)
              + 1;
    }

    Map<String, Object> variableAmountMap =
        budgetScenarioService.getVariableMap(budgetGenerator.getBudgetScenario(), yearNumber);

    if (!ObjectUtils.isEmpty(budgetStructure.getBudgetLevelList())) {
      for (BudgetLevel budgetLevel : budgetStructure.getBudgetLevelList()) {
        budgetLevelService.generateBudgetLevelFromGenerator(
            budgetLevel, null, globalBudget, variableAmountMap, true);
      }
    } else if (!ObjectUtils.isEmpty(budgetStructure.getBudgetList())) {
      for (Budget budget : budgetStructure.getBudgetList()) {
        budgetService.generateLineFromGenerator(budget, null, globalBudget);
      }
    } else if (!ObjectUtils.isEmpty(budgetStructure.getBudgetScenarioVariableSet())) {
      for (BudgetScenarioVariable budgetScenarioVariable :
          budgetStructure.getBudgetScenarioVariableSet()) {
        budgetService.generateLineFromGenerator(
            budgetScenarioVariable, null, variableAmountMap, globalBudget);
      }
    }
  }

  @Override
  public List<Map<String, Object>> visualizeVariableAmounts(BudgetGenerator budgetGenerator) {
    if (budgetGenerator.getBudgetStructure() == null
        || budgetGenerator.getBudgetScenario() == null) {
      return new ArrayList<>();
    }
    BudgetStructure budgetStructure = budgetGenerator.getBudgetStructure();
    List<BudgetScenarioLine> budgetScenarioLineOriginList =
        budgetGenerator.getBudgetScenario().getBudgetScenarioLineList();
    List<Map<String, Object>> budgetScenarioLineList = new ArrayList<>();
    if (!ObjectUtils.isEmpty(budgetStructure.getBudgetLevelList())) {
      for (BudgetLevel budgetLevel : budgetStructure.getBudgetLevelList()) {
        budgetScenarioLineList =
            budgetScenarioLineService.getLineUsingSection(
                budgetLevel,
                budgetLevel.getBudgetScenarioVariableSet(),
                budgetScenarioLineOriginList,
                budgetScenarioLineList);
      }
    } else if (!ObjectUtils.isEmpty(budgetStructure.getBudgetScenarioVariableSet())) {
      budgetScenarioLineList =
          budgetScenarioLineService.getLineUsingSection(
              null,
              budgetStructure.getBudgetScenarioVariableSet(),
              budgetScenarioLineOriginList,
              budgetScenarioLineList);
      ;
    }

    return budgetScenarioLineList;
  }
}
