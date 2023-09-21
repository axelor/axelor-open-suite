package com.axelor.apps.budget.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Year;
import com.axelor.apps.base.service.app.AppBaseService;
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
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

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
      BudgetGeneratorRepository budgetGeneratorRepository) {
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
  }

  @Override
  public void validateDates(GlobalBudget globalBudget) throws AxelorException {
    if (!ObjectUtils.isEmpty(globalBudget.getBudgetLevelList())) {
      for (BudgetLevel budgetLevelChild : globalBudget.getBudgetLevelList()) {
        budgetLevelService.validateDates(budgetLevelChild);
      }
    }
  }

  @Override
  @Transactional(rollbackOn = {RuntimeException.class})
  public void computeBudgetLevelTotals(Budget budget) {

    budgetLevelService.computeBudgetLevelTotals(budget);

    GlobalBudget globalBudget = budget.getGlobalBudget();

    if (globalBudget == null) {
      globalBudget =
          Optional.ofNullable(budget.getBudgetLevel())
              .map(BudgetLevel::getParentBudgetLevel)
              .map(BudgetLevel::getGlobalBudget)
              .orElse(null);
    }

    if (globalBudget != null) {
      computeTotals(globalBudget);
      globalBudgetRepository.save(globalBudget);
    }
  }

  @Override
  public void computeTotals(GlobalBudget globalBudget) {
    List<BudgetLevel> budgetLevelList = globalBudget.getBudgetLevelList();
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
    globalBudget.setTotalAmountExpected(totalAmountExpected);
    globalBudget.setTotalAmountCommitted(totalAmountCommitted);
    globalBudget.setTotalAmountPaid(totalAmountPaid);
    globalBudget.setTotalAmountRealized(totalAmountRealized);
    globalBudget.setRealizedWithNoPo(realizedWithNoPo);
    globalBudget.setRealizedWithPo(realizedWithPo);
    globalBudget.setTotalAmountAvailable(
        (totalAmountExpected.subtract(realizedWithPo).subtract(realizedWithNoPo))
            .max(BigDecimal.ZERO));
    globalBudget.setTotalFirmGap(totalFirmGap);
    globalBudget.setSimulatedAmount(simulatedAmount);
    globalBudget.setAvailableAmountWithSimulated(
        (globalBudget.getTotalAmountAvailable().subtract(simulatedAmount)).max(BigDecimal.ZERO));
  }

  @Override
  public void generateBudgetKey(GlobalBudget globalBudget) throws AxelorException {
    List<Budget> budgetList = globalBudget.getBudgetList();
    if (ObjectUtils.isEmpty(budgetList)
        && !ObjectUtils.isEmpty(globalBudget.getBudgetLevelList())) {
      budgetList =
          globalBudget.getBudgetLevelList().stream()
              .filter(bl -> !ObjectUtils.isEmpty(bl.getBudgetLevelList()))
              .map(BudgetLevel::getBudgetLevelList)
              .flatMap(Collection::stream)
              .filter(bl -> !ObjectUtils.isEmpty(bl.getBudgetList()))
              .map(BudgetLevel::getBudgetList)
              .flatMap(Collection::stream)
              .collect(Collectors.toList());
    }
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
  @Transactional
  public GlobalBudget generateGlobalBudget(BudgetGenerator budgetGenerator) throws AxelorException {

    GlobalBudget globalBudget = copyGlobalBudgetTemplate(budgetGenerator);
  
    fillGlobalBudgetWithLevels(budgetGenerator, globalBudget);
    globalBudgetRepository.save(globalBudget);
    return null;
  }

  @Override
  @Transactional
  public List<BudgetScenarioLine> visualizeVariableAmounts(BudgetGenerator budgetGenerator)
      throws AxelorException {

	  budgetGenerator = budgetGeneratorRepository.find(budgetGenerator.getId());
    BudgetStructure budgetStructure = budgetGenerator.getBudgetStructure();
    Set<BudgetScenarioVariable> allVariables = new HashSet<>();

    for (BudgetLevel groupBudgetLevel : budgetStructure.getBudgetLevelList()) {

      List<BudgetLevel> sectionBudgetLevelList = groupBudgetLevel.getBudgetLevelList();
      if (!ObjectUtils.isEmpty(sectionBudgetLevelList)) {
        for (BudgetLevel sectionBudgetLevel : sectionBudgetLevelList) {
          allVariables.addAll(sectionBudgetLevel.getBudgetScenarioVariableSet());
        }
      }
    }

    List<BudgetScenarioLine> filtredBudgetScerionLines =
        budgetGenerator.getBudgetScenario().getBudgetScenarioLineList().stream()
            .filter(it -> allVariables.contains(it.getBudgetScenarioVariable()))
            .collect(Collectors.toList());
    
   return filtredBudgetScerionLines;
   
  }

  protected GlobalBudget copyGlobalBudgetTemplate(BudgetGenerator budgetGenerator) {
    GlobalBudget globalBudget =
        new GlobalBudget(budgetGenerator.getCode(), budgetGenerator.getName());
    globalBudget.setFromDate(budgetGenerator.getFromDate());
    globalBudget.setToDate(budgetGenerator.getToDate());
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
    if (ObjectUtils.isEmpty(budgetGenerator.getBudgetStructure().getBudgetLevelList())) {
      return;
    }

    BudgetStructure budgetStructure = budgetGenerator.getBudgetStructure();

    Map<String, Object> variableAmountMap =
        budgetScenarioService.getVariableMap(budgetGenerator.getBudgetScenario(), 1);

    for (BudgetLevel groupBudgetLevel : budgetStructure.getBudgetLevelList()) {
      BudgetLevel optGroupBudgetLevel =
          budgetLevelManagementRepository.copy(groupBudgetLevel, false);
      optGroupBudgetLevel.setTypeSelect(BudgetLevelRepository.BUDGET_LEVEL_TYPE_SELECT_BUDGET);
      optGroupBudgetLevel.setSourceSelect(BudgetLevelRepository.BUDGET_LEVEL_SOURCE_AUTO);
      budgetGenerator.getBudgetStructure().removeBudgetLevelListItem(optGroupBudgetLevel);
      optGroupBudgetLevel.setBudgetStructure(null);
      globalBudget.addBudgetLevelListItem(optGroupBudgetLevel);

      List<BudgetLevel> sectionBudgetLevelList = groupBudgetLevel.getBudgetLevelList();
      if (!ObjectUtils.isEmpty(sectionBudgetLevelList)) {
        for (BudgetLevel sectionBudgetLevel : sectionBudgetLevelList) {

          BudgetLevel optSectionBudgetLevel =
              budgetLevelManagementRepository.copy(sectionBudgetLevel, false);
          optSectionBudgetLevel.setTypeSelect(
              BudgetLevelRepository.BUDGET_LEVEL_TYPE_SELECT_BUDGET);
          optSectionBudgetLevel.setSourceSelect(BudgetLevelRepository.BUDGET_LEVEL_SOURCE_AUTO);
          optGroupBudgetLevel.addBudgetLevelListItem(optSectionBudgetLevel);
          List<Budget> budgetList = sectionBudgetLevel.getBudgetList();
          Set<BudgetScenarioVariable> variablesList =
              sectionBudgetLevel.getBudgetScenarioVariableSet();

          if (!budgetStructure.getIsScenario() && !ObjectUtils.isEmpty(budgetList)) {
            for (Budget budget : budgetList) {
              Budget optBudget = budgetRepository.copy(budget, true);
              optBudget.setTypeSelect(BudgetRepository.BUDGET_TYPE_SELECT_BUDGET);
              optBudget.setSourceSelect(BudgetRepository.BUDGET_SOURCE_AUTO);
              optBudget.setAmountForGeneration(budget.getTotalAmountExpected());
              optBudget.setAvailableAmount(budget.getTotalAmountExpected());
              optBudget.setAvailableAmountWithSimulated(budget.getTotalAmountExpected());
              budgetService.generatePeriods(optBudget);
              optSectionBudgetLevel.addBudgetListItem(optBudget);
              globalBudget.addBudgetListItem(optBudget);
              budgetRepository.save(optBudget);
            }
          } else if (budgetStructure.getIsScenario() && !ObjectUtils.isEmpty(variablesList)) {

            for (BudgetScenarioVariable budgetScenarioVariable : variablesList) {
              Budget optBudget = new Budget();
              optBudget.setCode(budgetScenarioVariable.getCode());
              optBudget.setName(budgetScenarioVariable.getName());
              optBudget.setFromDate(globalBudget.getFromDate());
              optBudget.setToDate(globalBudget.getToDate());
              optBudget.setStatusSelect(BudgetRepository.STATUS_DRAFT);
              optBudget.setTypeSelect(BudgetRepository.BUDGET_TYPE_SELECT_BUDGET);
              optBudget.setSourceSelect(BudgetRepository.BUDGET_SOURCE_AUTO);
              optBudget.setCategory(budgetScenarioVariable.getCategory());
              BigDecimal calculatedAmount =
                  ((BigDecimal)
                          variableAmountMap.getOrDefault(
                              budgetScenarioVariable.getCode(), BigDecimal.ZERO))
                      .setScale(AppBaseService.DEFAULT_NB_DECIMAL_DIGITS, RoundingMode.HALF_UP);
              optBudget.setAmountForGeneration(calculatedAmount);
              optBudget.setTotalAmountExpected(calculatedAmount);
              optBudget.setAvailableAmount(calculatedAmount);
              optBudget.setAvailableAmountWithSimulated(optBudget.getAmountForGeneration());
              optBudget.setPeriodDurationSelect(0);
              budgetService.generatePeriods(optBudget);
              optSectionBudgetLevel.addBudgetListItem(optBudget);
              globalBudget.addBudgetListItem(optBudget);
              budgetRepository.save(optBudget);
            }
          }
        }
      }
    }
  }
}
