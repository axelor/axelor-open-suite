/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2026 Axelor (<http://axelor.com>).
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
package com.axelor.apps.supplychain.service.saleorder;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.account.db.repo.InvoiceLineRepository;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Unit;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.db.repo.SaleOrderLineRepository;
import com.axelor.apps.stock.db.StockMove;
import com.axelor.apps.stock.db.StockMoveLine;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TestSaleOrderInvoiceServiceQtyCoverage {

  private SaleOrderInvoiceServiceImpl saleOrderInvoiceService;
  private Map<SaleOrderLine, BigDecimal> invoicedQtyMap;
  private Unit unit;
  private SaleOrder saleOrder;

  @BeforeEach
  void prepare() throws AxelorException {
    saleOrderInvoiceService = mock(SaleOrderInvoiceServiceImpl.class);
    saleOrderInvoiceService.appBaseService = mock(AppBaseService.class);
    when(saleOrderInvoiceService.appBaseService.getNbDecimalDigitForQty()).thenReturn(2);

    doCallRealMethod().when(saleOrderInvoiceService).checkInvoiceLineQuantities(any());
    doCallRealMethod().when(saleOrderInvoiceService).getMaxQtyToInvoice(any());
    doCallRealMethod().when(saleOrderInvoiceService).isLinkedToReversion(any());
    doCallRealMethod().when(saleOrderInvoiceService).convertQtyToSaleOrderLineUnit(any(), any());

    invoicedQtyMap = new HashMap<>();
    when(saleOrderInvoiceService.computeNonCanceledInvoiceQty(any(), any()))
        .thenAnswer(
            invocation -> invoicedQtyMap.getOrDefault(invocation.getArgument(0), BigDecimal.ZERO));

    unit = new Unit();
    saleOrder = new SaleOrder();
  }

  @Test
  void testMaxQtyIsDeliveredQtyWhenOverDelivered() {
    SaleOrderLine line = createLine("10");
    line.setDeliveredQty(new BigDecimal("12"));

    assertQty("12", saleOrderInvoiceService.getMaxQtyToInvoice(line));
  }

  @Test
  void testCheckAcceptsRemainingQty() {
    SaleOrderLine line = createLine("10");
    invoicedQtyMap.put(line, new BigDecimal("4"));
    Invoice invoice = createInvoice(InvoiceRepository.OPERATION_TYPE_CLIENT_SALE, 0);
    addInvoiceLine(invoice, line, "6");

    Assertions.assertDoesNotThrow(
        () -> saleOrderInvoiceService.checkInvoiceLineQuantities(invoice));
  }

  @Test
  void testCheckIgnoresRefundAndAdvanceInvoices() {
    SaleOrderLine line = createLine("10");
    invoicedQtyMap.put(line, new BigDecimal("10"));
    Invoice refund = createInvoice(InvoiceRepository.OPERATION_TYPE_CLIENT_REFUND, 0);
    addInvoiceLine(refund, line, "10");
    Invoice advance =
        createInvoice(
            InvoiceRepository.OPERATION_TYPE_CLIENT_SALE,
            InvoiceRepository.OPERATION_SUB_TYPE_ADVANCE);
    addInvoiceLine(advance, line, "10");

    Assertions.assertDoesNotThrow(() -> saleOrderInvoiceService.checkInvoiceLineQuantities(refund));
    Assertions.assertDoesNotThrow(
        () -> saleOrderInvoiceService.checkInvoiceLineQuantities(advance));
  }

  @Test
  void testCheckIgnoresReversionAndNonPositiveLines() {
    SaleOrderLine line = createLine("10");
    invoicedQtyMap.put(line, new BigDecimal("10"));
    Invoice invoice = createInvoice(InvoiceRepository.OPERATION_TYPE_CLIENT_SALE, 0);
    InvoiceLine reversionLine = addInvoiceLine(invoice, line, "10");
    StockMove reversion = new StockMove();
    reversion.setIsReversion(true);
    StockMoveLine stockMoveLine = new StockMoveLine();
    stockMoveLine.setStockMove(reversion);
    reversionLine.setStockMoveLine(stockMoveLine);
    SaleOrderLine otherLine = createLine("10");
    invoicedQtyMap.put(otherLine, new BigDecimal("10"));
    addInvoiceLine(invoice, otherLine, "-2");

    Assertions.assertDoesNotThrow(
        () -> saleOrderInvoiceService.checkInvoiceLineQuantities(invoice));
  }

  protected SaleOrderLine createLine(String qty) {
    SaleOrderLine line = new SaleOrderLine();
    line.setQty(new BigDecimal(qty));
    line.setUnit(unit);
    line.setTypeSelect(SaleOrderLineRepository.TYPE_NORMAL);
    line.setSaleOrder(saleOrder);
    return line;
  }

  protected Invoice createInvoice(int operationTypeSelect, int operationSubTypeSelect) {
    Invoice invoice = new Invoice();
    invoice.setOperationTypeSelect(operationTypeSelect);
    invoice.setOperationSubTypeSelect(operationSubTypeSelect);
    return invoice;
  }

  protected InvoiceLine addInvoiceLine(Invoice invoice, SaleOrderLine saleOrderLine, String qty) {
    InvoiceLine invoiceLine = new InvoiceLine();
    invoiceLine.setSaleOrderLine(saleOrderLine);
    invoiceLine.setUnit(unit);
    invoiceLine.setQty(new BigDecimal(qty));
    invoiceLine.setTypeSelect(InvoiceLineRepository.TYPE_NORMAL);
    invoice.addInvoiceLineListItem(invoiceLine);
    return invoiceLine;
  }

  protected void assertQty(String expected, BigDecimal actual) {
    Assertions.assertEquals(0, new BigDecimal(expected).compareTo(actual), "got " + actual);
  }
}
