package com.axelor.apps.budget.service.observer;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.budget.service.saleorder.SaleOrderBudgetService;
import com.axelor.apps.budget.service.saleorder.SaleOrderCheckBudgetService;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.service.event.SaleOrderConfirm;
import com.axelor.event.Observes;
import com.axelor.inject.Beans;
import org.apache.commons.collections.CollectionUtils;

public class SaleOrderBudgetObserver {

  public void budgetConfirmSaleOrder(@Observes SaleOrderConfirm event) throws AxelorException {
    SaleOrder saleOrder = event.getSaleOrder();
    SaleOrderBudgetService saleOrderBudgetService = Beans.get(SaleOrderBudgetService.class);

    if (saleOrder != null && !CollectionUtils.isEmpty(saleOrder.getSaleOrderLineList())) {
      saleOrderBudgetService.generateBudgetDistribution(saleOrder);
      saleOrderBudgetService.updateBudgetLinesFromSaleOrder(saleOrder);
    }

    Beans.get(SaleOrderCheckBudgetService.class).checkNoComputeBudgetError(saleOrder);
  }
}
