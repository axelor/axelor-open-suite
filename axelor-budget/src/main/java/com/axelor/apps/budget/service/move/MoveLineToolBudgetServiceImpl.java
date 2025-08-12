package com.axelor.apps.budget.service.move;

import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.budget.db.BudgetDistribution;
import com.axelor.apps.budget.db.repo.BudgetDistributionRepository;
import com.axelor.common.ObjectUtils;
import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.List;

public class MoveLineToolBudgetServiceImpl implements MoveLineToolBudgetService {

  protected BudgetDistributionRepository budgetDistributionRepository;

  @Inject
  public MoveLineToolBudgetServiceImpl(BudgetDistributionRepository budgetDistributionRepository) {
    this.budgetDistributionRepository = budgetDistributionRepository;
  }

  @Override
  public List<BudgetDistribution> copyBudgetDistributionList(MoveLine moveLine) {
    if (moveLine == null || ObjectUtils.isEmpty(moveLine.getBudgetDistributionList())) {
      return new ArrayList<>();
    }

    List<BudgetDistribution> copyBudgetDistributionList = new ArrayList<>();
    for (BudgetDistribution budgetDistribution : moveLine.getBudgetDistributionList()) {
      copyBudgetDistributionList.add(budgetDistributionRepository.copy(budgetDistribution, false));
    }

    return copyBudgetDistributionList;
  }
}
