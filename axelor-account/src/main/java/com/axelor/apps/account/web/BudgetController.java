/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2019 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or  modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.axelor.apps.account.web;

import com.axelor.apps.account.db.Budget;
import com.axelor.apps.account.db.BudgetLine;
import com.axelor.apps.account.db.repo.BudgetRepository;
import com.axelor.apps.account.service.BudgetService;
import com.axelor.exception.service.TraceBackService;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Singleton;
import java.util.List;

@Singleton
public class BudgetController {

  public void compute(ActionRequest request, ActionResponse response) {
    try {
      Budget budget = request.getContext().asType(Budget.class);
      response.setValue("totalAmountExpected", Beans.get(BudgetService.class).compute(budget));
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void updateLines(ActionRequest request, ActionResponse response) {
    try {
      Budget budget = request.getContext().asType(Budget.class);
      budget = Beans.get(BudgetRepository.class).find(budget.getId());
      List<BudgetLine> budgetLineList = Beans.get(BudgetService.class).updateLines(budget);
      response.setValue("budgetLineList", budgetLineList);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void generatePeriods(ActionRequest request, ActionResponse response) {
    try {
      Budget budget = request.getContext().asType(Budget.class);
      response.setValue("budgetLineList", Beans.get(BudgetService.class).generatePeriods(budget));
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void checkSharedDates(ActionRequest request, ActionResponse response) {
    try {
      Budget budget = request.getContext().asType(Budget.class);
      Beans.get(BudgetService.class).checkSharedDates(budget);
    } catch (Exception e) {
      response.setError(e.getMessage());
    }
  }

  public void validate(ActionRequest request, ActionResponse response) {
    try {
      Budget budget = request.getContext().asType(Budget.class);
      budget = Beans.get(BudgetRepository.class).find(budget.getId());
      Beans.get(BudgetService.class).validate(budget);
      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void draft(ActionRequest request, ActionResponse response) {
    try {
      Budget budget = request.getContext().asType(Budget.class);
      budget = Beans.get(BudgetRepository.class).find(budget.getId());
      Beans.get(BudgetService.class).draft(budget);
      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void computeTotalAmountRealized(ActionRequest request, ActionResponse response) {
    try {
      Budget budget = request.getContext().asType(Budget.class);
      budget = Beans.get(BudgetRepository.class).find(budget.getId());
      response.setValue(
          "totalAmountRealized", Beans.get(BudgetService.class).computeTotalAmountRealized(budget));
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
