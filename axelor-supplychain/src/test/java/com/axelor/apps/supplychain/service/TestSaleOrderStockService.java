/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2020 Axelor (<http://axelor.com>).
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.db.repo.SaleOrderRepository;
import com.axelor.exception.AxelorException;
import java.util.ArrayList;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TestSaleOrderStockService {

  private SaleOrderStockServiceImpl saleOrderStockService;

  @Before
  public void prepare() throws AxelorException {
    saleOrderStockService = mock(SaleOrderStockServiceImpl.class);
    when(saleOrderStockService.isStockMoveProduct(any(SaleOrderLine.class), any(SaleOrder.class)))
        .thenReturn(true);
    doCallRealMethod().when(saleOrderStockService).updateDeliveryState(any(SaleOrder.class));
  }

  @Test
  public void testUpdateDeliveryStateSaleOrderWithNull() throws AxelorException {
    SaleOrder saleOrder = new SaleOrder();
    saleOrderStockService.updateDeliveryState(saleOrder);
    Assert.assertEquals(
        SaleOrderRepository.DELIVERY_STATE_NOT_DELIVERED, (int) saleOrder.getDeliveryState());
  }

  @Test
  public void testUpdateDeliveryStateEmptySaleOrder() throws AxelorException {
    SaleOrder saleOrder = new SaleOrder();
    saleOrder.setSaleOrderLineList(new ArrayList<>());
    saleOrderStockService.updateDeliveryState(saleOrder);
    Assert.assertEquals(
        SaleOrderRepository.DELIVERY_STATE_NOT_DELIVERED, (int) saleOrder.getDeliveryState());
  }

  @Test
  public void testUpdateDeliveryStateDeliveredSaleOrder() throws AxelorException {
    SaleOrder saleOrder = new SaleOrder();
    saleOrder.setSaleOrderLineList(new ArrayList<>());
    SaleOrderLine saleOrderLine1 = new SaleOrderLine();
    SaleOrderLine saleOrderLine2 = new SaleOrderLine();
    saleOrderLine1.setDeliveryState(SaleOrderRepository.DELIVERY_STATE_DELIVERED);
    saleOrderLine2.setDeliveryState(SaleOrderRepository.DELIVERY_STATE_DELIVERED);
    saleOrder.addSaleOrderLineListItem(saleOrderLine1);
    saleOrder.addSaleOrderLineListItem(saleOrderLine2);

    saleOrderStockService.updateDeliveryState(saleOrder);
    Assert.assertEquals(
        SaleOrderRepository.DELIVERY_STATE_DELIVERED, (int) saleOrder.getDeliveryState());
  }

  @Test
  public void testUpdateDeliveryStatePartiallyDeliveredSaleOrder() throws AxelorException {
    SaleOrder saleOrder = new SaleOrder();
    SaleOrderLine saleOrderLine1 = new SaleOrderLine();
    SaleOrderLine saleOrderLine2 = new SaleOrderLine();
    saleOrderLine1.setDeliveryState(SaleOrderRepository.DELIVERY_STATE_DELIVERED);
    saleOrderLine2.setDeliveryState(SaleOrderRepository.DELIVERY_STATE_NOT_DELIVERED);
    saleOrder.addSaleOrderLineListItem(saleOrderLine1);
    saleOrder.addSaleOrderLineListItem(saleOrderLine2);

    saleOrderStockService.updateDeliveryState(saleOrder);
    Assert.assertEquals(
        SaleOrderRepository.DELIVERY_STATE_PARTIALLY_DELIVERED, (int) saleOrder.getDeliveryState());
  }

  @Test
  public void testUpdateDeliveryStatePartiallyDelivered2SaleOrder() throws AxelorException {
    SaleOrder saleOrder = new SaleOrder();
    SaleOrderLine saleOrderLine1 = new SaleOrderLine();
    SaleOrderLine saleOrderLine2 = new SaleOrderLine();
    saleOrderLine1.setDeliveryState(SaleOrderRepository.DELIVERY_STATE_NOT_DELIVERED);
    saleOrderLine2.setDeliveryState(SaleOrderRepository.DELIVERY_STATE_DELIVERED);
    saleOrder.addSaleOrderLineListItem(saleOrderLine1);
    saleOrder.addSaleOrderLineListItem(saleOrderLine2);

    saleOrderStockService.updateDeliveryState(saleOrder);
    Assert.assertEquals(
        SaleOrderRepository.DELIVERY_STATE_PARTIALLY_DELIVERED, (int) saleOrder.getDeliveryState());
  }

  @Test
  public void testUpdateDeliveryStatePartiallyDeliveredLinesSaleOrder() throws AxelorException {
    SaleOrder saleOrder = new SaleOrder();
    saleOrder.setSaleOrderLineList(new ArrayList<>());
    SaleOrderLine saleOrderLine1 = new SaleOrderLine();
    SaleOrderLine saleOrderLine2 = new SaleOrderLine();
    SaleOrderLine saleOrderLine3 = new SaleOrderLine();
    saleOrderLine1.setDeliveryState(SaleOrderRepository.DELIVERY_STATE_DELIVERED);
    saleOrderLine2.setDeliveryState(SaleOrderRepository.DELIVERY_STATE_NOT_DELIVERED);
    saleOrderLine3.setDeliveryState(SaleOrderRepository.DELIVERY_STATE_PARTIALLY_DELIVERED);
    saleOrder.addSaleOrderLineListItem(saleOrderLine1);
    saleOrder.addSaleOrderLineListItem(saleOrderLine2);
    saleOrder.addSaleOrderLineListItem(saleOrderLine3);

    saleOrderStockService.updateDeliveryState(saleOrder);
    Assert.assertEquals(
        SaleOrderRepository.DELIVERY_STATE_PARTIALLY_DELIVERED, (int) saleOrder.getDeliveryState());
  }

  @Test
  public void testUpdateDeliveryStateOnlyPartiallyDeliveredLinesSaleOrder() throws AxelorException {
    SaleOrder saleOrder = new SaleOrder();
    saleOrder.setSaleOrderLineList(new ArrayList<>());
    SaleOrderLine saleOrderLine1 = new SaleOrderLine();
    SaleOrderLine saleOrderLine2 = new SaleOrderLine();
    SaleOrderLine saleOrderLine3 = new SaleOrderLine();
    saleOrderLine1.setDeliveryState(SaleOrderRepository.DELIVERY_STATE_NOT_DELIVERED);
    saleOrderLine2.setDeliveryState(SaleOrderRepository.DELIVERY_STATE_PARTIALLY_DELIVERED);
    saleOrderLine3.setDeliveryState(SaleOrderRepository.DELIVERY_STATE_PARTIALLY_DELIVERED);
    saleOrder.addSaleOrderLineListItem(saleOrderLine1);
    saleOrder.addSaleOrderLineListItem(saleOrderLine2);
    saleOrder.addSaleOrderLineListItem(saleOrderLine3);

    saleOrderStockService.updateDeliveryState(saleOrder);
    Assert.assertEquals(
        SaleOrderRepository.DELIVERY_STATE_PARTIALLY_DELIVERED, (int) saleOrder.getDeliveryState());
  }
}
