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
import com.axelor.apps.base.db.AdvancedImport;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.ImportHistory;
import com.axelor.apps.base.db.repo.AdvancedImportRepository;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.exceptions.BaseExceptionMessage;
import com.axelor.apps.base.service.advanced.imports.AdvancedImportService;
import com.axelor.apps.base.service.advanced.imports.DataImportService;
import com.axelor.apps.base.service.advanced.imports.ValidatorService;
import com.axelor.apps.budget.db.Budget;
import com.axelor.apps.budget.db.BudgetLevel;
import com.axelor.apps.budget.db.BudgetScenarioVariable;
import com.axelor.apps.budget.db.GlobalBudget;
import com.axelor.apps.budget.db.repo.AdvancedImportBudgetRepository;
import com.axelor.apps.budget.db.repo.BudgetLevelManagementRepository;
import com.axelor.apps.budget.db.repo.BudgetLevelRepository;
import com.axelor.apps.budget.db.repo.BudgetManagementRepository;
import com.axelor.apps.budget.exception.BudgetExceptionMessage;
import com.axelor.apps.project.db.Project;
import com.axelor.apps.project.db.repo.ProjectRepository;
import com.axelor.common.ObjectUtils;
import com.axelor.i18n.I18n;
import com.axelor.meta.db.MetaFile;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import com.google.inject.servlet.RequestScoped;
import java.io.IOException;
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
    List<Budget> budgetList = budgetLevel.getBudgetList();
    List<BudgetLevel> budgetLevelList = budgetLevel.getBudgetLevelList();
    Map<String, BigDecimal> amountByField =
        budgetToolsService.buildMapWithAmounts(budgetList, budgetLevelList);

    budgetLevel.setTotalAmountExpected(amountByField.get("totalAmountExpected"));
    budgetLevel.setTotalAmountCommitted(amountByField.get("totalAmountCommitted"));
    budgetLevel.setTotalAmountPaid(amountByField.get("totalAmountPaid"));
    budgetLevel.setTotalAmountRealized(amountByField.get("totalAmountRealized"));
    budgetLevel.setRealizedWithNoPo(amountByField.get("realizedWithNoPo"));
    budgetLevel.setRealizedWithPo(amountByField.get("realizedWithPo"));
    budgetLevel.setTotalAmountAvailable(
        (amountByField
                .get("totalAmountExpected")
                .subtract(amountByField.get("realizedWithPo"))
                .subtract(amountByField.get("realizedWithNoPo")))
            .max(BigDecimal.ZERO));
    budgetLevel.setTotalFirmGap(amountByField.get("totalFirmGap"));
    budgetLevel.setSimulatedAmount(amountByField.get("simulatedAmount"));
    budgetLevel.setAvailableAmountWithSimulated(
        (budgetLevel.getTotalAmountAvailable().subtract(amountByField.get("simulatedAmount")))
            .max(BigDecimal.ZERO));
  }

  @Override
  public MetaFile importBudgetLevel(BudgetLevel globalBudgetLevel)
      throws ClassNotFoundException, AxelorException, IOException {
    MetaFile errorLogFile = null;
    if (globalBudgetLevel != null && globalBudgetLevel.getImportFile() != null) {
      AdvancedImport advancedImport =
          advancedImportRepo
              .all()
              .filter("self.importId = ?", AdvancedImportBudgetRepository.IMPORT_ID_2)
              .fetchOne();

      if (advancedImport != null) {

        AdvancedImport copyAdvancedImport = advancedImportRepo.copy(advancedImport, false);
        copyAdvancedImport.setImportFile(globalBudgetLevel.getImportFile());
        advancedImportService.apply(copyAdvancedImport);
        Boolean isLog = validatorService.validate(copyAdvancedImport);

        if (isLog) {
          throw new AxelorException(
              TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
              I18n.get(BaseExceptionMessage.ADVANCED_IMPORT_CHECK_LOG));
        }

        ImportHistory importHistory = this.importData(copyAdvancedImport);
        errorLogFile = importHistory.getLogMetaFile();

        if (errorLogFile == null) {
          this.computeBudgetLevel(globalBudgetLevel);
        }

        this.removeCopyAdvancedImport(copyAdvancedImport);
      }
    }
    return errorLogFile;
  }

  @Transactional(rollbackOn = {RuntimeException.class})
  protected void removeCopyAdvancedImport(AdvancedImport copyAdvancedImport) {
    advancedImportRepo.remove(copyAdvancedImport);
  }

  protected ImportHistory importData(AdvancedImport advancedImport)
      throws ClassNotFoundException, IOException, AxelorException {
    return dataImportService.importData(advancedImport);
  }

  @Override
  @Transactional(rollbackOn = {RuntimeException.class})
  public void computeBudgetLevel(BudgetLevel globalBudgetLevel) throws AxelorException {
    List<Budget> budgetList =
        budgetRepository
            .all()
            .filter("self.globalBudget.id = ?", globalBudgetLevel.getId())
            .fetch();

    for (Budget budget : budgetList) {
      budgetService.createBudgetKey(budget);
      budgetRepository.save(budget);
    }
  }

  @Override
  public void archiveChildren(BudgetLevel budgetLevel) {

    if (budgetLevel == null) {
      return;
    }

    if (!CollectionUtils.isEmpty(budgetLevel.getBudgetLevelList())) {
      for (BudgetLevel child : budgetLevel.getBudgetLevelList()) {
        archiveChildren(child);
      }
    } else if (!CollectionUtils.isEmpty(budgetLevel.getBudgetList())) {
      for (Budget budget : budgetLevel.getBudgetList()) {
        budgetService.archiveBudget(budget);
      }
    }

    archiveBudgetLevel(budgetLevel);
  }

  @Transactional
  protected void archiveBudgetLevel(BudgetLevel budgetLevel) {
    if (budgetLevel != null) {
      budgetLevel.setStatusSelect(BudgetLevelRepository.BUDGET_LEVEL_STATUS_SELECT_ARCHIVED);
      budgetLevel.setArchived(true);
      budgetLevelRepository.save(budgetLevel);
    }
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
  @Transactional(rollbackOn = {Exception.class})
  public void setProjectBudget(BudgetLevel budgetLevel) {
    Project project = budgetLevel.getProject();

    if (project.getBudget() != null) {
      return;
    }

    project.setBudget(budgetLevel);
    projectRepo.save(project);
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
          checkBudgetKey = budgetService.checkBudgetKeyInConfig(company);
        }
        for (Budget budget : budgetLevel.getBudgetList()) {
          budgetService.validateBudget(budget, checkBudgetKey);
        }
      }
    }
    validateLevel(budgetLevel);
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void validateLevel(BudgetLevel budgetLevel) {
    if (budgetLevel != null) {
      budgetLevel = budgetLevelRepository.find(budgetLevel.getId());
      budgetLevel.setStatusSelect(BudgetLevelRepository.BUDGET_LEVEL_STATUS_SELECT_VALID);
      budgetLevelRepository.save(budgetLevel);
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

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void draftLevel(BudgetLevel budgetLevel) {
    if (budgetLevel != null) {
      budgetLevel = budgetLevelRepository.find(budgetLevel.getId());
      budgetLevel.setStatusSelect(BudgetLevelRepository.BUDGET_LEVEL_STATUS_SELECT_DRAFT);
      budgetLevelRepository.save(budgetLevel);
    }
  }

  @Override
  @Transactional
  public void computeChildrenKey(BudgetLevel level) throws AxelorException {
    if (level.getId() == null) {
      return;
    }
    if (!ObjectUtils.isEmpty(level.getBudgetList())) {
      for (Budget budget : level.getBudgetList()) {
        budgetService.createBudgetKey(budget);
      }
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
    LocalDate fromDate = null;
    LocalDate toDate = null;
    if (budgetLevel != null && budgetLevel.getParentBudgetLevel() != null) {
      fromDate = budgetLevel.getParentBudgetLevel().getFromDate();
      toDate = budgetLevel.getParentBudgetLevel().getToDate();
    } else if (budgetLevel != null && budgetLevel.getGlobalBudget() != null) {
      fromDate = budgetLevel.getGlobalBudget().getFromDate();
      toDate = budgetLevel.getGlobalBudget().getToDate();
    }

    if ((budgetLevel.getFromDate() == null
            || (budgetLevel.getFromDate() != null && budgetLevel.getFromDate().isBefore(fromDate)))
        || (budgetLevel.getToDate() == null
            || (budgetLevel.getToDate() != null && budgetLevel.getToDate().isAfter(toDate)))) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          String.format(
              I18n.get(BudgetExceptionMessage.WRONG_DATES_ON_BUDGET_LEVEL), budgetLevel.getCode()));
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
  public void resetBudgetLevel(BudgetLevel budgetLevel) {

    budgetLevel.setStatusSelect(BudgetLevelRepository.BUDGET_LEVEL_STATUS_SELECT_DRAFT);

    budgetLevel.setArchived(false);

    budgetLevel.setTotalAmountCommitted(BigDecimal.ZERO);
    budgetLevel.setTotalAmountAvailable(budgetLevel.getTotalAmountExpected());
    budgetLevel.setAvailableAmountWithSimulated(budgetLevel.getTotalAmountExpected());
    budgetLevel.setRealizedWithNoPo(BigDecimal.ZERO);
    budgetLevel.setRealizedWithPo(BigDecimal.ZERO);
    budgetLevel.setSimulatedAmount(BigDecimal.ZERO);
    budgetLevel.setTotalFirmGap(BigDecimal.ZERO);
    budgetLevel.setTotalAmountPaid(BigDecimal.ZERO);
    budgetLevel.setBudgetStructure(null);

    if (!ObjectUtils.isEmpty(budgetLevel.getBudgetLevelList())) {
      budgetLevel.getBudgetLevelList().forEach(child -> resetBudgetLevel(child));
    } else if (!CollectionUtils.isEmpty(budgetLevel.getBudgetList())) {
      budgetLevel.getBudgetList().forEach(child -> budgetService.resetBudget(child));
    }
  }

  @Override
  public List<Budget> getAllBudgets(BudgetLevel budgetLevel, List<Budget> budgetList) {
    if (budgetLevel == null) {
      return budgetList;
    }

    if (!ObjectUtils.isEmpty(budgetLevel.getBudgetList())) {
      budgetList.addAll(budgetLevel.getBudgetList());
      return budgetList;
    }
    if (!ObjectUtils.isEmpty(budgetLevel.getBudgetLevelList())) {
      for (BudgetLevel child : budgetLevel.getBudgetLevelList()) {
        getAllBudgets(child, budgetList);
      }
    }
    return budgetList;
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
}
