package com.axelor.apps.budget.web;

import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.budget.db.Budget;
import com.axelor.apps.budget.db.BudgetDistribution;
import com.axelor.apps.budget.db.GlobalBudget;
import com.axelor.apps.budget.db.repo.GlobalBudgetRepository;
import com.axelor.apps.budget.service.GlobalBudgetService;
import com.axelor.apps.purchase.db.repo.PurchaseOrderRepository;
import com.axelor.apps.sale.db.repo.SaleOrderRepository;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.common.base.Joiner;
import java.util.List;

public class GlobalBudgetViewController {

  public void viewGlobalPurchaseOrderLine(ActionRequest request, ActionResponse response) {
    GlobalBudget globalBudget = request.getContext().asType(GlobalBudget.class);
    List<Long> budgetIdList = Beans.get(GlobalBudgetService.class).getAllBudgetIds(globalBudget);

    String domain =
        String.format(
            "self.purchaseOrderLine.purchaseOrder.statusSelect in (%d,%d,%d) and self.budget.id IN (%s)",
            PurchaseOrderRepository.STATUS_REQUESTED,
            PurchaseOrderRepository.STATUS_VALIDATED,
            PurchaseOrderRepository.STATUS_FINISHED,
            Joiner.on(",").join(budgetIdList));

    response.setView(
        ActionView.define(I18n.get("Committed lines"))
            .model(BudgetDistribution.class.getName())
            .add("grid", "budget-budget-distribution-purchase-order-line-global-dashlet-grid")
            .add("form", "budget-budget-distribution-purchase-order-line-dashlet-form")
            .domain(domain)
            .context("_globalId", globalBudget.getId())
            .map());
  }

  public void viewGlobalSaleOrderLine(ActionRequest request, ActionResponse response) {
    GlobalBudget globalBudget = request.getContext().asType(GlobalBudget.class);
    List<Long> budgetIdList = Beans.get(GlobalBudgetService.class).getAllBudgetIds(globalBudget);

    String domain =
        String.format(
            "self.saleOrderLine.saleOrder.statusSelect in (%d,%d) and self.budget.id IN (%s)",
            SaleOrderRepository.STATUS_ORDER_CONFIRMED,
            SaleOrderRepository.STATUS_ORDER_COMPLETED,
            Joiner.on(",").join(budgetIdList));

    response.setView(
        ActionView.define(I18n.get("Committed lines"))
            .model(BudgetDistribution.class.getName())
            .add("grid", "budget-budget-distribution-sale-order-line-global-dashlet-grid")
            .add("form", "sale-order-budget-distribution-form")
            .domain(domain)
            .context("_globalId", globalBudget.getId())
            .map());
  }

  public void viewBudgetLines(ActionRequest request, ActionResponse response) {
    GlobalBudget globalBudget = request.getContext().asType(GlobalBudget.class);
    List<Long> budgetIdList = Beans.get(GlobalBudgetService.class).getAllBudgetIds(globalBudget);

    String domain = String.format("self.id IN (%s)", Joiner.on(",").join(budgetIdList));

    response.setView(
        ActionView.define(I18n.get("Lines"))
            .model(Budget.class.getName())
            .add("grid", "budget-lines-grid")
            .add("form", "budget-included-form")
            .param("details-view", "true")
            .param("showArchived", "true")
            .domain(domain)
            .context("_globalId", globalBudget.getId())
            .context(
                "_isReadOnly",
                globalBudget.getStatusSelect()
                    != GlobalBudgetRepository.GLOBAL_BUDGET_STATUS_SELECT_DRAFT)
            .context("_typeSelect", "budget")
            .map());
  }

  public void viewBudgetDistribution(ActionRequest request, ActionResponse response) {
    GlobalBudget globalBudget = request.getContext().asType(GlobalBudget.class);
    List<Long> budgetIdList = Beans.get(GlobalBudgetService.class).getAllBudgetIds(globalBudget);

    String domain =
        String.format(
            "self.budget.id IN (%s) AND self.purchase", Joiner.on(",").join(budgetIdList));

    response.setView(
        ActionView.define(I18n.get("Distribution lines"))
            .model(BudgetDistribution.class.getName())
            .add("grid", "budget-distribution-line-grid")
            .add("form", "budget-distribution-line-form")
            .param("show-toolbar", "true")
            .param("show-confirm", "true")
            .domain(domain)
            .context("_globalBudgetId", globalBudget.getId())
            .map());
  }

  public void viewSimulatedMove(ActionRequest request, ActionResponse response) {
    GlobalBudget globalBudget = request.getContext().asType(GlobalBudget.class);
    List<Long> budgetIdList = Beans.get(GlobalBudgetService.class).getAllBudgetIds(globalBudget);

    String domain =
        String.format(
            "self.moveLine.move.statusSelect = %d AND self.budget.id IN (%s)",
            MoveRepository.STATUS_SIMULATED, Joiner.on(",").join(budgetIdList));

    response.setView(
        ActionView.define(I18n.get("Simulated Moves"))
            .model(BudgetDistribution.class.getName())
            .add("grid", "budget-distribution-simulated-moves")
            .add("form", "budget-distribution-line-form")
            .param("show-toolbar", "true")
            .param("show-confirm", "true")
            .domain(domain)
            .context("_globalBudgetId", globalBudget.getId())
            .map());
  }

  public void viewRealizedWithPo(ActionRequest request, ActionResponse response) {
    GlobalBudget globalBudget = request.getContext().asType(GlobalBudget.class);
    List<Long> budgetIdList = Beans.get(GlobalBudgetService.class).getAllBudgetIds(globalBudget);

    String domain =
        String.format(
            "(self.invoiceLine.invoice.purchaseOrder is not null OR self.invoiceLine.invoice.saleOrder is not null) "
                + "AND self.invoiceLine.invoice.statusSelect = %d AND self.budget.id IN (%s)",
            InvoiceRepository.STATUS_VENTILATED, Joiner.on(",").join(budgetIdList));

    response.setView(
        ActionView.define(I18n.get("Display realized with po"))
            .model(BudgetDistribution.class.getName())
            .add("grid", "budget-distribution-realized-with-po-line-grid")
            .add("form", "budget-distribution-line-form")
            .param("show-toolbar", "true")
            .param("show-confirm", "true")
            .domain(domain)
            .context("_globalBudgetId", globalBudget.getId())
            .map());
  }

  public void viewRealizedWithoutPo(ActionRequest request, ActionResponse response) {
    GlobalBudget globalBudget = request.getContext().asType(GlobalBudget.class);
    List<Long> budgetIdList = Beans.get(GlobalBudgetService.class).getAllBudgetIds(globalBudget);

    String domain =
        String.format(
            "((self.invoiceLine.invoice.purchaseOrder is null AND self.invoiceLine.invoice.saleOrder is null AND self.invoiceLine.invoice.statusSelect = %d) "
                + "OR (self.moveLine IS NOT NULL AND self.moveLine.move.statusSelect in (%d,%d) AND self.moveLine.move.invoice IS NULL)) "
                + "AND self.budget.id IN (%s)",
            InvoiceRepository.STATUS_VENTILATED,
            MoveRepository.STATUS_DAYBOOK,
            MoveRepository.STATUS_ACCOUNTED,
            Joiner.on(",").join(budgetIdList));

    response.setView(
        ActionView.define(I18n.get("Display realized with no po"))
            .model(BudgetDistribution.class.getName())
            .add("grid", "budget-distribution-realized-without-po-line-grid")
            .add("form", "budget-distribution-line-form")
            .param("show-toolbar", "true")
            .param("show-confirm", "true")
            .domain(domain)
            .context("_globalBudgetId", globalBudget.getId())
            .map());
  }
}
