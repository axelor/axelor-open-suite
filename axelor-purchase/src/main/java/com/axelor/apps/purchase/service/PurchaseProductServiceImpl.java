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

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.Unit;
import com.axelor.apps.base.service.ShippingCoefService;
import com.axelor.apps.base.service.UnitConversionService;
import com.axelor.apps.purchase.db.PurchaseOrderLine;
import com.axelor.apps.purchase.db.repo.PurchaseOrderLineRepository;
import com.axelor.apps.purchase.db.repo.PurchaseOrderRepository;
import com.axelor.inject.Beans;
import java.math.BigDecimal;

public class PurchaseProductServiceImpl implements PurchaseProductService {

  @Override
  public BigDecimal getLastShippingCoef(Product product) throws AxelorException {
    PurchaseOrderLine lastPurchaseOrderLine =
        Beans.get(PurchaseOrderLineRepository.class)
            .all()
            .filter(
                "self.product.id = :productId "
                    + "AND (self.purchaseOrder.statusSelect = :validated "
                    + "OR self.purchaseOrder.statusSelect = :finished)")
            .bind("productId", product.getId())
            .bind("validated", PurchaseOrderRepository.STATUS_VALIDATED)
            .bind("finished", PurchaseOrderRepository.STATUS_FINISHED)
            .order("-purchaseOrder.validationDateTime")
            .fetchOne();
    if (lastPurchaseOrderLine != null) {
      Partner partner = lastPurchaseOrderLine.getPurchaseOrder().getSupplierPartner();
      Company company = lastPurchaseOrderLine.getPurchaseOrder().getCompany();
      Unit productUnit =
          Beans.get(PurchaseOrderLineService.class).getPurchaseUnit(lastPurchaseOrderLine);
      BigDecimal qty =
          Beans.get(UnitConversionService.class)
              .convert(
                  lastPurchaseOrderLine.getUnit(),
                  productUnit,
                  lastPurchaseOrderLine.getQty(),
                  lastPurchaseOrderLine.getQty().scale(),
                  product);
      return Beans.get(ShippingCoefService.class).getShippingCoef(product, partner, company, qty);
    } else {
      return BigDecimal.ONE;
    }
  }
}
