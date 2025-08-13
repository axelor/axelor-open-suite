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
package com.axelor.apps.budget.web;

import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.service.exception.ErrorException;
import com.axelor.apps.budget.db.Budget;
import com.axelor.apps.budget.db.BudgetDistribution;
import com.axelor.apps.budget.db.GlobalBudget;
import com.axelor.apps.budget.db.repo.GlobalBudgetRepository;
import com.axelor.apps.budget.service.globalbudget.GlobalBudgetToolsService;
import com.axelor.apps.purchase.db.repo.PurchaseOrderRepository;
import com.axelor.apps.sale.db.repo.SaleOrderRepository;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import java.util.List;

public class GlobalBudgetViewController {

  @ErrorException
  public void viewGlobalPurchaseOrderLine(ActionRequest request, ActionResponse response)
      throws AxelorException {
    GlobalBudget globalBudget = request.getContext().asType(GlobalBudget.class);
    List<Long> budgetIdList =
        Beans.get(GlobalBudgetToolsService.class).getAllBudgetIds(globalBudget);

    String domain =
        String.format(
            "self.purchaseOrderLine.purchaseOrder.statusSelect in (%d,%d,%d) and self.budget.id IN (:budgetIdList)",
            PurchaseOrderRepository.STATUS_REQUESTED,
            PurchaseOrderRepository.STATUS_VALIDATED,
            PurchaseOrderRepository.STATUS_FINISHED);

    response.setView(
        ActionView.define(I18n.get("Committed lines"))
            .model(BudgetDistribution.class.getName())
            .add("grid", "budget-budget-distribution-purchase-order-line-global-dashlet-grid")
            .add("form", "budget-budget-distribution-purchase-order-line-dashlet-form")
            .domain(domain)
            .context("_globalId", globalBudget.getId())
            .context("budgetIdList", budgetIdList)
            .map());
  }

  @ErrorException
  public void viewGlobalSaleOrderLine(ActionRequest request, ActionResponse response)
      throws AxelorException {
    GlobalBudget globalBudget = request.getContext().asType(GlobalBudget.class);
    List<Long> budgetIdList =
        Beans.get(GlobalBudgetToolsService.class).getAllBudgetIds(globalBudget);

    String domain =
        String.format(
            "self.saleOrderLine.saleOrder.statusSelect in (%d,%d) and self.budget.id IN (:budgetIdList)",
            SaleOrderRepository.STATUS_ORDER_CONFIRMED, SaleOrderRepository.STATUS_ORDER_COMPLETED);

    response.setView(
        ActionView.define(I18n.get("Committed lines"))
            .model(BudgetDistribution.class.getName())
            .add("grid", "budget-budget-distribution-sale-order-line-global-dashlet-grid")
            .add("form", "sale-order-budget-distribution-form")
            .domain(domain)
            .context("_globalId", globalBudget.getId())
            .context("budgetIdList", budgetIdList)
            .map());
  }

  @ErrorException
  public void viewBudgetLines(ActionRequest request, ActionResponse response)
      throws AxelorException {
    GlobalBudget globalBudget = request.getContext().asType(GlobalBudget.class);
    List<Long> budgetIdList =
        Beans.get(GlobalBudgetToolsService.class).getAllBudgetIds(globalBudget);

    String domain = "self.id IN (:budgetIdList)";

    response.setView(
        ActionView.define(I18n.get("Lines"))
            .model(Budget.class.getName())
            .add("grid", "budget-grid")
            .add("form", "budget-form")
            .param("details-view", "true")
            .param("showArchived", "true")
            .domain(domain)
            .context("_globalId", globalBudget.getId())
            .context(
                "_isReadOnly",
                globalBudget.getStatusSelect()
                    != GlobalBudgetRepository.GLOBAL_BUDGET_STATUS_SELECT_DRAFT)
            .context("_typeSelect", "budget")
            .context("budgetIdList", budgetIdList)
            .map());
  }

  @ErrorException
  public void viewSimulatedMove(ActionRequest request, ActionResponse response)
      throws AxelorException {
    GlobalBudget globalBudget = request.getContext().asType(GlobalBudget.class);
    List<Long> budgetIdList =
        Beans.get(GlobalBudgetToolsService.class).getAllBudgetIds(globalBudget);

    String domain =
        String.format(
            "self.moveLine.move.statusSelect = %d AND self.budget.id IN (:budgetIdList)",
            MoveRepository.STATUS_SIMULATED);

    response.setView(
        ActionView.define(I18n.get("Simulated Moves"))
            .model(BudgetDistribution.class.getName())
            .add("grid", "budget-distribution-simulated-moves")
            .add("form", "budget-distribution-line-form")
            .param("show-toolbar", "true")
            .param("show-confirm", "true")
            .domain(domain)
            .context("_globalBudgetId", globalBudget.getId())
            .context("budgetIdList", budgetIdList)
            .map());
  }

  @ErrorException
  public void viewRealizedWithPo(ActionRequest request, ActionResponse response)
      throws AxelorException {
    GlobalBudget globalBudget = request.getContext().asType(GlobalBudget.class);
    List<Long> budgetIdList =
        Beans.get(GlobalBudgetToolsService.class).getAllBudgetIds(globalBudget);

    String domain =
        String.format(
            "self.moveLine IS NOT NULL AND self.moveLine.move.invoice IS NOT NULL AND self.moveLine.move.statusSelect in (%d,%d) AND "
                + "(self.moveLine.move.invoice.purchaseOrder IS NOT NULL OR self.moveLine.move.invoice.saleOrder IS NOT NULL) "
                + "AND self.budget.id IN (:budgetList)",
            MoveRepository.STATUS_DAYBOOK, MoveRepository.STATUS_ACCOUNTED);

    response.setView(
        ActionView.define(I18n.get("Display realized with po"))
            .model(BudgetDistribution.class.getName())
            .add("grid", "budget-distribution-realized-with-po-line-grid")
            .add("form", "budget-distribution-line-form")
            .param("show-toolbar", "true")
            .param("show-confirm", "true")
            .domain(domain)
            .context("_globalBudgetId", globalBudget.getId())
            .context("budgetIdList", budgetIdList)
            .map());
  }

  @ErrorException
  public void viewRealizedWithoutPo(ActionRequest request, ActionResponse response)
      throws AxelorException {
    GlobalBudget globalBudget = request.getContext().asType(GlobalBudget.class);
    List<Long> budgetIdList =
        Beans.get(GlobalBudgetToolsService.class).getAllBudgetIds(globalBudget);

    String domain =
        String.format(
            "self.moveLine IS NOT NULL AND self.moveLine.move.statusSelect in (%d,%d) AND "
                + "(self.moveLine.move.invoice IS NULL OR "
                + "(self.moveLine.move.invoice.purchaseOrder is null AND self.moveLine.move.invoice.saleOrder is null))"
                + "AND self.budget.id IN (:budgetIdList)",
            MoveRepository.STATUS_DAYBOOK, MoveRepository.STATUS_ACCOUNTED);

    response.setView(
        ActionView.define(I18n.get("Display realized with no po"))
            .model(BudgetDistribution.class.getName())
            .add("grid", "budget-distribution-realized-without-po-line-grid")
            .add("form", "budget-distribution-line-form")
            .param("show-toolbar", "true")
            .param("show-confirm", "true")
            .domain(domain)
            .context("_globalBudgetId", globalBudget.getId())
            .context("budgetIdList", budgetIdList)
            .map());
  }
}
