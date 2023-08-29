package com.axelor.apps.budget.service;

import com.axelor.apps.budget.db.Budget;
import com.axelor.apps.budget.db.BudgetVersion;
import com.axelor.apps.budget.db.GlobalBudget;
import com.axelor.apps.budget.db.VersionExpectedAmountsLine;
import com.axelor.apps.budget.db.repo.BudgetVersionRepository;
import com.axelor.apps.budget.db.repo.VersionExpectedAmountsLineRepository;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.util.List;

public class BudgetVersionServiceImpl implements BudgetVersionService {

  protected BudgetVersionRepository budgetVersionRepository;
  protected VersionExpectedAmountsLineRepository versionExpectedAmountsLineRepository;

  @Inject
  public BudgetVersionServiceImpl(
      BudgetVersionRepository budgetVersionRepository,
      VersionExpectedAmountsLineRepository versionExpectedAmountsLineRepository) {
    this.budgetVersionRepository = budgetVersionRepository;
    this.versionExpectedAmountsLineRepository = versionExpectedAmountsLineRepository;
  }

  @Override
  @Transactional(rollbackOn = {RuntimeException.class})
  public BudgetVersion createNewVersion(GlobalBudget globalBudget, String name) {
    BudgetVersion budgetVersion = new BudgetVersion();
    budgetVersion.setGlobalBudget(globalBudget);
    budgetVersion.setName(name);
    budgetVersion.setCode(globalBudget.getCode());
    budgetVersion.setIsActive(false);

    List<Budget> budgets = globalBudget.getBudgetList();
    BigDecimal globalExpectedAmount = BigDecimal.ZERO;

    for (Budget budget : budgets) {
      VersionExpectedAmountsLine versionExpectedAmountsLine = new VersionExpectedAmountsLine();
      versionExpectedAmountsLine.setBudget(budget);
      versionExpectedAmountsLine.setExpectedAmount(budget.getTotalAmountExpected());
      globalExpectedAmount = globalExpectedAmount.add(budget.getTotalAmountExpected());
      budgetVersion.addVersionExpectedAmountsLineListItem(versionExpectedAmountsLine);
    }

    globalBudget.addBudgetVersionListItem(budgetVersion);

    budgetVersion = budgetVersionRepository.save(budgetVersion);

    return budgetVersion;
  }
}
