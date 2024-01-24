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
package com.axelor.apps.supplychain.db.repo;

import com.axelor.apps.base.db.repo.ProductRepository;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.db.repo.SaleOrderLineRepository;
import com.axelor.apps.sale.db.repo.SaleOrderRepository;
import com.axelor.apps.sale.service.SaleOrderLineSaleRepository;
import com.axelor.apps.supplychain.service.SaleOrderLineServiceSupplyChainImpl;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import java.math.BigDecimal;
import java.util.Map;

public class SaleOrderLineSupplychainRepository extends SaleOrderLineSaleRepository {

  @Override
  public Map<String, Object> populate(Map<String, Object> json, Map<String, Object> context) {
    Long saleOrderLineId = (Long) json.get("id");
    SaleOrderLine saleOrderLine = find(saleOrderLineId);

    SaleOrder saleOrder = saleOrderLine.getSaleOrder();

    if (this.availabilityIsNotManaged(saleOrderLine, saleOrder)) {
      return super.populate(json, context);
    }

    BigDecimal availableStock =
        Beans.get(SaleOrderLineServiceSupplyChainImpl.class)
            .getAvailableStock(saleOrder, saleOrderLine);
    BigDecimal allocatedStock =
        Beans.get(SaleOrderLineServiceSupplyChainImpl.class)
            .getAllocatedStock(saleOrder, saleOrderLine);

    BigDecimal availableQty = availableStock.add(allocatedStock);
    BigDecimal realQty = saleOrderLine.getQty();

    if (availableQty.compareTo(realQty) >= 0) {
      saleOrderLine.setAvailableStatus(I18n.get("Available"));
      saleOrderLine.setAvailableStatusSelect(SaleOrderLineRepository.STATUS_AVAILABLE);
    } else if (availableQty.compareTo(realQty) < 0) {
      saleOrderLine.setAvailableStatus(
          I18n.get("Missing") + " (" + availableQty.subtract(realQty) + ")");
      saleOrderLine.setAvailableStatusSelect(SaleOrderLineRepository.STATUS_MISSING);
    }
    json.put("availableStatus", saleOrderLine.getAvailableStatus());
    json.put("availableStatusSelect", saleOrderLine.getAvailableStatusSelect());

    return super.populate(json, context);
  }

  protected boolean availabilityIsNotManaged(SaleOrderLine saleOrderLine, SaleOrder saleOrder) {
    return saleOrder == null
        || saleOrderLine.getTypeSelect() != SaleOrderLineRepository.TYPE_NORMAL
        || saleOrder.getStatusSelect() != SaleOrderRepository.STATUS_ORDER_CONFIRMED
        || saleOrder.getStockLocation() == null
        || saleOrderLine.getDeliveryState() == SaleOrderLineRepository.DELIVERY_STATE_DELIVERED
        || (saleOrderLine.getProduct() != null
            && saleOrderLine
                .getProduct()
                .getProductTypeSelect()
                .equals(ProductRepository.PRODUCT_TYPE_SERVICE));
  }
}
