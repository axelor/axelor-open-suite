package com.axelor.apps.purchase.service;

import com.axelor.apps.purchase.db.PurchaseOrder;
import com.axelor.apps.purchase.db.repo.PurchaseOrderRepository;
import java.util.Objects;

public class PurchaseOrderTypeSelectServiceImpl implements PurchaseOrderTypeSelectService {
  @Override
  public void setTypeSelect(PurchaseOrder purchaseOrder) {
    Objects.requireNonNull(purchaseOrder);

    purchaseOrder.setTypeSelect(PurchaseOrderRepository.TYPE_STANDARD);
  }
}
