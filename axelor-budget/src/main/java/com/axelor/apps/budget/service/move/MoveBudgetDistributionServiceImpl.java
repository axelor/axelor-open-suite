/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2025 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.axelor.apps.budget.service.move;

import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.repo.MoveLineRepository;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.budget.db.Budget;
import com.axelor.apps.budget.db.BudgetDistribution;
import com.axelor.apps.budget.db.repo.BudgetRepository;
import com.axelor.apps.budget.service.AppBudgetService;
import com.axelor.apps.budget.service.BudgetService;
import com.axelor.apps.budget.service.compute.BudgetLineComputeService;
import com.axelor.common.ObjectUtils;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class MoveBudgetDistributionServiceImpl implements MoveBudgetDistributionService {

  protected AppBudgetService appBudgetService;
  protected BudgetService budgetService;
  protected MoveLineRepository moveLineRepository;
  protected BudgetRepository budgetRepository;
  protected BudgetLineComputeService budgetLineComputeService;
  protected MoveLineToolBudgetService moveLineToolBudgetService;

  @Inject
  public MoveBudgetDistributionServiceImpl(
      AppBudgetService appBudgetService,
      BudgetService budgetService,
      MoveLineRepository moveLineRepository,
      BudgetRepository budgetRepository,
      BudgetLineComputeService budgetLineComputeService,
      MoveLineToolBudgetService moveLineToolBudgetService) {
    this.appBudgetService = appBudgetService;
    this.budgetService = budgetService;
    this.moveLineRepository = moveLineRepository;
    this.budgetRepository = budgetRepository;
    this.budgetLineComputeService = budgetLineComputeService;
    this.moveLineToolBudgetService = moveLineToolBudgetService;
  }

  @Override
  public void checkChanges(MoveLine moveLine, boolean budgetAlreadyChanged) {
    if (!appBudgetService.isApp("budget")
        || MoveRepository.STATUS_DAYBOOK
            != Optional.ofNullable(moveLine)
                .map(MoveLine::getMove)
                .map(Move::getStatusSelect)
                .orElse(MoveRepository.STATUS_NEW)
        || moveLine.getId() == null) {
      return;
    }

    Map<Budget, BigDecimal> budgetChangesMap = getBudgetChanges(moveLine, budgetAlreadyChanged);

    updateChangedBudgetDistribution(budgetChangesMap, moveLine);
  }

  protected Map<Budget, BigDecimal> getBudgetChanges(
      MoveLine moveLine, boolean budgetAlreadyChanged) {
    Map<Budget, BigDecimal> budgetChangesMap = new HashMap<>();
    List<BudgetDistribution> budgetDistributionList = moveLine.getBudgetDistributionList();
    List<BudgetDistribution> savedBudgetDistributionList =
        getOldBudgetDistributionList(moveLine, budgetAlreadyChanged);

    fillBudgetChangesMap(budgetChangesMap, budgetDistributionList, savedBudgetDistributionList);
    return budgetChangesMap;
  }

  protected List<BudgetDistribution> getOldBudgetDistributionList(
      MoveLine moveLine, boolean budgetAlreadyChanged) {
    List<BudgetDistribution> savedBudgetDistributionList = moveLine.getOldBudgetDistributionList();
    if (ObjectUtils.isEmpty(savedBudgetDistributionList)
        && !budgetAlreadyChanged
        && moveLine.getId() != null) {
      MoveLine savedMoveLine = moveLineRepository.find(moveLine.getId());
      savedBudgetDistributionList =
          moveLineToolBudgetService.copyBudgetDistributionList(savedMoveLine);
    }

    return savedBudgetDistributionList;
  }

  protected void fillBudgetChangesMap(
      Map<Budget, BigDecimal> map,
      List<BudgetDistribution> budgetDistributionList,
      List<BudgetDistribution> savedBudgetDistributionList) {
    if (ObjectUtils.notEmpty(budgetDistributionList)) {
      for (BudgetDistribution budgetDistribution : budgetDistributionList) {
        fillMapWithBudgetDistribution(
            map, budgetDistribution.getBudget(), budgetDistribution.getAmount());
      }
    }
    if (ObjectUtils.notEmpty(savedBudgetDistributionList)) {
      for (BudgetDistribution budgetDistribution : savedBudgetDistributionList) {
        fillMapWithBudgetDistribution(
            map, budgetDistribution.getBudget(), budgetDistribution.getAmount().negate());
      }
    }
  }

  protected void fillMapWithBudgetDistribution(
      Map<Budget, BigDecimal> map, Budget budget, BigDecimal amount) {
    if (budget == null || amount.signum() == 0) {
      return;
    }

    if (map.containsKey(budget)) {
      BigDecimal newValue = map.get(budget).add(amount);
      if (newValue.signum() == 0) {
        map.remove(budget);
      } else {
        map.replace(budget, newValue);
      }
    } else {
      if (amount.signum() != 0) {
        map.put(budget, amount);
      }
    }
  }

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

  @Transactional(rollbackOn = {Exception.class})
  protected void updateAmountsOnBudget(Budget budget, BigDecimal amount, MoveLine moveLine) {
    if (moveLine == null || budget == null || amount.signum() == 0 || moveLine.getMove() == null) {
      return;
    }

    budgetLineComputeService.updateBudgetLineAmounts(
        moveLine.getMove(),
        budget,
        amount,
        moveLine.getBudgetFromDate(),
        moveLine.getBudgetToDate(),
        moveLine.getMove().getDate());

    budgetService.computeTotalAmountRealized(budget);
    budgetService.computeTotalFirmGap(budget);
    budgetService.computeTotalSimulatedAmount(moveLine.getMove(), budget, false);
    budgetService.computeTotalAvailableWithSimulatedAmount(budget);
    budgetRepository.save(budget);
  }
}
