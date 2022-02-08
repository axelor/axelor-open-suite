/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2022 Axelor (<http://axelor.com>).
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
package com.axelor.apps.purchase.service;

import com.axelor.apps.base.db.Product;
import com.axelor.apps.purchase.db.PurchaseOrder;
import com.axelor.apps.purchase.db.PurchaseOrderLine;
import com.axelor.apps.purchase.db.repo.PurchaseOrderLineRepository;
import java.util.Map;

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
