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
import java.util.List;
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
      BudgetLevelRepository budgetLevelRepository) {
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
  }

  @Override
  public void computeTotals(BudgetLevel budgetLevel) {
    List<BudgetLevel> budgetLevelList = budgetLevel.getBudgetLevelList();
    BigDecimal totalAmountExpected = BigDecimal.ZERO;
    BigDecimal totalAmountCommitted = BigDecimal.ZERO;
    BigDecimal totalAmountRealized = BigDecimal.ZERO;
    BigDecimal realizedWithPo = BigDecimal.ZERO;
    BigDecimal realizedWithNoPo = BigDecimal.ZERO;
    BigDecimal totalAmountPaid = BigDecimal.ZERO;
    BigDecimal totalFirmGap = BigDecimal.ZERO;
    BigDecimal simulatedAmount = BigDecimal.ZERO;
    if (budgetLevelList != null) {
      for (BudgetLevel budgetLevelObj : budgetLevelList) {
        totalAmountExpected = totalAmountExpected.add(budgetLevelObj.getTotalAmountExpected());
        totalAmountCommitted = totalAmountCommitted.add(budgetLevelObj.getTotalAmountCommitted());
        totalAmountPaid = totalAmountPaid.add(budgetLevelObj.getTotalAmountPaid());
        totalAmountRealized = totalAmountRealized.add(budgetLevelObj.getTotalAmountRealized());
        realizedWithPo = realizedWithPo.add(budgetLevelObj.getRealizedWithPo());
        realizedWithNoPo = realizedWithNoPo.add(budgetLevelObj.getRealizedWithNoPo());
        totalFirmGap = totalFirmGap.add(budgetLevelObj.getTotalFirmGap());
        simulatedAmount = simulatedAmount.add(budgetLevelObj.getSimulatedAmount());
      }
    }
    budgetLevel.setTotalAmountExpected(totalAmountExpected);
    budgetLevel.setTotalAmountCommitted(totalAmountCommitted);
    budgetLevel.setTotalAmountPaid(totalAmountPaid);
    budgetLevel.setTotalAmountRealized(totalAmountRealized);
    budgetLevel.setRealizedWithNoPo(realizedWithNoPo);
    budgetLevel.setRealizedWithPo(realizedWithPo);
    budgetLevel.setTotalAmountAvailable(
        (totalAmountExpected.subtract(realizedWithPo).subtract(realizedWithNoPo))
            .max(BigDecimal.ZERO));
    budgetLevel.setTotalFirmGap(totalFirmGap);
    budgetLevel.setSimulatedAmount(simulatedAmount);
    budgetLevel.setAvailableAmountWithSimulated(
        (budgetLevel.getTotalAmountAvailable().subtract(simulatedAmount)).max(BigDecimal.ZERO));
  }

  @Override
  public void computeBudgetTotals(BudgetLevel budgetLevel) {
    List<Budget> budgetList = budgetLevel.getBudgetList();
    BigDecimal totalAmountExpected = BigDecimal.ZERO;
    BigDecimal totalAmountCommitted = BigDecimal.ZERO;
    BigDecimal totalAmountRealized = BigDecimal.ZERO;
    BigDecimal realizedWithPo = BigDecimal.ZERO;
    BigDecimal realizedWithNoPo = BigDecimal.ZERO;
    BigDecimal totalAmountPaid = BigDecimal.ZERO;
    BigDecimal totalFirmGap = BigDecimal.ZERO;
    BigDecimal simulatedAmount = BigDecimal.ZERO;
    if (!ObjectUtils.isEmpty(budgetList)) {
      for (Budget budget : budgetList) {
        totalAmountExpected = totalAmountExpected.add(budget.getTotalAmountExpected());
        totalAmountCommitted = totalAmountCommitted.add(budget.getTotalAmountCommitted());
        totalAmountPaid = totalAmountPaid.add(budget.getTotalAmountPaid());
        totalAmountRealized = totalAmountRealized.add(budget.getTotalAmountRealized());
        realizedWithPo = realizedWithPo.add(budget.getRealizedWithPo());
        realizedWithNoPo = realizedWithNoPo.add(budget.getRealizedWithNoPo());
        totalFirmGap = totalFirmGap.add(budget.getTotalFirmGap());
        simulatedAmount = simulatedAmount.add(budget.getSimulatedAmount());
      }
    }
    budgetLevel.setTotalAmountExpected(totalAmountExpected);
    budgetLevel.setTotalAmountCommitted(totalAmountCommitted);
    budgetLevel.setTotalAmountPaid(totalAmountPaid);
    budgetLevel.setTotalAmountRealized(totalAmountRealized);
    budgetLevel.setRealizedWithNoPo(realizedWithNoPo);
    budgetLevel.setRealizedWithPo(realizedWithPo);
    budgetLevel.setTotalAmountAvailable(
        (totalAmountExpected.subtract(realizedWithPo).subtract(realizedWithNoPo))
            .max(BigDecimal.ZERO));
    budgetLevel.setTotalFirmGap(totalFirmGap);
    budgetLevel.setSimulatedAmount(simulatedAmount);
    budgetLevel.setAvailableAmountWithSimulated(
        (budgetLevel.getTotalAmountAvailable().subtract(simulatedAmount)).max(BigDecimal.ZERO));
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
            .filter(
                "self.budgetLevel.parentBudgetLevel.globalBudget.id = ?", globalBudgetLevel.getId())
            .fetch();

    for (Budget budget : budgetList) {
      budgetService.createBudgetKey(budget);
      budgetRepository.save(budget);
    }
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
  public void getUpdatedGroupBudgetLevelList(
      List<BudgetLevel> groupBudgetLevelList, LocalDate fromDate, LocalDate toDate)
      throws AxelorException {
    if (CollectionUtils.isNotEmpty(groupBudgetLevelList)) {
      for (BudgetLevel groupBudgetLevel : groupBudgetLevelList) {
        if (groupBudgetLevel.getId() != null) {
          updateBudgetLevelDates(groupBudgetLevel, fromDate, toDate);
          getUpdatedSectionBudgetList(groupBudgetLevel.getBudgetLevelList(), fromDate, toDate);
        }
      }
    }
  }

  @Override
  public void getUpdatedSectionBudgetList(
      List<BudgetLevel> sectionBudgetLevelList, LocalDate fromDate, LocalDate toDate)
      throws AxelorException {

    if (CollectionUtils.isNotEmpty(sectionBudgetLevelList)) {
      for (BudgetLevel sectionBudgetLevel : sectionBudgetLevelList) {
        if (sectionBudgetLevel.getId() != null) {
          updateBudgetLevelDates(sectionBudgetLevel, fromDate, toDate);
          getUpdatedBudgetList(sectionBudgetLevel.getBudgetList(), fromDate, toDate);
        }
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
    budgetLevel = budgetLevelManagementRepository.find(budgetLevel.getId());
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
        if (budgetLevel.getParentBudgetLevel() != null
            && budgetLevel.getParentBudgetLevel().getGlobalBudget() != null) {
          Company company = budgetLevel.getParentBudgetLevel().getGlobalBudget().getCompany();
          checkBudgetKey = budgetService.checkBudgetKeyInConfig(company);
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
      budgetLevel.setStatusSelect(BudgetLevelRepository.BUDGET_LEVEL_STATUS_SELECT_DRAFT);
      budgetLevelManagementRepository.save(budgetLevel);
    }
  }

  @Override
  public Integer getBudgetControlLevel(Budget budget) {

    if (appBudgetService.getAppBudget() == null
        || !appBudgetService.getAppBudget().getCheckAvailableBudget()) {
      return null;
    }

    if (budget != null
        && budget.getBudgetLevel() != null
        && budget.getBudgetLevel().getParentBudgetLevel() != null
        && budget.getBudgetLevel().getParentBudgetLevel().getGlobalBudget() != null
        && budget
                .getBudgetLevel()
                .getParentBudgetLevel()
                .getGlobalBudget()
                .getCheckAvailableSelect()
            != null
        && budget
                .getBudgetLevel()
                .getParentBudgetLevel()
                .getGlobalBudget()
                .getCheckAvailableSelect()
            != 0) {
      return budget
          .getBudgetLevel()
          .getParentBudgetLevel()
          .getGlobalBudget()
          .getCheckAvailableSelect();
    } else {
      return appBudgetService.getAppBudget().getCheckAvailableBudget()
          ? BudgetLevelRepository.BUDGET_LEVEL_AVAILABLE_AMOUNT_BUDGET_LINE
          : null;
    }
  }

  @Override
  @Transactional
  public void computeChildrenKey(BudgetLevel section) throws AxelorException {
    if (section.getId() == null) {
      return;
    }
    if (!CollectionUtils.isEmpty(section.getBudgetList())) {
      for (Budget budget : section.getBudgetList()) {
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
  @Transactional(rollbackOn = {RuntimeException.class})
  public void computeBudgetLevelTotals(Budget budget) {

    BudgetLevel sectionBudgetLevel = budget.getBudgetLevel();

    if (sectionBudgetLevel != null) {

      computeBudgetTotals(sectionBudgetLevel);
      budgetLevelRepository.save(sectionBudgetLevel);

      BudgetLevel groupBudgetLevel = sectionBudgetLevel.getParentBudgetLevel();
      if (groupBudgetLevel != null) {
        computeTotals(groupBudgetLevel);
        budgetLevelRepository.save(groupBudgetLevel);
      }
    }
  }

  @Override
  public void recomputeBudgetLevelTotals(BudgetLevel budgetLevel) {
    if (!ObjectUtils.isEmpty(budgetLevel.getBudgetLevelList())) {
      for (BudgetLevel child : budgetLevel.getBudgetLevelList()) {
        recomputeBudgetLevelTotals(child);
      }
    }

    computeBudgetTotals(budgetLevel);
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

    if (!ObjectUtils.isEmpty(budgetLevel.getBudgetLevelList())) {
      budgetLevel.getBudgetLevelList().forEach(child -> resetBudgetLevel(child));
    } else if (!CollectionUtils.isEmpty(budgetLevel.getBudgetList())) {
      budgetLevel.getBudgetList().forEach(child -> budgetService.resetBudget(child));
    }
  }
}
