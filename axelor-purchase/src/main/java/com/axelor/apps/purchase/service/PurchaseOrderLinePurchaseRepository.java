package com.axelor.apps.purchase.service;

import com.axelor.apps.base.db.Product;
import com.axelor.apps.purchase.db.PurchaseOrder;
import com.axelor.apps.purchase.db.PurchaseOrderLine;
import com.axelor.apps.purchase.db.repo.PurchaseOrderLineRepository;
import com.axelor.apps.purchase.module.PurchaseModule;
import java.util.Map;
import javax.annotation.Priority;
import javax.enterprise.inject.Alternative;

@Alternative
@Priority(PurchaseModule.PRIORITY)
public class PurchaseOrderLinePurchaseRepository extends PurchaseOrderLineRepository {

  @Override
  public Map<String, Object> populate(Map<String, Object> json, Map<String, Object> context) {
    if (context.get("_model") != null
        && context.get("_model").toString().contains("PurchaseOrder")
        && context.get("id") != null) {
      Long id = (Long) json.get("id");
      if (id != null) {
        PurchaseOrderLine purchaseOrderLine = find(id);
        PurchaseOrder purchaseOrder = purchaseOrderLine.getPurchaseOrder();
        Product product = purchaseOrderLine.getProduct();
        json.put(
            "$hasWarning",
            purchaseOrder != null
                && product != null
                && product.getDefaultSupplierPartner() != null
                && purchaseOrder.getSupplierPartner() != null
                && product.getDefaultSupplierPartner() != purchaseOrder.getSupplierPartner());
      }
    }
    return super.populate(json, context);
  }
}
