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
package com.axelor.apps.supplychain.db.repo;

import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.service.app.AppService;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.db.repo.SaleOrderManagementRepository;
import com.axelor.apps.supplychain.service.AccountingSituationSupplychainService;
import com.axelor.exception.AxelorException;
import com.axelor.inject.Beans;
import java.math.BigDecimal;

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
        saleOrderLine.setInvoicingDate(null);
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
}
