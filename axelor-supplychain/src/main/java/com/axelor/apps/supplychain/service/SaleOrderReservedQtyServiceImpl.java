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
package com.axelor.apps.supplychain.service;

import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.exception.AxelorException;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class SaleOrderReservedQtyServiceImpl implements SaleOrderReservedQtyService {

  protected ReservedQtyService reservedQtyService;

  @Inject
  public SaleOrderReservedQtyServiceImpl(ReservedQtyService reservedQtyService) {
    this.reservedQtyService = reservedQtyService;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void allocateAll(SaleOrder saleOrder) throws AxelorException {
    List<SaleOrderLine> saleOrderLineList =
        saleOrder.getSaleOrderLineList() == null
            ? new ArrayList<>()
            : saleOrder.getSaleOrderLineList();

    for (SaleOrderLine saleOrderLine : saleOrderLineList) {
      reservedQtyService.allocateAll(saleOrderLine);
    }
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void deallocateAll(SaleOrder saleOrder) throws AxelorException {
    List<SaleOrderLine> saleOrderLineList =
        saleOrder.getSaleOrderLineList() == null
            ? new ArrayList<>()
            : saleOrder.getSaleOrderLineList();
    for (SaleOrderLine saleOrderLine : saleOrderLineList) {
      reservedQtyService.updateReservedQty(saleOrderLine, BigDecimal.ZERO);
    }
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void reserveAll(SaleOrder saleOrder) throws AxelorException {
    List<SaleOrderLine> saleOrderLineList =
        saleOrder.getSaleOrderLineList() == null
            ? new ArrayList<>()
            : saleOrder.getSaleOrderLineList();
    for (SaleOrderLine saleOrderLine : saleOrderLineList) {
      reservedQtyService.requestQty(saleOrderLine);
    }
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void cancelReservation(SaleOrder saleOrder) throws AxelorException {
    List<SaleOrderLine> saleOrderLineList =
        saleOrder.getSaleOrderLineList() == null
            ? new ArrayList<>()
            : saleOrder.getSaleOrderLineList();
    for (SaleOrderLine saleOrderLine : saleOrderLineList) {
      reservedQtyService.cancelReservation(saleOrderLine);
    }
  }
}
