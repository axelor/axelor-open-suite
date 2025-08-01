/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2025 Axelor (<http://axelor.com>).
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
package com.axelor.apps.production.service.observer;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.production.service.SaleOrderConfirmProductionService;
import com.axelor.apps.production.service.SaleOrderCopyProductionService;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.service.event.SaleOrderConfirm;
import com.axelor.apps.sale.service.event.SaleOrderCopy;
import com.axelor.event.Observes;
import com.axelor.inject.Beans;

public class SaleOrderProductionObserver {

  public void productionConfirmSaleOrder(@Observes SaleOrderConfirm event) throws AxelorException {
    SaleOrder saleOrder = event.getSaleOrder();
    Beans.get(SaleOrderConfirmProductionService.class).confirmProcess(saleOrder);
  }

  public void copySaleOrder(@Observes SaleOrderCopy event) {
    SaleOrder saleOrder = event.getSaleOrder();
    Beans.get(SaleOrderCopyProductionService.class).copySaleOrderProductionProcess(saleOrder);
  }
}
