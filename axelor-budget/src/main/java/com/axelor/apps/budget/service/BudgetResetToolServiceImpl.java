/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2023 Axelor (<http://axelor.com>).
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
package com.axelor.apps.budget.service;

import com.axelor.apps.budget.db.Budget;
import com.axelor.apps.budget.db.BudgetLine;
import com.axelor.apps.budget.db.repo.BudgetRepository;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import org.apache.commons.collections.CollectionUtils;

public class BudgetResetToolServiceImpl implements BudgetResetToolService {

  protected BudgetLineResetToolService budgetLineResetToolService;
  protected BudgetRepository budgetRepository;

  @Inject
  public BudgetResetToolServiceImpl(
      BudgetLineResetToolService budgetLineResetToolService, BudgetRepository budgetRepository) {
    this.budgetLineResetToolService = budgetLineResetToolService;
    this.budgetRepository = budgetRepository;
  }

  @Override
  @Transactional
  public Budget resetBudget(Budget entity) {

    entity.setStatusSelect(BudgetRepository.STATUS_DRAFT);
    entity.setArchived(false);

    entity.setTotalAmountExpected(entity.getTotalAmountExpected());
    entity.setTotalAmountCommitted(BigDecimal.ZERO);
    entity.setRealizedWithNoPo(BigDecimal.ZERO);
    entity.setRealizedWithPo(BigDecimal.ZERO);
    entity.setSimulatedAmount(BigDecimal.ZERO);
    entity.setAvailableAmount(entity.getTotalAmountExpected());
    entity.setAvailableAmountWithSimulated(entity.getTotalAmountExpected());
    entity.setTotalAmountRealized(BigDecimal.ZERO);
    entity.setTotalFirmGap(BigDecimal.ZERO);
    entity.setTotalAmountPaid(BigDecimal.ZERO);

    if (!CollectionUtils.isEmpty(entity.getBudgetLineList())) {
      for (BudgetLine child : entity.getBudgetLineList()) {
        child = budgetLineResetToolService.resetBudgetLine(child);
      }
    }
    return budgetRepository.save(entity);
  }
}
