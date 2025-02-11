package com.axelor.apps.budget.service.move;

import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.repo.MoveLineRepository;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.budget.db.Budget;
import com.axelor.apps.budget.db.BudgetDistribution;
import com.axelor.apps.budget.db.BudgetLine;
import com.axelor.apps.budget.db.repo.BudgetRepository;
import com.axelor.apps.budget.service.AppBudgetService;
import com.axelor.apps.budget.service.BudgetLineService;
import com.axelor.apps.budget.service.BudgetService;
import com.axelor.common.ObjectUtils;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class MoveBudgetDistributionServiceImpl implements MoveBudgetDistributionService {

  protected AppBudgetService appBudgetService;
  protected BudgetService budgetService;
  protected BudgetLineService budgetLineService;
  protected MoveLineRepository moveLineRepository;
  protected BudgetRepository budgetRepository;

  @Inject
  public MoveBudgetDistributionServiceImpl(
      AppBudgetService appBudgetService,
      BudgetService budgetService,
      BudgetLineService budgetLineService,
      MoveLineRepository moveLineRepository,
      BudgetRepository budgetRepository) {
    this.appBudgetService = appBudgetService;
    this.budgetService = budgetService;
    this.budgetLineService = budgetLineService;
    this.moveLineRepository = moveLineRepository;
    this.budgetRepository = budgetRepository;
  }

  @Override
  public void checkChanges(MoveLine moveLine) {
    if (!appBudgetService.isApp("budget")
        || MoveRepository.STATUS_DAYBOOK
            != Optional.ofNullable(moveLine)
                .map(MoveLine::getMove)
                .map(Move::getStatusSelect)
                .orElse(MoveRepository.STATUS_NEW)
        || moveLine.getId() == null) {
      return;
    }

    MoveLine savedMoveLine = moveLineRepository.find(moveLine.getId());
    Map<Budget, BigDecimal> budgetChangesMap = getBudgetChanges(moveLine, savedMoveLine);

    updateChangedBudgetDistribution(budgetChangesMap, moveLine);
  }

  protected Map<Budget, BigDecimal> getBudgetChanges(MoveLine moveLine, MoveLine savedMoveLine) {
    Map<Budget, BigDecimal> budgetChangesMap = new HashMap<>();
    List<BudgetDistribution> budgetDistributionList = moveLine.getBudgetDistributionList();
    List<BudgetDistribution> savedBudgetDistributionList =
        savedMoveLine.getBudgetDistributionList();

    fillBudgetChangesMap(budgetChangesMap, budgetDistributionList, savedBudgetDistributionList);
    return budgetChangesMap;
  }

  protected void fillBudgetChangesMap(
      Map<Budget, BigDecimal> map,
      List<BudgetDistribution> budgetDistributionList,
      List<BudgetDistribution> savedBudgetDistributionList) {
    if (ObjectUtils.notEmpty(budgetDistributionList)) {
      for (BudgetDistribution budgetDistribution : budgetDistributionList) {
        Budget budget = budgetDistribution.getBudget();
        if (map.containsKey(budget)) {
          map.replace(budget, map.get(budget).add(budgetDistribution.getAmount()));
        } else {
          map.put(budget, budgetDistribution.getAmount());
        }
      }
    }
    if (ObjectUtils.notEmpty(savedBudgetDistributionList)) {
      for (BudgetDistribution budgetDistribution : savedBudgetDistributionList) {
        Budget budget = budgetDistribution.getBudget();
        if (map.containsKey(budget)) {
          BigDecimal newValue = map.get(budget).subtract(budgetDistribution.getAmount());
          if (newValue.signum() == 0) {
            map.remove(budget);
          } else {
            map.replace(budget, newValue);
          }
        } else {
          if (budgetDistribution.getAmount().signum() != 0) {
            map.put(budget, budgetDistribution.getAmount().negate());
          }
        }
      }
    }
  }

  @Transactional(rollbackOn = {Exception.class})
  protected void updateChangedBudgetDistribution(
      Map<Budget, BigDecimal> budgetChangesMap, MoveLine moveLine) {
    if (ObjectUtils.isEmpty(budgetChangesMap)) {
      return;
    }

    Budget budget = null;
    BigDecimal amount = BigDecimal.ZERO;
    for (Map.Entry<Budget, BigDecimal> entry : budgetChangesMap.entrySet()) {
      budget = entry.getKey();
      amount = entry.getValue();

      updateAmountsOnBudget(budget, amount, moveLine);
    }
  }

  protected void updateAmountsOnBudget(Budget budget, BigDecimal amount, MoveLine moveLine) {
    if (moveLine == null || budget == null || amount.signum() == 0 || moveLine.getMove() == null) {
      return;
    }

    LocalDate date = moveLine.getMove().getDate();
    Optional<BudgetLine> optBudgetLine =
        budgetLineService.findBudgetLineAtDate(budget.getBudgetLineList(), date);

    optBudgetLine.ifPresent(
        budgetLine -> budgetService.updateBudgetLineAmounts(budgetLine, budget, amount));

    budgetService.computeTotalAmountRealized(budget);
    budgetService.computeTotalFirmGap(budget);
    budgetService.computeTotalSimulatedAmount(moveLine.getMove(), budget, false);
    budgetService.computeTotalAvailableWithSimulatedAmount(budget);
    budgetRepository.save(budget);
  }
}
