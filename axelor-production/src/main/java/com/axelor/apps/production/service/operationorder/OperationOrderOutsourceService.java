package com.axelor.apps.production.service.operationorder;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.production.db.ManufOrder;
import com.axelor.apps.production.db.OperationOrder;
import com.axelor.apps.purchase.db.PurchaseOrder;
import com.axelor.apps.purchase.db.PurchaseOrderLine;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface OperationOrderOutsourceService {

  Optional<Partner> getOutsourcePartner(OperationOrder operationOrder);

  List<PurchaseOrderLine> createPurchaseOrderLines(
      OperationOrder operationOrder, PurchaseOrder purchaseOrder) throws AxelorException;

  Optional<PurchaseOrderLine> createPurchaseOrderLine(
      OperationOrder operationOrder, PurchaseOrder purchaseOrder, Product product)
      throws AxelorException;

  Optional<PurchaseOrderLine> createPurchaseOrderLine(
      ManufOrder manufOrder, PurchaseOrder purchaseOrder, Product product) throws AxelorException;

  List<PurchaseOrderLine> createPurchaseOrderLines(
      ManufOrder manufOrder, Set<Product> productSet, PurchaseOrder purchaseOrder)
      throws AxelorException;
}
