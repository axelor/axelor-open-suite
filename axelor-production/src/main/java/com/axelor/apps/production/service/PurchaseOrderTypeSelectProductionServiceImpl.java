package com.axelor.apps.production.service;

import com.axelor.apps.purchase.db.PurchaseOrder;
import com.axelor.apps.purchase.db.repo.PurchaseOrderRepository;
import com.axelor.apps.purchase.service.PurchaseOrderTypeSelectServiceImpl;
import java.util.Objects;

public class PurchaseOrderTypeSelectProductionServiceImpl
    extends PurchaseOrderTypeSelectServiceImpl {

  @Override
  public void setTypeSelect(PurchaseOrder purchaseOrder) {
    Objects.requireNonNull(purchaseOrder);

    if (purchaseOrder.getSupplierPartner() != null
        && purchaseOrder.getSupplierPartner().getIsSubcontractor()) {
      purchaseOrder.setTypeSelect(PurchaseOrderRepository.TYPE_SUBCONTRACTING);
    } else {
      super.setTypeSelect(purchaseOrder);
    }
  }
}
