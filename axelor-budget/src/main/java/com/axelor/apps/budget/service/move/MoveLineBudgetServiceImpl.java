package com.axelor.apps.budget.service.move;

import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.repo.MoveLineRepository;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.budget.db.BudgetDistribution;
import com.axelor.apps.budget.exception.BudgetExceptionMessage;
import com.axelor.apps.budget.service.BudgetDistributionService;
import com.axelor.apps.budget.service.BudgetService;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import com.google.inject.servlet.RequestScoped;

@RequestScoped
public class MoveLineBudgetServiceImpl implements MoveLineBudgetService {

  protected MoveLineRepository moveLineRepository;
  protected BudgetService budgetService;
  protected BudgetDistributionService budgetDistributionService;

  @Inject
  public MoveLineBudgetServiceImpl(
      MoveLineRepository moveLineRepository,
      BudgetService budgetService,
      BudgetDistributionService budgetDistributionService) {
    this.moveLineRepository = moveLineRepository;
    this.budgetService = budgetService;
    this.budgetDistributionService = budgetDistributionService;
  }

  @Override
  @Transactional
  public String computeBudgetDistribution(MoveLine moveLine) {
    if (moveLine == null || moveLine.getMove() == null) {
      return "";
    }
    moveLine.clearBudgetDistributionList();
    String alertMessage =
        budgetDistributionService.createBudgetDistribution(
            moveLine.getAnalyticMoveLineList(),
            moveLine.getAccount(),
            moveLine.getMove().getCompany(),
            moveLine.getMove().getDate(),
            moveLine.getCredit().add(moveLine.getDebit()),
            moveLine.getName(),
            moveLine);
    return alertMessage;
  }

  @Override
  public void checkAmountForMoveLine(MoveLine moveLine) throws AxelorException {
    if (moveLine.getBudgetDistributionList() != null
        && !moveLine.getBudgetDistributionList().isEmpty()) {
      for (BudgetDistribution budgetDistribution : moveLine.getBudgetDistributionList()) {
        if (budgetDistribution.getAmount().compareTo(moveLine.getCredit().add(moveLine.getDebit()))
            > 0) {
          throw new AxelorException(
              TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
              I18n.get(BudgetExceptionMessage.BUDGET_DISTRIBUTION_LINE_SUM_GREATER_MOVE),
              budgetDistribution.getBudget().getCode(),
              moveLine.getAccount().getCode());
        }
      }
    }
  }
}
