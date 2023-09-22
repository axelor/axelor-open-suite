package com.axelor.apps.budget.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.budget.db.Budget;
import com.axelor.apps.budget.db.BudgetLevel;
import com.axelor.apps.budget.db.BudgetLine;
import com.axelor.apps.budget.db.BudgetScenarioVariable;
import com.axelor.apps.budget.db.BudgetVersion;
import com.axelor.apps.budget.db.GlobalBudget;
import com.axelor.apps.budget.db.GlobalBudgetTemplate;
import com.axelor.apps.budget.db.VersionExpectedAmountsLine;
import com.axelor.apps.budget.db.repo.BudgetLevelManagementRepository;
import com.axelor.apps.budget.db.repo.BudgetLevelRepository;
import com.axelor.apps.budget.db.repo.BudgetRepository;
import com.axelor.apps.budget.db.repo.BudgetVersionRepository;
import com.axelor.apps.budget.db.repo.GlobalBudgetRepository;
import com.axelor.auth.AuthUtils;
import com.axelor.common.ObjectUtils;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import org.apache.commons.collections.CollectionUtils;

import java.math.BigDecimal;
import java.util.Collection;
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

  @Inject
  public GlobalBudgetServiceImpl(
      BudgetLevelService budgetLevelService,
      GlobalBudgetRepository globalBudgetRepository,
      BudgetService budgetService,
      BudgetRepository budgetRepository,
      BudgetLevelManagementRepository budgetLevelManagementRepository,
      BudgetVersionRepository budgetVersionRepo,
      BudgetScenarioService budgetScenarioService) {
    this.budgetLevelService = budgetLevelService;
    this.globalBudgetRepository = globalBudgetRepository;
    this.budgetService = budgetService;
    this.budgetRepository = budgetRepository;
    this.budgetLevelManagementRepository = budgetLevelManagementRepository;
    this.budgetVersionRepo = budgetVersionRepo;
    this.budgetScenarioService = budgetScenarioService;
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
  @Transactional
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
  public GlobalBudget generateGlobalBudgetWithTemplate(GlobalBudgetTemplate globalBudgetTemplate)
      throws AxelorException {
    GlobalBudget globalBudget = copyGlobalBudgetTemplate(globalBudgetTemplate);
    fillGlobalBudgetWithLevels(globalBudgetTemplate, globalBudget);

    globalBudgetRepository.save(globalBudget);

    return globalBudget;
  }

  protected GlobalBudget copyGlobalBudgetTemplate(GlobalBudgetTemplate globalBudgetTemplate) {
    GlobalBudget globalBudget =
        new GlobalBudget(globalBudgetTemplate.getCode(), globalBudgetTemplate.getName());
    globalBudget.setFromDate(globalBudgetTemplate.getFromDate());
    globalBudget.setToDate(globalBudgetTemplate.getToDate());
    globalBudget.setCompany(globalBudgetTemplate.getCompany());
    globalBudget.setBudgetTypeSelect(globalBudgetTemplate.getBudgetTypeSelect());

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
      GlobalBudgetTemplate globalBudgetTemplate, GlobalBudget globalBudget) throws AxelorException {
    if (ObjectUtils.isEmpty(globalBudgetTemplate.getBudgetLevelList())) {
      return;
    }

    Map<String, Object> variableAmountMap =
        budgetScenarioService.getVariableMap(globalBudgetTemplate.getBudgetScenario(), 1);

    for (BudgetLevel groupBudgetLevel : globalBudgetTemplate.getBudgetLevelList()) {
      BudgetLevel optGroupBudgetLevel =
          budgetLevelManagementRepository.copy(groupBudgetLevel, false);
      optGroupBudgetLevel.setTypeSelect(BudgetLevelRepository.BUDGET_LEVEL_TYPE_SELECT_BUDGET);
      optGroupBudgetLevel.setSourceSelect(BudgetLevelRepository.BUDGET_LEVEL_SOURCE_AUTO);
      globalBudgetTemplate.removeBudgetLevelListItem(optGroupBudgetLevel);
      optGroupBudgetLevel.setGlobalBudgetTemplate(null);
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

          budgetService.generateBudgetsUsingTemplate(
              globalBudgetTemplate,
              budgetList,
              variablesList,
              optSectionBudgetLevel,
              globalBudget,
              variableAmountMap);
        }
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
              .orElse(null);
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

  @Transactional
  public void resetGlobalBudget(GlobalBudget globalBudget) {
    globalBudget.setCode(globalBudget.getCode() + " (" + I18n.get("copy") + ")");
    globalBudget.setStatusSelect(GlobalBudgetRepository.GLOBAL_BUDGET_STATUS_SELECT_DRAFT);
    globalBudget.setArchived(false);

    globalBudget.setTotalAmountCommitted(BigDecimal.ZERO);
    globalBudget.setTotalAmountAvailable(globalBudget.getTotalAmountExpected());
    globalBudget.setAvailableAmountWithSimulated(globalBudget.getTotalAmountExpected());
    globalBudget.setRealizedWithNoPo(BigDecimal.ZERO);
    globalBudget.setRealizedWithPo(BigDecimal.ZERO);
    globalBudget.setSimulatedAmount(BigDecimal.ZERO);
    globalBudget.setTotalFirmGap(BigDecimal.ZERO);
    globalBudget.setTotalAmountPaid(BigDecimal.ZERO);

    globalBudget.clearBudgetList();
    List<BudgetLevel> budgetLevels = globalBudget.getBudgetLevelList();

    if (ObjectUtils.notEmpty(budgetLevels)) {
      budgetLevels.forEach(child -> budgetLevelService.resetBudgetLevel(child));
    } else if (CollectionUtils.isNotEmpty(budgetLevels)) {
      globalBudget.getBudgetList().forEach(child -> budgetService.resetBudget(child));
    }
  }
  

}
