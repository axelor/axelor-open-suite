/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2019 Axelor (<http://axelor.com>).
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

import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.Unit;
import com.axelor.apps.base.db.repo.PriceListLineRepository;
import com.axelor.apps.base.service.ShippingCoefService;
import com.axelor.apps.base.service.UnitConversionService;
import com.axelor.apps.purchase.db.PurchaseOrderLine;
import com.axelor.apps.purchase.db.SupplierCatalog;
import com.axelor.apps.purchase.db.repo.PurchaseOrderLineRepository;
import com.axelor.apps.purchase.db.repo.PurchaseOrderRepository;
import com.axelor.exception.AxelorException;
import com.axelor.inject.Beans;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

public class PurchaseProductServiceImpl implements PurchaseProductService {

  @Override
  public Map<String, Object> getDiscountsFromCatalog(
      SupplierCatalog supplierCatalog, BigDecimal price) {
    Map<String, Object> discounts = new HashMap<>();

    if (supplierCatalog.getPrice().compareTo(price) != 0) {
      discounts.put("discountAmount", price.subtract(supplierCatalog.getPrice()));
      discounts.put("discountTypeSelect", 2);
    } else {
      discounts.put("discountTypeSelect", PriceListLineRepository.AMOUNT_TYPE_NONE);
      discounts.put("discountAmount", BigDecimal.ZERO);
    }

    return discounts;
  }

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
            .order("-purchaseOrder.validationDate")
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
