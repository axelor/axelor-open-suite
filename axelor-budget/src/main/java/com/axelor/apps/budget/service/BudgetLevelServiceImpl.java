package com.axelor.apps.budget.service;

import com.axelor.apps.budget.db.Budget;
import com.axelor.apps.budget.db.repo.BudgetBudgetRepository;
import com.axelor.apps.budget.db.repo.BudgetRepository;
import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.AdvancedImport;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.ImportHistory;
import com.axelor.apps.base.db.repo.AdvancedImportBudgetRepository;
import com.axelor.apps.base.db.repo.AdvancedImportRepository;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.exceptions.BaseExceptionMessage;
import com.axelor.apps.base.service.advanced.imports.AdvancedImportService;
import com.axelor.apps.base.service.advanced.imports.DataImportService;
import com.axelor.apps.base.service.advanced.imports.ValidatorService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.budget.db.BudgetLevel;
import com.axelor.apps.budget.db.repo.BudgetLevelManagementRepository;
import com.axelor.apps.budget.db.repo.BudgetLevelRepository;
import com.axelor.apps.project.db.Project;
import com.axelor.apps.project.db.repo.ProjectRepository;
import com.axelor.apps.purchase.db.repo.PurchaseOrderLineRepository;
import com.axelor.apps.purchase.db.repo.PurchaseOrderRepository;
import com.axelor.auth.AuthUtils;
import com.axelor.common.ObjectUtils;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
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
  protected AppBaseService appBaseService;
  protected PurchaseOrderRepository purchaseOrderRepo;
  protected PurchaseOrderLineRepository purchaseOrderLineRepo;
  protected ProjectRepository projectRepo;
  protected BudgetBudgetService budgetBudgetService;
  protected AppAccountService appAccountService;
  protected BudgetBudgetRepository budgetBudgetRepository;

  @Inject
  public BudgetLevelServiceImpl(
      BudgetLevelManagementRepository budgetLevelManagementRepository,
      AdvancedImportRepository advancedImportRepo,
      AdvancedImportService advancedImportService,
      DataImportService dataImportService,
      ValidatorService validatorService,
      AppBaseService appBaseService,
      PurchaseOrderRepository purchaseOrderRepo,
      PurchaseOrderLineRepository purchaseOrderLineRepo,
      ProjectRepository projectRepo,
      BudgetBudgetService budgetBudgetService,
      AppAccountService appAccountService,
      BudgetBudgetRepository budgetBudgetRepository) {
    this.budgetLevelManagementRepository = budgetLevelManagementRepository;
    this.advancedImportRepo = advancedImportRepo;
    this.advancedImportService = advancedImportService;
    this.validatorService = validatorService;
    this.dataImportService = dataImportService;
    this.appBaseService = appBaseService;
    this.purchaseOrderRepo = purchaseOrderRepo;
    this.purchaseOrderLineRepo = purchaseOrderLineRepo;
    this.projectRepo = projectRepo;
    this.budgetBudgetService = budgetBudgetService;
    this.appAccountService = appAccountService;
    this.budgetBudgetRepository = budgetBudgetRepository;
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
                    .compareTo(BigDecimal.ZERO)
                > 0
            ? totalAmountExpected.subtract(realizedWithPo).subtract(realizedWithNoPo)
            : BigDecimal.ZERO);
    budgetLevel.setTotalFirmGap(totalFirmGap);
    budgetLevel.setSimulatedAmount(simulatedAmount);
    budgetLevel.setAvailableAmountWithSimulated(
        (budgetLevel.getTotalAmountAvailable().subtract(simulatedAmount)).compareTo(BigDecimal.ZERO)
                > 0
            ? (budgetLevel.getTotalAmountAvailable().subtract(simulatedAmount))
            : BigDecimal.ZERO);
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
    if (budgetList != null) {
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
                    .compareTo(BigDecimal.ZERO)
                > 0
            ? totalAmountExpected.subtract(realizedWithPo).subtract(realizedWithNoPo)
            : BigDecimal.ZERO);
    budgetLevel.setTotalFirmGap(totalFirmGap);
    budgetLevel.setSimulatedAmount(simulatedAmount);
    budgetLevel.setAvailableAmountWithSimulated(
        (budgetLevel.getTotalAmountAvailable().subtract(simulatedAmount)).compareTo(BigDecimal.ZERO)
                > 0
            ? (budgetLevel.getTotalAmountAvailable().subtract(simulatedAmount))
            : BigDecimal.ZERO);
  }

  @Override
  @Transactional(rollbackOn = {RuntimeException.class})
  public BudgetLevel createGlobalBudgets(BudgetLevel budgetLevel) {

    List<BudgetLevel> groupBudgetLevelList = budgetLevel.getBudgetLevelList();

    BudgetLevel optGlobalBudgetLevel = budgetLevelManagementRepository.copy(budgetLevel, false);
    optGlobalBudgetLevel.setStatusSelect(BudgetLevelRepository.BUDGET_LEVEL_STATUS_SELECT_DRAFT);
    optGlobalBudgetLevel.setTypeSelect(BudgetLevelRepository.BUDGET_LEVEL_TYPE_SELECT_BUDGET);
    optGlobalBudgetLevel.setSourceSelect(BudgetLevelRepository.BUDGET_LEVEL_SOURCE_AUTO);
    if (AuthUtils.getUser() != null) {
      if (optGlobalBudgetLevel.getCompanyDepartment() == null) {
        optGlobalBudgetLevel.setCompanyDepartment(AuthUtils.getUser().getCompanyDepartment());
      }
      optGlobalBudgetLevel.setBudgetManager(AuthUtils.getUser());
    }

    for (BudgetLevel groupBudgetLevel : groupBudgetLevelList) {
      List<BudgetLevel> sectionBudgetLevelList = groupBudgetLevel.getBudgetLevelList();

      BudgetLevel optGroupBudgetLevel =
          budgetLevelManagementRepository.copy(groupBudgetLevel, false);
      optGroupBudgetLevel.setStatusSelect(BudgetLevelRepository.BUDGET_LEVEL_STATUS_SELECT_DRAFT);
      optGroupBudgetLevel.setTypeSelect(BudgetLevelRepository.BUDGET_LEVEL_TYPE_SELECT_BUDGET);
      optGroupBudgetLevel.setSourceSelect(BudgetLevelRepository.BUDGET_LEVEL_SOURCE_AUTO);

      for (BudgetLevel sectionBudgetLevel : sectionBudgetLevelList) {
        List<Budget> budgetList = sectionBudgetLevel.getBudgetList();

        BudgetLevel optSectionBudgetLevel =
            budgetLevelManagementRepository.copy(sectionBudgetLevel, false);
        optSectionBudgetLevel.setStatusSelect(
            BudgetLevelRepository.BUDGET_LEVEL_STATUS_SELECT_DRAFT);
        optSectionBudgetLevel.setTypeSelect(BudgetLevelRepository.BUDGET_LEVEL_TYPE_SELECT_BUDGET);
        optSectionBudgetLevel.setSourceSelect(BudgetLevelRepository.BUDGET_LEVEL_SOURCE_AUTO);

        for (Budget budget : budgetList) {
          Budget optBudget = budgetBudgetRepository.copy(budget, true);
          optBudget.setTypeSelect(BudgetRepository.BUDGET_TYPE_SELECT_BUDGET);
          optBudget.setSourceSelect(BudgetRepository.BUDGET_SOURCE_AUTO);
          optBudget.clearAccountModelSet();
          optBudget.setBudgetModel(budget);
          optSectionBudgetLevel.addBudgetListItem(optBudget);
        }
        optGroupBudgetLevel.addBudgetLevelListItem(optSectionBudgetLevel);
      }
      optGlobalBudgetLevel.addBudgetLevelListItem(optGroupBudgetLevel);
    }
    budgetLevelManagementRepository.save(optGlobalBudgetLevel);

    return optGlobalBudgetLevel;
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
        budgetBudgetRepository
            .all()
            .filter(
                "self.budgetLevel.parentBudgetLevel.parentBudgetLevel.id = ?",
                globalBudgetLevel.getId())
            .fetch();

    for (Budget budget : budgetList) {
      budgetBudgetService.createBudgetKey(budget);
      budgetBudgetRepository.save(budget);
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
      for (BudgetLevel group : globalBudgetLevel.getBudgetLevelList()) {
        group.setStatusSelect(BudgetLevelRepository.BUDGET_LEVEL_STATUS_SELECT_ARCHIVED);
        group.setArchived(true);
        if (!CollectionUtils.isEmpty(group.getBudgetLevelList())) {
          for (BudgetLevel section : group.getBudgetLevelList()) {
            section.setStatusSelect(BudgetLevelRepository.BUDGET_LEVEL_STATUS_SELECT_ARCHIVED);
            section.setArchived(true);
            if (!CollectionUtils.isEmpty(section.getBudgetList())) {
              for (Budget budget : section.getBudgetList()) {
                budget.setArchived(true);
              }
            }
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
      BudgetLevelService budgetLevelService = Beans.get(BudgetLevelService.class);
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
      BudgetLevelService budgetLevelService = Beans.get(BudgetLevelService.class);
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
      BudgetBudgetService budgetBudgetService = Beans.get(BudgetBudgetService.class);
      for (Budget budget : budgetList) {
        if (budget.getId() != null) {
          budgetBudgetService.updateBudgetDates(budget, fromDate, toDate);
          budgetBudgetService.getUpdatedBudgetLineList(budget, fromDate, toDate);
        }
      }
    }
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void updateBudgetLevelDates(BudgetLevel budgetLevel, LocalDate fromDate, LocalDate toDate)
      throws AxelorException {
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
            && budgetLevel.getParentBudgetLevel().getParentBudgetLevel() != null) {
          Company company = budgetLevel.getParentBudgetLevel().getParentBudgetLevel().getCompany();
          checkBudgetKey = budgetBudgetService.checkBudgetKeyInConfig(company);
        }
        for (Budget budget : budgetLevel.getBudgetList()) {
          budgetBudgetService.validateBudget(budget, checkBudgetKey);
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
          budgetBudgetService.draftBudget(budget);
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

    if (!appAccountService.isApp("budget")
        || !appAccountService.getAppBudget().getCheckAvailableBudget()) {
      return null;
    }

    if (budget != null
        && budget.getBudgetLevel() != null
        && budget.getBudgetLevel().getParentBudgetLevel() != null
        && budget.getBudgetLevel().getParentBudgetLevel().getParentBudgetLevel() != null
        && budget
                .getBudgetLevel()
                .getParentBudgetLevel()
                .getParentBudgetLevel()
                .getCheckAvailableSelect()
            != null
        && budget
                .getBudgetLevel()
                .getParentBudgetLevel()
                .getParentBudgetLevel()
                .getCheckAvailableSelect()
            != 0) {
      return budget
          .getBudgetLevel()
          .getParentBudgetLevel()
          .getParentBudgetLevel()
          .getCheckAvailableSelect();
    } else {
      return appAccountService.getAppBudget().getCheckAvailableBudget()
          ? BudgetLevelRepository.BUDGET_LEVEL_AVAILABLE_AMOUNT_BUDGET_LINE
          : null;
    }
  }

  @Override
  @Transactional
  public void computeChildrenKey(BudgetLevel section) throws AxelorException {
    if (!CollectionUtils.isEmpty(section.getBudgetList())) {
      for (Budget budget : section.getBudgetList()) {
        budgetBudgetService.createBudgetKey(budget);
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
          budgetBudgetService.checkDatesOnBudget(budget);
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
                I18n.get(
                    com.axelor.apps.budget.exception.IExceptionMessage.WRONG_DATES_ON_BUDGET_LEVEL),
                budgetLevel.getCode()));
      }
    }
  }
}
