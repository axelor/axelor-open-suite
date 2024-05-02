package com.axelor.apps.repo;

import com.axelor.apps.budget.db.repo.SaleOrderBudgetRepository;
import com.axelor.apps.sale.db.SaleOrder;

public class SaleOrderConstructionRepository extends SaleOrderBudgetRepository {

  @Override
  public SaleOrder save(SaleOrder saleOrder) {
    return super.save(saleOrder);
  }
}
