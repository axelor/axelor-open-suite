package com.axelor.apps.budget.db.repo;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.budget.service.saleorder.SaleOrderLineBudgetService;
import com.axelor.apps.businessproject.db.repo.SaleOrderProjectRepository;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.inject.Beans;
import javax.persistence.PersistenceException;
import org.apache.commons.collections.CollectionUtils;

public class BudgetSaleOrderRepository extends SaleOrderProjectRepository {

  @Override
  public SaleOrder save(SaleOrder saleOrder) {
    try {
      if (!CollectionUtils.isEmpty(saleOrder.getSaleOrderLineList())) {
        SaleOrderLineBudgetService saleOrderBudgetService =
            Beans.get(SaleOrderLineBudgetService.class);
        for (SaleOrderLine saleOrderLine : saleOrder.getSaleOrderLineList()) {
          saleOrderBudgetService.checkAmountForSaleOrderLine(saleOrderLine);
        }
      }
    } catch (AxelorException e) {
      throw new PersistenceException(e.getLocalizedMessage());
    }

    super.save(saleOrder);
    return saleOrder;
  }
}
