package com.axelor.apps.businessproject.service;

import com.axelor.apps.account.db.Account;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.service.CurrencyScaleService;
import com.axelor.apps.budget.db.GlobalBudget;
import com.axelor.apps.budget.db.repo.BudgetDistributionRepository;
import com.axelor.apps.budget.db.repo.BudgetRepository;
import com.axelor.apps.budget.service.AppBudgetService;
import com.axelor.apps.budget.service.BudgetDistributionServiceImpl;
import com.axelor.apps.budget.service.BudgetLevelService;
import com.axelor.apps.budget.service.BudgetLineService;
import com.axelor.apps.budget.service.BudgetService;
import com.axelor.apps.budget.service.BudgetToolsService;
import com.axelor.apps.budget.service.compute.BudgetLineComputeService;
import com.axelor.common.ObjectUtils;
import com.axelor.studio.db.AppBudget;
import com.google.inject.Inject;
import java.time.LocalDate;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class BudgetDistributionBusinessProjectServiceImpl extends BudgetDistributionServiceImpl {

  @Inject
  public BudgetDistributionBusinessProjectServiceImpl(
      BudgetDistributionRepository budgetDistributionRepository,
      BudgetLineService budgetLineService,
      BudgetLevelService budgetLevelService,
      BudgetRepository budgetRepo,
      BudgetService budgetService,
      BudgetToolsService budgetToolsService,
      CurrencyScaleService currencyScaleService,
      AppBudgetService appBudgetService,
      BudgetLineComputeService budgetLineComputeService) {
    super(
        budgetDistributionRepository,
        budgetLineService,
        budgetLevelService,
        budgetRepo,
        budgetService,
        budgetToolsService,
        currencyScaleService,
        appBudgetService,
        budgetLineComputeService);
  }

  @Override
  public String getBudgetDomain(
      Company company,
      LocalDate date,
      String technicalTypeSelect,
      Account account,
      Set<GlobalBudget> globalBudgetSet)
      throws AxelorException {
    String query =
        super.getBudgetDomain(company, date, technicalTypeSelect, account, globalBudgetSet);

    String budget = "self.globalBudget";

    if (!ObjectUtils.isEmpty(globalBudgetSet)) {
      AppBudget appBudget = appBudgetService.getAppBudget();
      if (appBudget != null && appBudget.getEnableProject()) {
        query =
            query.concat(
                String.format(
                    " AND %s.id IN (%s)",
                    budget,
                    globalBudgetSet.stream()
                        .map(GlobalBudget::getId)
                        .map(Objects::toString)
                        .collect(Collectors.joining(","))));
      }
    }

    return query;
  }
}
