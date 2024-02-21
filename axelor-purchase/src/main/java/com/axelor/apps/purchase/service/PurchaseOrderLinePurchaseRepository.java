/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2024 Axelor (<http://axelor.com>).
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
package com.axelor.apps.purchase.service;

import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.purchase.db.PurchaseOrder;
import com.axelor.apps.purchase.db.PurchaseOrderLine;
import com.axelor.apps.purchase.db.repo.PurchaseOrderLineRepository;
import com.axelor.inject.Beans;
import java.util.Map;

public class PurchaseOrderLinePurchaseRepository extends PurchaseOrderLineRepository {

  @Override
  public Map<String, Object> populate(Map<String, Object> json, Map<String, Object> context) {
    json.put(
        "$nbDecimalDigitForUnitPrice",
        Beans.get(AppBaseService.class).getNbDecimalDigitForUnitPrice());
    json.put("$nbDecimalDigitForQty", Beans.get(AppBaseService.class).getNbDecimalDigitForQty());

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
