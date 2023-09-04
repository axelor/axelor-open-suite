package com.axelor.apps.budget.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.budget.db.Budget;
import com.axelor.apps.budget.db.BudgetLevel;
import com.axelor.apps.budget.db.GlobalBudget;
import com.axelor.apps.budget.db.GlobalBudgetTemplate;
import com.axelor.apps.budget.db.repo.BudgetLevelManagementRepository;
import com.axelor.apps.budget.db.repo.BudgetLevelRepository;
import com.axelor.apps.budget.db.repo.BudgetRepository;
import com.axelor.apps.budget.db.repo.GlobalBudgetRepository;
import com.axelor.auth.AuthUtils;
import com.axelor.common.ObjectUtils;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.util.List;

public class GlobalBudgetServiceImpl implements GlobalBudgetService {

  protected BudgetLevelService budgetLevelService;
  protected GlobalBudgetRepository globalBudgetRepository;
  protected BudgetService budgetService;
  protected BudgetRepository budgetRepository;
  protected BudgetLevelManagementRepository budgetLevelManagementRepository;

  @Inject
  public GlobalBudgetServiceImpl(
      BudgetLevelService budgetLevelService,
      GlobalBudgetRepository globalBudgetRepository,
      BudgetService budgetService,
      BudgetRepository budgetRepository,
      BudgetLevelManagementRepository budgetLevelManagementRepository) {
    this.budgetLevelService = budgetLevelService;
    this.globalBudgetRepository = globalBudgetRepository;
    this.budgetService = budgetService;
    this.budgetRepository = budgetRepository;
    this.budgetLevelManagementRepository = budgetLevelManagementRepository;
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
                    .compareTo(BigDecimal.ZERO)
                > 0
            ? totalAmountExpected.subtract(realizedWithPo).subtract(realizedWithNoPo)
            : BigDecimal.ZERO);
    globalBudget.setTotalFirmGap(totalFirmGap);
    globalBudget.setSimulatedAmount(simulatedAmount);
    globalBudget.setAvailableAmountWithSimulated(
        (globalBudget.getTotalAmountAvailable().subtract(simulatedAmount))
                    .compareTo(BigDecimal.ZERO)
                > 0
            ? (globalBudget.getTotalAmountAvailable().subtract(simulatedAmount))
            : BigDecimal.ZERO);
  }

  @Override
  public void validateChildren(GlobalBudget globalBudget) throws AxelorException {
    if (!ObjectUtils.isEmpty(globalBudget.getBudgetLevelList())) {
      for (BudgetLevel budgetLevel : globalBudget.getBudgetLevelList()) {
        budgetLevelService.validateChildren(budgetLevel);
      }
    }

    globalBudget.setStatusSelect(GlobalBudgetRepository.GLOBAL_BUDGET_STATUS_SELECT_VALID);
  }

  @Override
  public void archiveChildren(GlobalBudget globalBudget) throws AxelorException {
    if (!ObjectUtils.isEmpty(globalBudget.getBudgetLevelList())) {
      for (BudgetLevel budgetLevel : globalBudget.getBudgetLevelList()) {
        budgetLevelService.archiveBudgetLevel(budgetLevel);
      }
    }

    changeGlobalBudgetStatus(
        globalBudget, GlobalBudgetRepository.GLOBAL_BUDGET_STATUS_SELECT_ARCHIVED);
  }

  @Override
  public void draftChildren(GlobalBudget globalBudget) throws AxelorException {
    if (!ObjectUtils.isEmpty(globalBudget.getBudgetLevelList())) {
      for (BudgetLevel budgetLevel : globalBudget.getBudgetLevelList()) {
        budgetLevelService.draftChildren(budgetLevel);
      }
    }

    changeGlobalBudgetStatus(
        globalBudget, GlobalBudgetRepository.GLOBAL_BUDGET_STATUS_SELECT_DRAFT);
  }

  @Transactional(rollbackOn = {Exception.class})
  protected void changeGlobalBudgetStatus(GlobalBudget globalBudget, int status) {
    if (globalBudget != null) {
      globalBudget.setStatusSelect(status);
    }
  }

  @Override
  @Transactional(rollbackOn = {RuntimeException.class})
  public void generateBudgetKey(GlobalBudget globalBudget) throws AxelorException {
    if (ObjectUtils.isEmpty(globalBudget.getBudgetList())) {
      return;
    }

    for (Budget budget : globalBudget.getBudgetList()) {
      budgetService.createBudgetKey(budget);
      budgetRepository.save(budget);
    }
  }

  @Override
  @Transactional(rollbackOn = {RuntimeException.class})
  public GlobalBudget generateGlobalBudgetWithTemplate(GlobalBudgetTemplate globalBudgetTemplate) {
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
      GlobalBudgetTemplate globalBudgetTemplate, GlobalBudget globalBudget) {
    if (ObjectUtils.isEmpty(globalBudgetTemplate.getBudgetLevelList())) {
      return;
    }

    for (BudgetLevel groupBudgetLevel : globalBudgetTemplate.getBudgetLevelList()) {
      BudgetLevel optGroupBudgetLevel =
          budgetLevelManagementRepository.copy(groupBudgetLevel, false);
      optGroupBudgetLevel.setTypeSelect(BudgetLevelRepository.BUDGET_LEVEL_TYPE_SELECT_BUDGET);
      optGroupBudgetLevel.setSourceSelect(BudgetLevelRepository.BUDGET_LEVEL_SOURCE_AUTO);
      globalBudgetTemplate.removeBudgetLevelListItem(optGroupBudgetLevel);
      optGroupBudgetLevel.setGlobalBudgetTemplate(null);

      List<BudgetLevel> sectionBudgetLevelList = groupBudgetLevel.getBudgetLevelList();
      if (!ObjectUtils.isEmpty(sectionBudgetLevelList)) {
        for (BudgetLevel sectionBudgetLevel : sectionBudgetLevelList) {

          BudgetLevel optSectionBudgetLevel =
              budgetLevelManagementRepository.copy(sectionBudgetLevel, false);
          optSectionBudgetLevel.setTypeSelect(
              BudgetLevelRepository.BUDGET_LEVEL_TYPE_SELECT_BUDGET);
          optSectionBudgetLevel.setSourceSelect(BudgetLevelRepository.BUDGET_LEVEL_SOURCE_AUTO);
          List<Budget> budgetList = sectionBudgetLevel.getBudgetList();

          if (!ObjectUtils.isEmpty(budgetList)) {
            for (Budget budget : budgetList) {
              Budget optBudget = budgetRepository.copy(budget, true);
              optBudget.setTypeSelect(BudgetRepository.BUDGET_TYPE_SELECT_BUDGET);
              optBudget.setSourceSelect(BudgetRepository.BUDGET_SOURCE_AUTO);
              optSectionBudgetLevel.addBudgetListItem(optBudget);
              globalBudget.addBudgetListItem(optBudget);
            }
          }
          optGroupBudgetLevel.addBudgetLevelListItem(optSectionBudgetLevel);
        }
      }
      globalBudget.addBudgetLevelListItem(optGroupBudgetLevel);
    }
  }
}
