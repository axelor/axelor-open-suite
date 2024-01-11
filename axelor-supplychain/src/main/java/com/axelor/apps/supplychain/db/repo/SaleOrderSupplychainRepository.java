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

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.db.repo.SaleOrderManagementRepository;
import com.axelor.apps.supplychain.listener.InvoicingStateCache;
import com.axelor.apps.supplychain.service.AccountingSituationSupplychainService;
import com.axelor.inject.Beans;
import com.axelor.studio.app.service.AppService;
import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class SaleOrderSupplychainRepository extends SaleOrderManagementRepository {

  @Override
  public SaleOrder copy(SaleOrder entity, boolean deep) {

    SaleOrder copy = super.copy(entity, deep);

    if (!Beans.get(AppService.class).isApp("supplychain")) {
      return copy;
    }

    copy.setShipmentDate(null);
    copy.setDeliveryState(DELIVERY_STATE_NOT_DELIVERED);
    copy.setAmountInvoiced(null);
    copy.setStockMoveList(null);

    if (copy.getSaleOrderLineList() != null) {
      for (SaleOrderLine saleOrderLine : copy.getSaleOrderLineList()) {
        saleOrderLine.setDeliveryState(null);
        saleOrderLine.setDeliveredQty(null);
        saleOrderLine.setAmountInvoiced(null);
        saleOrderLine.setInvoiced(null);
        saleOrderLine.setIsInvoiceControlled(null);
        saleOrderLine.setReservedQty(BigDecimal.ZERO);
      }
    }

    return copy;
  }

  @Override
  public void remove(SaleOrder order) {

    Partner partner = order.getClientPartner();

    super.remove(order);

    try {
      Beans.get(AccountingSituationSupplychainService.class).updateUsedCredit(partner);
    } catch (AxelorException e) {
      e.printStackTrace();
    }
  }

  @Override
  public Map<String, Object> populate(Map<String, Object> json, Map<String, Object> context) {
    Long saleOrderId = (Long) json.get("id");
    InvoicingStateCache invoicingStateCache = Beans.get(InvoicingStateCache.class);
    Integer invoicingState;
    try {
      invoicingState = invoicingStateCache.getInvoicingStateFromCache(saleOrderId);
    } catch (ExecutionException e) {
      throw new RuntimeException(e);
    }
    json.put("$invoicingState", invoicingState);
    return super.populate(json, context);
  }
}
