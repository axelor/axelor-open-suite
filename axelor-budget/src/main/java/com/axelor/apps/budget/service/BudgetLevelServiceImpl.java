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

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.repo.AdvancedImportRepository;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.advanced.imports.AdvancedImportService;
import com.axelor.apps.base.service.advanced.imports.DataImportService;
import com.axelor.apps.base.service.advanced.imports.ValidatorService;
import com.axelor.apps.budget.db.Budget;
import com.axelor.apps.budget.db.BudgetLevel;
import com.axelor.apps.budget.db.BudgetScenarioVariable;
import com.axelor.apps.budget.db.GlobalBudget;
import com.axelor.apps.budget.db.repo.BudgetLevelManagementRepository;
import com.axelor.apps.budget.db.repo.BudgetLevelRepository;
import com.axelor.apps.budget.db.repo.BudgetManagementRepository;
import com.axelor.apps.budget.exception.BudgetExceptionMessage;
import com.axelor.apps.project.db.repo.ProjectRepository;
import com.axelor.common.ObjectUtils;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import com.google.inject.servlet.RequestScoped;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.commons.collections.CollectionUtils;

@RequestScoped
public class BudgetLevelServiceImpl implements BudgetLevelService {

  protected BudgetLevelManagementRepository budgetLevelManagementRepository;
  protected AdvancedImportRepository advancedImportRepo;
  protected AdvancedImportService advancedImportService;
  protected ValidatorService validatorService;
  protected DataImportService dataImportService;
  protected ProjectRepository projectRepo;
  protected BudgetService budgetService;
  protected BudgetManagementRepository budgetRepository;
  protected AppBudgetService appBudgetService;
  protected BudgetLevelRepository budgetLevelRepository;
  protected BudgetToolsService budgetToolsService;

  @Inject
  public BudgetLevelServiceImpl(
      BudgetLevelManagementRepository budgetLevelManagementRepository,
      AdvancedImportRepository advancedImportRepo,
      AdvancedImportService advancedImportService,
      DataImportService dataImportService,
      ValidatorService validatorService,
      ProjectRepository projectRepo,
      BudgetService budgetService,
      BudgetManagementRepository budgetRepository,
      AppBudgetService appBudgetService,
      BudgetLevelRepository budgetLevelRepository,
      BudgetToolsService budgetToolsService) {
    this.budgetLevelManagementRepository = budgetLevelManagementRepository;
    this.advancedImportRepo = advancedImportRepo;
    this.advancedImportService = advancedImportService;
    this.validatorService = validatorService;
    this.dataImportService = dataImportService;
    this.projectRepo = projectRepo;
    this.budgetService = budgetService;
    this.budgetRepository = budgetRepository;
    this.appBudgetService = appBudgetService;
    this.budgetLevelRepository = budgetLevelRepository;
    this.budgetToolsService = budgetToolsService;
  }

  @Override
  public void computeTotals(BudgetLevel budgetLevel) {
    Map<String, BigDecimal> amountByField =
        budgetToolsService.buildMapWithAmounts(
            budgetLevel.getBudgetList(), budgetLevel.getBudgetLevelList());
    budgetLevel.setTotalAmountExpected(amountByField.get("totalAmountExpected"));
    budgetLevel.setTotalAmountCommitted(amountByField.get("totalAmountCommitted"));
    budgetLevel.setTotalAmountPaid(amountByField.get("totalAmountPaid"));
    budgetLevel.setTotalAmountRealized(amountByField.get("totalAmountRealized"));
    budgetLevel.setRealizedWithNoPo(amountByField.get("realizedWithNoPo"));
    budgetLevel.setRealizedWithPo(amountByField.get("realizedWithPo"));
    budgetLevel.setTotalAmountAvailable(
        amountByField
            .get("totalAmountExpected")
            .subtract(amountByField.get("realizedWithPo"))
            .subtract(amountByField.get("realizedWithNoPo"))
            .max(BigDecimal.ZERO));
    budgetLevel.setTotalFirmGap(amountByField.get("totalFirmGap"));
    budgetLevel.setSimulatedAmount(amountByField.get("simulatedAmount"));
    budgetLevel.setAvailableAmountWithSimulated(
        budgetLevel
            .getTotalAmountAvailable()
            .subtract(amountByField.get("simulatedAmount"))
            .max(BigDecimal.ZERO));
  }

  @Override
  @Transactional
  public void archiveBudgetLevel(BudgetLevel globalBudgetLevel) {
    globalBudgetLevel = budgetLevelManagementRepository.find(globalBudgetLevel.getId());

    if (globalBudgetLevel == null) {
      return;
    }
    globalBudgetLevel.setStatusSelect(BudgetLevelRepository.BUDGET_LEVEL_STATUS_SELECT_ARCHIVED);
    globalBudgetLevel.setArchived(true);

    if (!CollectionUtils.isEmpty(globalBudgetLevel.getBudgetLevelList())) {
      for (BudgetLevel section : globalBudgetLevel.getBudgetLevelList()) {
        section.setStatusSelect(BudgetLevelRepository.BUDGET_LEVEL_STATUS_SELECT_ARCHIVED);
        section.setArchived(true);

        if (!CollectionUtils.isEmpty(section.getBudgetList())) {
          for (Budget budget : section.getBudgetList()) {
            budget.setArchived(true);
            budgetRepository.save(budget);
          }
        }
      }
    }
    budgetLevelManagementRepository.save(globalBudgetLevel);
  }

  @Override
  public void getUpdatedBudgetLevelList(
      List<BudgetLevel> budgetLevelList, LocalDate fromDate, LocalDate toDate)
      throws AxelorException {
    if (CollectionUtils.isNotEmpty(budgetLevelList)) {
      for (BudgetLevel budgetLevel : budgetLevelList) {
        if (!ObjectUtils.isEmpty(budgetLevel.getBudgetLevelList())) {
          getUpdatedBudgetLevelList(budgetLevel.getBudgetLevelList(), fromDate, toDate);
        } else if (!ObjectUtils.isEmpty(budgetLevel.getBudgetList())) {
          getUpdatedBudgetList(budgetLevel.getBudgetList(), fromDate, toDate);
        }

        updateBudgetLevelDates(budgetLevel, fromDate, toDate);
      }
    }
  }

  @Override
  public void getUpdatedBudgetList(List<Budget> budgetList, LocalDate fromDate, LocalDate toDate)
      throws AxelorException {

    if (CollectionUtils.isNotEmpty(budgetList)) {
      for (Budget budget : budgetList) {
        if (budget.getId() != null) {
          budgetService.updateBudgetDates(budget, fromDate, toDate);
          budgetService.getUpdatedBudgetLineList(budget, fromDate, toDate);
        }
      }
    }
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void updateBudgetLevelDates(
      BudgetLevel budgetLevel, LocalDate fromDate, LocalDate toDate) {
    budgetLevel.setFromDate(fromDate);
    budgetLevel.setToDate(toDate);
    budgetLevelManagementRepository.save(budgetLevel);
  }

  @Override
  public void validateChildren(BudgetLevel budgetLevel) throws AxelorException {
    if (!ObjectUtils.isEmpty(budgetLevel.getBudgetLevelList())) {
      for (BudgetLevel budgetLevelChild : budgetLevel.getBudgetLevelList()) {
        validateChildren(budgetLevelChild);
      }
    } else {
      if (!CollectionUtils.isEmpty(budgetLevel.getBudgetList())) {
        boolean checkBudgetKey = false;
        GlobalBudget globalBudget = budgetToolsService.getGlobalBudgetUsingBudgetLevel(budgetLevel);
        if (globalBudget != null) {
          Company company = globalBudget.getCompany();
          checkBudgetKey = budgetToolsService.checkBudgetKeyInConfig(company);
        }
        for (Budget budget : budgetLevel.getBudgetList()) {
          budgetService.validateBudget(budget, checkBudgetKey);
        }
      }
    }
    validateLevel(budgetLevel);
  }

  @Transactional(rollbackOn = {Exception.class})
  protected void validateLevel(BudgetLevel budgetLevel) {
    if (budgetLevel != null) {
      budgetLevel = budgetLevelRepository.find(budgetLevel.getId());
      budgetLevel.setStatusSelect(BudgetLevelRepository.BUDGET_LEVEL_STATUS_SELECT_VALID);
      budgetLevelManagementRepository.save(budgetLevel);
    }
  }

  @Override
  public void draftChildren(BudgetLevel budgetLevel) {
    if (!ObjectUtils.isEmpty(budgetLevel.getBudgetLevelList())) {
      for (BudgetLevel budgetLevelChild : budgetLevel.getBudgetLevelList()) {
        draftChildren(budgetLevelChild);
      }
    } else {
      if (!CollectionUtils.isEmpty(budgetLevel.getBudgetList())) {
        for (Budget budget : budgetLevel.getBudgetList()) {
          budgetService.draftBudget(budget);
        }
      }
    }
    draftLevel(budgetLevel);
  }

  @Transactional(rollbackOn = {Exception.class})
  protected void draftLevel(BudgetLevel budgetLevel) {
    if (budgetLevel != null) {
      budgetLevel = budgetLevelRepository.find(budgetLevel.getId());
      budgetLevel.setStatusSelect(BudgetLevelRepository.BUDGET_LEVEL_STATUS_SELECT_DRAFT);
      budgetLevelManagementRepository.save(budgetLevel);
    }
  }

  @Override
  public void validateDates(BudgetLevel budgetLevel) throws AxelorException {
    if (!ObjectUtils.isEmpty(budgetLevel.getBudgetLevelList())) {
      for (BudgetLevel budgetLevelChild : budgetLevel.getBudgetLevelList()) {
        validateDates(budgetLevelChild);
      }
    } else {
      if (!CollectionUtils.isEmpty(budgetLevel.getBudgetList())) {
        for (Budget budget : budgetLevel.getBudgetList()) {
          budgetService.checkDatesOnBudget(budget);
        }
      }
    }
    validateBudgetLevelDates(budgetLevel);
  }

  @Override
  public void validateBudgetLevelDates(BudgetLevel budgetLevel) throws AxelorException {
    if (budgetLevel != null && budgetLevel.getParentBudgetLevel() != null) {
      BudgetLevel parent = budgetLevel.getParentBudgetLevel();
      if ((budgetLevel.getFromDate() == null
              || (budgetLevel.getFromDate() != null
                  && budgetLevel.getFromDate().isBefore(parent.getFromDate())))
          || (budgetLevel.getToDate() == null
              || (budgetLevel.getToDate() != null
                  && budgetLevel.getToDate().isAfter(parent.getToDate())))) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
            String.format(
                I18n.get(BudgetExceptionMessage.WRONG_DATES_ON_BUDGET_LEVEL),
                budgetLevel.getCode()));
      }
    }
  }

  @Override
  public void computeBudgetLevelTotals(Budget budget) {

    BudgetLevel sectionBudgetLevel = budget.getBudgetLevel();

    computeLevelTotals(sectionBudgetLevel);
  }

  @Transactional
  protected void computeLevelTotals(BudgetLevel budgetLevel) {
    if (budgetLevel == null) {
      return;
    }
    computeTotals(budgetLevel);

    computeLevelTotals(budgetLevel.getParentBudgetLevel());
  }

  @Override
  public List<BudgetLevel> getLastSections(GlobalBudget globalBudget) {
    List<BudgetLevel> budgetLevelList = new ArrayList<>();

    if (ObjectUtils.isEmpty(globalBudget.getBudgetLevelList())) {
      return budgetLevelList;
    }

    for (BudgetLevel budgetLevel : globalBudget.getBudgetLevelList()) {
      getLastSection(budgetLevel, budgetLevelList);
    }
    return budgetLevelList;
  }

  protected void getLastSection(BudgetLevel budgetLevel, List<BudgetLevel> budgetLevelList) {
    if (budgetLevel != null && ObjectUtils.isEmpty(budgetLevel.getBudgetLevelList())) {
      budgetLevelList.add(budgetLevel);
      return;
    }

    for (BudgetLevel child : budgetLevel.getBudgetLevelList()) {
      getLastSection(child, budgetLevelList);
    }
  }

  @Override
  public void generateBudgetLevelFromGenerator(
      BudgetLevel budgetLevel,
      BudgetLevel parent,
      GlobalBudget globalBudget,
      Map<String, Object> variableAmountMap,
      boolean linkToGlobal)
      throws AxelorException {
    BudgetLevel optBudgetLevel = budgetLevelManagementRepository.copy(budgetLevel, false);
    optBudgetLevel.setFromDate(globalBudget.getFromDate());
    optBudgetLevel.setToDate(globalBudget.getToDate());
    optBudgetLevel.setTypeSelect(BudgetLevelRepository.BUDGET_LEVEL_TYPE_SELECT_BUDGET);
    optBudgetLevel.setSourceSelect(BudgetLevelRepository.BUDGET_LEVEL_SOURCE_AUTO);
    optBudgetLevel.setBudgetTypeSelect(globalBudget.getBudgetTypeSelect());
    optBudgetLevel.setBudgetStructure(null);
    if (parent != null) {
      parent.addBudgetLevelListItem(optBudgetLevel);
    }
    if (linkToGlobal) {
      globalBudget.addBudgetLevelListItem(optBudgetLevel);
    }

    if (!ObjectUtils.isEmpty(budgetLevel.getBudgetLevelList())) {
      for (BudgetLevel child : budgetLevel.getBudgetLevelList()) {
        generateBudgetLevelFromGenerator(
            child, optBudgetLevel, globalBudget, variableAmountMap, false);
      }
    } else if (!ObjectUtils.isEmpty(budgetLevel.getBudgetList())) {
      for (Budget budget : budgetLevel.getBudgetList()) {
        budgetService.generateLineFromGenerator(budget, optBudgetLevel, globalBudget);
      }
    } else if (!ObjectUtils.isEmpty(budgetLevel.getBudgetScenarioVariableSet())) {
      for (BudgetScenarioVariable budgetScenarioVariable :
          budgetLevel.getBudgetScenarioVariableSet()) {
        budgetService.generateLineFromGenerator(
            budgetScenarioVariable, optBudgetLevel, variableAmountMap, globalBudget);
      }
    }
  }
}
