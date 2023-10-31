package com.axelor.apps.budget.service;

import com.axelor.apps.base.AxelorException;
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
import com.axelor.apps.budget.db.repo.BudgetGeneratorRepository;
import com.axelor.apps.budget.db.repo.BudgetLevelManagementRepository;
import com.axelor.apps.budget.db.repo.BudgetLevelRepository;
import com.axelor.apps.budget.db.repo.BudgetRepository;
import com.axelor.apps.budget.db.repo.BudgetVersionRepository;
import com.axelor.apps.budget.db.repo.GlobalBudgetRepository;
import com.axelor.auth.AuthUtils;
import com.axelor.common.ObjectUtils;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;

public class GlobalBudgetServiceImpl implements GlobalBudgetService {

  protected BudgetLevelService budgetLevelService;
  protected GlobalBudgetRepository globalBudgetRepository;
  protected BudgetService budgetService;
  protected BudgetRepository budgetRepository;
  protected BudgetLevelManagementRepository budgetLevelManagementRepository;
  protected BudgetVersionRepository budgetVersionRepo;
  protected BudgetScenarioService budgetScenarioService;
  protected BudgetLevelRepository budgetLevelRepository;
  protected BudgetScenarioLineService budgetScenarioLineService;
  protected BudgetGeneratorRepository budgetGeneratorRepository;
  protected AppBudgetService appBudgetService;
  protected BudgetToolsService budgetToolsService;

  @Inject
  public GlobalBudgetServiceImpl(
      BudgetLevelService budgetLevelService,
      GlobalBudgetRepository globalBudgetRepository,
      BudgetService budgetService,
      BudgetRepository budgetRepository,
      BudgetLevelManagementRepository budgetLevelManagementRepository,
      BudgetVersionRepository budgetVersionRepo,
      BudgetScenarioService budgetScenarioService,
      BudgetLevelRepository budgetLevelRepository,
      BudgetScenarioLineService budgetScenarioLineService,
      BudgetGeneratorRepository budgetGeneratorRepository,
      AppBudgetService appBudgetService,
      BudgetToolsService budgetToolsService) {
    this.budgetGeneratorRepository = budgetGeneratorRepository;
    this.budgetLevelService = budgetLevelService;
    this.globalBudgetRepository = globalBudgetRepository;
    this.budgetService = budgetService;
    this.budgetRepository = budgetRepository;
    this.budgetLevelManagementRepository = budgetLevelManagementRepository;
    this.budgetVersionRepo = budgetVersionRepo;
    this.budgetScenarioService = budgetScenarioService;
    this.budgetLevelRepository = budgetLevelRepository;
    this.budgetScenarioLineService = budgetScenarioLineService;
    this.appBudgetService = appBudgetService;
    this.budgetToolsService = budgetToolsService;
  }

  @Override
  public void validateDates(GlobalBudget globalBudget) throws AxelorException {
    if (!ObjectUtils.isEmpty(globalBudget.getBudgetLevelList())) {
      for (BudgetLevel budgetLevelChild : globalBudget.getBudgetLevelList()) {
        budgetLevelService.validateDates(budgetLevelChild);
      }
    } else if (!CollectionUtils.isEmpty(globalBudget.getBudgetList())) {
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
  public void generateBudgetKey(GlobalBudget globalBudget) throws AxelorException {
    List<Budget> budgetList = getAllBudgets(globalBudget);
    if (ObjectUtils.isEmpty(budgetList)) {
      return;
    }

    for (Budget budget : budgetList) {
      budgetService.createBudgetKey(budget);
      if (budget.getGlobalBudget() == null) {
        globalBudget.addBudgetListItem(budget);
      }
    }
  }

  @Override
  public List<Budget> getAllBudgets(GlobalBudget globalBudget) {
    List<Budget> budgetList = new ArrayList<>();
    if (!ObjectUtils.isEmpty(globalBudget.getBudgetList())) {
      budgetList = globalBudget.getBudgetList();
    }
    if (!ObjectUtils.isEmpty(globalBudget.getBudgetLevelList())) {
      for (BudgetLevel budgetLevel : globalBudget.getBudgetLevelList()) {
        budgetLevelService.getAllBudgets(budgetLevel, budgetList);
      }
    }

    return budgetList;
  }

  @Override
  public List<Long> getAllBudgetIds(GlobalBudget globalBudget) {
    List<Long> budgetIdList =
        getAllBudgets(globalBudget).stream().map(Budget::getId).collect(Collectors.toList());
    if (ObjectUtils.isEmpty(budgetIdList)) {
      budgetIdList.add(0L);
    }

    return budgetIdList;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public GlobalBudget changeBudgetVersion(GlobalBudget globalBudget, BudgetVersion budgetVersion)
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
              .get();
      if (versionExpectedAmountsLine != null) {
        budget.setActiveVersionExpectedAmountsLine(versionExpectedAmountsLine);
        budget.setAmountForGeneration(versionExpectedAmountsLine.getExpectedAmount());
        budget.setTotalAmountExpected(versionExpectedAmountsLine.getExpectedAmount());
        budget.setPeriodDurationSelect(0);
        budget.clearBudgetLineList();
        List<BudgetLine> budgetLineList = budgetService.generatePeriods(budget);
        if (!ObjectUtils.isEmpty(budgetLineList) && budgetLineList.size() == 1) {
          BudgetLine budgetLine = budgetLineList.get(0);
          recomputeImputedAmountsOnBudgetLine(budgetLine, budget);
        }
        computeBudgetLevelTotals(budget);
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
    BigDecimal firmGap =
        budgetLine
            .getAmountExpected()
            .subtract(budgetLine.getRealizedWithPo().add(budgetLine.getRealizedWithNoPo()));
    budgetLine.setFirmGap(firmGap.signum() >= 0 ? BigDecimal.ZERO : firmGap.abs());
    budgetLine.setAmountPaid(budget.getTotalAmountPaid());
  }

  @Override
  @Transactional
  public GlobalBudget generateGlobalBudget(BudgetGenerator budgetGenerator, Year year)
      throws AxelorException {

    GlobalBudget globalBudget = copyGlobalBudgetTemplate(budgetGenerator, year);

    fillGlobalBudgetWithLevels(budgetGenerator, globalBudget);

    computeTotals(globalBudget);

    globalBudgetRepository.save(globalBudget);
    return globalBudget;
  }

  @Override
  public List<BudgetScenarioLine> visualizeVariableAmounts(BudgetGenerator budgetGenerator) {
    if (budgetGenerator.getBudgetStructure() == null
        || budgetGenerator.getBudgetScenario() == null) {
      return new ArrayList<>();
    }
    BudgetStructure budgetStructure = budgetGenerator.getBudgetStructure();
    List<BudgetScenarioLine> budgetScenarioLineOriginList =
        budgetGenerator.getBudgetScenario().getBudgetScenarioLineList();
    List<BudgetScenarioLine> budgetScenarioLineList = new ArrayList<>();
    if (ObjectUtils.isEmpty(budgetStructure.getBudgetLevelList())) {
      return budgetScenarioLineList;
    }

    for (BudgetLevel groupBudgetLevel : budgetStructure.getBudgetLevelList()) {

      List<BudgetLevel> sectionBudgetLevelList = groupBudgetLevel.getBudgetLevelList();
      if (!ObjectUtils.isEmpty(sectionBudgetLevelList)) {
        for (BudgetLevel sectionBudgetLevel : sectionBudgetLevelList) {

          budgetScenarioLineList =
              budgetScenarioLineService.getLineUsingSection(
                  sectionBudgetLevel, budgetScenarioLineOriginList, budgetScenarioLineList);
        }
      }
    }

    return budgetScenarioLineList;
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
      BudgetGenerator budgetGenerator, GlobalBudget globalBudget) throws AxelorException {
    if (budgetGenerator.getBudgetStructure() == null) {
      return;
    }

    BudgetStructure budgetStructure = budgetGenerator.getBudgetStructure();

    Map<String, Object> variableAmountMap =
        budgetScenarioService.getVariableMap(budgetGenerator.getBudgetScenario(), 1);

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
  public void fillGlobalBudgetOnBudget(GlobalBudget globalBudget) {
    if (globalBudget == null || ObjectUtils.isEmpty(globalBudget.getBudgetLevelList())) {
      return;
    }

    List<Budget> budgetList = getAllBudgets(globalBudget);

    if (!ObjectUtils.isEmpty(budgetList)) {
      for (Budget budget : budgetList) {
        budget.setGlobalBudget(globalBudget);
      }
    }
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
  public Integer getBudgetControlLevel(Budget budget) {

    if (appBudgetService.getAppBudget() == null
        || !appBudgetService.getAppBudget().getCheckAvailableBudget()) {
      return null;
    }
    GlobalBudget globalBudget = budgetToolsService.getGlobalBudgetUsingBudget(budget);
    if (globalBudget != null && globalBudget.getCheckAvailableSelect() != 0) {
      return globalBudget.getCheckAvailableSelect();
    } else {
      return appBudgetService.getAppBudget().getCheckAvailableBudget()
          ? GlobalBudgetRepository.BUDGET_LEVEL_AVAILABLE_AMOUNT_BUDGET_LINE
          : null;
    }
  }

  @Override
  public List<BudgetLevel> getFilteredBudgetLevelList(GlobalBudget globalBudget) {
    return globalBudget.getBudgetLevelList().stream()
        .filter(
            budgetLevel ->
                ObjectUtils.isEmpty(budgetLevel.getValidatorSet())
                    || budgetLevel.getValidatorSet().contains(AuthUtils.getUser()))
        .collect(Collectors.toList());
  }

  @Override
  public List<BudgetLevel> getOtherUsersBudgetLevelList(GlobalBudget globalBudget) {
    return globalBudget.getBudgetLevelList().stream()
        .filter(
            budgetLevel ->
                !ObjectUtils.isEmpty(budgetLevel.getValidatorSet())
                    && !budgetLevel.getValidatorSet().contains(AuthUtils.getUser()))
        .collect(Collectors.toList());
  }
}
