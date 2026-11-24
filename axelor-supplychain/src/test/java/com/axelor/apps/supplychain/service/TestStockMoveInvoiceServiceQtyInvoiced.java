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
package com.axelor.apps.supplychain.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.Unit;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.purchase.db.PurchaseOrderLine;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.stock.db.StockMove;
import com.axelor.apps.stock.db.StockMoveLine;
import com.axelor.apps.stock.db.repo.StockMoveLineRepository;
import com.axelor.apps.stock.db.repo.StockMoveRepository;
import com.axelor.apps.supplychain.service.saleorderline.SaleOrderLineSublineServiceImpl;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TestStockMoveInvoiceServiceQtyInvoiced {

  private static final LocalDateTime T1 = LocalDateTime.of(2026, 11, 1, 10, 0);
  private static final LocalDateTime T2 = LocalDateTime.of(2026, 11, 2, 10, 0);
  private static final LocalDateTime T3 = LocalDateTime.of(2026, 11, 3, 10, 0);

  private StockMoveInvoiceServiceImpl stockMoveInvoiceService;
  private Unit unit;
  private Product product;
  private PurchaseOrderLine purchaseOrderLine;
  private StockMove stockMove;

  @BeforeEach
  void prepare() throws AxelorException {
    stockMoveInvoiceService = mock(StockMoveInvoiceServiceImpl.class);
    doCallRealMethod()
        .when(stockMoveInvoiceService)
        .updateStockMoveLinesQtyInvoiced(any(), any(), any());
    doCallRealMethod().when(stockMoveInvoiceService).getInvoiceLineTargetList(any(), any());
    doCallRealMethod()
        .when(stockMoveInvoiceService)
        .getUnlinkedInvoiceLineTargetList(any(), any(), any());
    doCallRealMethod().when(stockMoveInvoiceService).getVentilationOrderComparator();
    doCallRealMethod()
        .when(stockMoveInvoiceService)
        .allocateInvoiceLineQty(any(), any(), anyBoolean());
    doCallRealMethod().when(stockMoveInvoiceService).isSupplierRefund(any());
    doCallRealMethod().when(stockMoveInvoiceService).isLinkedToReversion(any());
    doCallRealMethod().when(stockMoveInvoiceService).convertQty(any(), any(), any(), any());
    when(stockMoveInvoiceService.getUnlinkedInvoiceLineList(any())).thenReturn(List.of());
    doCallRealMethod()
        .when(stockMoveInvoiceService)
        .updateSaleStockMoveLinesQtyInvoiced(any(), any(), any());
    doCallRealMethod().when(stockMoveInvoiceService).getSaleInvoiceLineTargetList(any(), any());
    doCallRealMethod().when(stockMoveInvoiceService).getAllSubSaleOrderLines(any());
    doCallRealMethod().when(stockMoveInvoiceService).allocateQty(any(), any(), any(), anyBoolean());
    stockMoveInvoiceService.saleOrderLineSublineService = new SaleOrderLineSublineServiceImpl();
    doCallRealMethod()
        .when(stockMoveInvoiceService)
        .computeNonCanceledInvoiceQty(any(StockMoveLine.class));
    doCallRealMethod().when(stockMoveInvoiceService).isIncomingPurchaseOrderStockMoveLine(any());
    doCallRealMethod().when(stockMoveInvoiceService).isOutgoingSaleOrderStockMoveLine(any());
    when(stockMoveInvoiceService.computeDirectNonCanceledInvoiceQty(any(), eq(false)))
        .thenReturn(new BigDecimal("2"));
    when(stockMoveInvoiceService.computeDirectNonCanceledInvoiceQty(any(), eq(true)))
        .thenReturn(new BigDecimal("7"));
    stockMoveInvoiceService.appBaseService = mock(AppBaseService.class);
    when(stockMoveInvoiceService.appBaseService.getNbDecimalDigitForQty()).thenReturn(4);

    unit = new Unit();
    product = new Product();
    purchaseOrderLine = new PurchaseOrderLine();
    stockMove = new StockMove();
    stockMove.setIsReversion(false);
  }

  @Test
  void testRefundThenReinvoiceKeepsReinvoicedQty() throws AxelorException {
    StockMoveLine stockMoveLine = createStockMoveLine("10");
    List<InvoiceLine> invoiceLineList =
        List.of(
            createInvoiceLine(1L, T1, false, "10"),
            createInvoiceLine(2L, T2, true, "10"),
            createInvoiceLine(3L, T3, false, "10"));

    update(List.of(stockMoveLine), invoiceLineList);

    assertQtyInvoiced("10", stockMoveLine);
  }

  @Test
  void testPartialRefundLeavesRemainingQty() throws AxelorException {
    StockMoveLine stockMoveLine = createStockMoveLine("10");
    List<InvoiceLine> invoiceLineList =
        List.of(createInvoiceLine(1L, T1, false, "10"), createInvoiceLine(2L, T2, true, "4"));

    update(List.of(stockMoveLine), invoiceLineList);

    assertQtyInvoiced("6", stockMoveLine);
  }

  @Test
  void testAllocationFollowsVentilationOrderNotListOrder() throws AxelorException {
    StockMoveLine stockMoveLine = createStockMoveLine("10");
    List<InvoiceLine> invoiceLineList =
        List.of(
            createInvoiceLine(3L, T3, false, "10"),
            createInvoiceLine(2L, T2, true, "10"),
            createInvoiceLine(1L, T1, false, "10"));

    update(List.of(stockMoveLine), invoiceLineList);

    assertQtyInvoiced("10", stockMoveLine);
  }

  @Test
  void testRefundVentilatedBeforeInvoiceFreesNothing() throws AxelorException {
    StockMoveLine stockMoveLine = createStockMoveLine("10");
    List<InvoiceLine> invoiceLineList =
        List.of(createInvoiceLine(1L, T2, false, "10"), createInvoiceLine(2L, T1, true, "10"));

    update(List.of(stockMoveLine), invoiceLineList);

    assertQtyInvoiced("10", stockMoveLine);
  }

  @Test
  void testMissingVentilationDateComesFirst() throws AxelorException {
    StockMoveLine stockMoveLine = createStockMoveLine("10");
    List<InvoiceLine> invoiceLineList =
        List.of(createInvoiceLine(2L, T1, true, "10"), createInvoiceLine(1L, null, false, "10"));

    update(List.of(stockMoveLine), invoiceLineList);

    assertQtyInvoiced("0", stockMoveLine);
  }

  @Test
  void testSameVentilationDateOrderedByInvoiceId() throws AxelorException {
    StockMoveLine stockMoveLine = createStockMoveLine("10");
    List<InvoiceLine> invoiceLineList =
        List.of(createInvoiceLine(2L, T1, true, "10"), createInvoiceLine(1L, T1, false, "10"));

    update(List.of(stockMoveLine), invoiceLineList);

    assertQtyInvoiced("0", stockMoveLine);
  }

  @Test
  void testInvoicedQtyCappedAtRealQty() throws AxelorException {
    StockMoveLine stockMoveLine = createStockMoveLine("10");

    update(List.of(stockMoveLine), List.of(createInvoiceLine(1L, T1, false, "12")));

    assertQtyInvoiced("10", stockMoveLine);
  }

  @Test
  void testSplitLinesFilledInOrder() throws AxelorException {
    StockMoveLine firstLine = createStockMoveLine("6");
    StockMoveLine secondLine = createStockMoveLine("4");
    List<InvoiceLine> invoiceLineList =
        List.of(createInvoiceLine(1L, T1, false, "10"), createInvoiceLine(2L, T2, true, "3"));

    update(List.of(firstLine, secondLine), invoiceLineList);

    assertQtyInvoiced("3", firstLine);
    assertQtyInvoiced("4", secondLine);
  }

  @Test
  void testInvoiceLineLinkedToReversionIgnored() throws AxelorException {
    StockMoveLine stockMoveLine = createStockMoveLine("10");
    StockMove reversion = new StockMove();
    reversion.setIsReversion(true);
    StockMoveLine reversionLine = new StockMoveLine();
    reversionLine.setStockMove(reversion);
    InvoiceLine refundLine = createInvoiceLine(2L, T2, true, "10");
    refundLine.setStockMoveLine(reversionLine);

    update(List.of(stockMoveLine), List.of(createInvoiceLine(1L, T1, false, "10"), refundLine));

    assertQtyInvoiced("10", stockMoveLine);
  }

  @Test
  void testSaleRefundThenReinvoiceKeepsReinvoicedQty() throws AxelorException {
    SaleOrderLine saleOrderLine = createSaleOrderLine("10", null);
    StockMoveLine stockMoveLine = createSaleStockMoveLine(saleOrderLine, stockMove, "10");
    List<InvoiceLine> invoiceLineList =
        List.of(
            createSaleInvoiceLine(saleOrderLine, 1L, T1, false, "10"),
            createSaleInvoiceLine(saleOrderLine, 2L, T2, true, "10"),
            createSaleInvoiceLine(saleOrderLine, 3L, T3, false, "10"));

    updateSale(List.of(stockMoveLine), invoiceLineList);

    assertQtyInvoiced("10", stockMoveLine);
  }

  @Test
  void testSalePartialOrderInvoice() throws AxelorException {
    SaleOrderLine saleOrderLine = createSaleOrderLine("10", null);
    StockMoveLine stockMoveLine = createSaleStockMoveLine(saleOrderLine, stockMove, "10");

    updateSale(
        List.of(stockMoveLine), List.of(createSaleInvoiceLine(saleOrderLine, 1L, T1, false, "4")));

    assertQtyInvoiced("4", stockMoveLine);
  }

  @Test
  void testSaleOrderInvoiceFillsDeliveriesInOrder() throws AxelorException {
    SaleOrderLine saleOrderLine = createSaleOrderLine("10", null);
    StockMove backorder = new StockMove();
    backorder.setIsReversion(false);
    StockMoveLine firstLine = createSaleStockMoveLine(saleOrderLine, stockMove, "4");
    StockMoveLine backorderLine = createSaleStockMoveLine(saleOrderLine, backorder, "6");

    updateSale(
        List.of(firstLine, backorderLine),
        List.of(createSaleInvoiceLine(saleOrderLine, 1L, T1, false, "10")));

    assertQtyInvoiced("4", firstLine);
    assertQtyInvoiced("6", backorderLine);
  }

  @Test
  void testSaleDeliveryInvoiceFillsItsOwnLineFirst() throws AxelorException {
    SaleOrderLine saleOrderLine = createSaleOrderLine("10", null);
    StockMove backorder = new StockMove();
    backorder.setIsReversion(false);
    StockMoveLine firstLine = createSaleStockMoveLine(saleOrderLine, stockMove, "6");
    StockMoveLine backorderLine = createSaleStockMoveLine(saleOrderLine, backorder, "4");
    InvoiceLine invoiceLine = createSaleInvoiceLine(saleOrderLine, 1L, T1, false, "4");
    invoiceLine.setStockMoveLine(backorderLine);

    updateSale(List.of(firstLine, backorderLine), List.of(invoiceLine));

    assertQtyInvoiced("0", firstLine);
    assertQtyInvoiced("4", backorderLine);
  }

  @Test
  void testSaleParentInvoiceCoversChildDelivery() throws AxelorException {
    SaleOrderLine parent = createSaleOrderLine("10", null);
    SaleOrderLine child = createSaleOrderLine("10", parent);
    StockMoveLine childLine = createSaleStockMoveLine(child, stockMove, "100");

    updateSale(List.of(childLine), List.of(createSaleInvoiceLine(parent, 1L, T1, false, "5")));

    assertQtyInvoiced("50", childLine);
    Assertions.assertTrue(childLine.getQtyInvoiced().scale() <= 10);
  }

  @Test
  void testSaleParentInvoiceCoversWholeChildDelivery() throws AxelorException {
    SaleOrderLine parent = createSaleOrderLine("10", null);
    SaleOrderLine child = createSaleOrderLine("10", parent);
    StockMoveLine childLine = createSaleStockMoveLine(child, stockMove, "100");

    updateSale(List.of(childLine), List.of(createSaleInvoiceLine(parent, 1L, T1, false, "10")));

    assertQtyInvoiced("100", childLine);
  }

  @Test
  void testOutgoingSaleLineCountsInvoicedQtyAndDrafts() throws AxelorException {
    stockMove.setTypeSelect(StockMoveRepository.TYPE_OUTGOING);
    StockMoveLine stockMoveLine =
        createSaleStockMoveLine(createSaleOrderLine("10", null), stockMove, "10");
    stockMoveLine.setLineTypeSelect(StockMoveLineRepository.TYPE_NORMAL);
    stockMoveLine.setQtyInvoiced(new BigDecimal("4"));

    Assertions.assertEquals(
        0,
        new BigDecimal("6")
            .compareTo(stockMoveInvoiceService.computeNonCanceledInvoiceQty(stockMoveLine)));
  }

  @Test
  void testOutgoingReversionLineKeepsDirectCount() throws AxelorException {
    stockMove.setTypeSelect(StockMoveRepository.TYPE_OUTGOING);
    stockMove.setIsReversion(true);
    StockMoveLine stockMoveLine =
        createSaleStockMoveLine(createSaleOrderLine("10", null), stockMove, "10");
    stockMoveLine.setLineTypeSelect(StockMoveLineRepository.TYPE_NORMAL);
    stockMoveLine.setQtyInvoiced(new BigDecimal("4"));

    Assertions.assertEquals(
        0,
        new BigDecimal("7")
            .compareTo(stockMoveInvoiceService.computeNonCanceledInvoiceQty(stockMoveLine)));
  }

  protected void updateSale(
      List<StockMoveLine> stockMoveLineList, List<InvoiceLine> invoiceLineList)
      throws AxelorException {
    Set<StockMove> stockMoveSet = new LinkedHashSet<>();
    stockMoveLineList.forEach(stockMoveLine -> stockMoveSet.add(stockMoveLine.getStockMove()));
    stockMoveInvoiceService.updateSaleStockMoveLinesQtyInvoiced(
        stockMoveSet, stockMoveLineList, invoiceLineList);
  }

  protected SaleOrderLine createSaleOrderLine(String qty, SaleOrderLine parent) {
    SaleOrderLine saleOrderLine = new SaleOrderLine();
    saleOrderLine.setQty(new BigDecimal(qty));
    saleOrderLine.setUnit(unit);
    saleOrderLine.setProduct(product);
    if (parent != null) {
      saleOrderLine.setParentSaleOrderLine(parent);
      parent.addSubSaleOrderLineListItem(saleOrderLine);
    }
    return saleOrderLine;
  }

  protected StockMoveLine createSaleStockMoveLine(
      SaleOrderLine saleOrderLine, StockMove targetStockMove, String realQty) {
    StockMoveLine stockMoveLine = new StockMoveLine();
    stockMoveLine.setStockMove(targetStockMove);
    stockMoveLine.setSaleOrderLine(saleOrderLine);
    stockMoveLine.setProduct(product);
    stockMoveLine.setUnit(unit);
    stockMoveLine.setRealQty(new BigDecimal(realQty));
    stockMoveLine.setQtyInvoiced(BigDecimal.ZERO);
    return stockMoveLine;
  }

  protected InvoiceLine createSaleInvoiceLine(
      SaleOrderLine saleOrderLine,
      Long invoiceId,
      LocalDateTime ventilatedDateTime,
      boolean isRefund,
      String qty) {
    InvoiceLine invoiceLine = createInvoiceLine(invoiceId, ventilatedDateTime, isRefund, qty);
    invoiceLine.setPurchaseOrderLine(null);
    invoiceLine.setSaleOrderLine(saleOrderLine);
    invoiceLine
        .getInvoice()
        .setOperationTypeSelect(
            isRefund
                ? InvoiceRepository.OPERATION_TYPE_CLIENT_REFUND
                : InvoiceRepository.OPERATION_TYPE_CLIENT_SALE);
    return invoiceLine;
  }

  protected void update(List<StockMoveLine> stockMoveLineList, List<InvoiceLine> invoiceLineList)
      throws AxelorException {
    Set<StockMove> stockMoveSet = new LinkedHashSet<>(List.of(stockMove));
    stockMoveInvoiceService.updateStockMoveLinesQtyInvoiced(
        stockMoveSet, stockMoveLineList, invoiceLineList);
  }

  protected StockMoveLine createStockMoveLine(String realQty) {
    StockMoveLine stockMoveLine = new StockMoveLine();
    stockMoveLine.setStockMove(stockMove);
    stockMoveLine.setPurchaseOrderLine(purchaseOrderLine);
    stockMoveLine.setProduct(product);
    stockMoveLine.setUnit(unit);
    stockMoveLine.setRealQty(new BigDecimal(realQty));
    stockMoveLine.setQtyInvoiced(BigDecimal.ZERO);
    return stockMoveLine;
  }

  protected InvoiceLine createInvoiceLine(
      Long invoiceId, LocalDateTime ventilatedDateTime, boolean isRefund, String qty) {
    Invoice invoice = new Invoice();
    invoice.setId(invoiceId);
    invoice.setVentilatedDateTime(ventilatedDateTime);
    invoice.setOperationTypeSelect(
        isRefund
            ? InvoiceRepository.OPERATION_TYPE_SUPPLIER_REFUND
            : InvoiceRepository.OPERATION_TYPE_SUPPLIER_PURCHASE);
    InvoiceLine invoiceLine = new InvoiceLine();
    invoiceLine.setId(invoiceId);
    invoiceLine.setInvoice(invoice);
    invoiceLine.setPurchaseOrderLine(purchaseOrderLine);
    invoiceLine.setProduct(product);
    invoiceLine.setUnit(unit);
    invoiceLine.setQty(new BigDecimal(qty));
    return invoiceLine;
  }

  protected void assertQtyInvoiced(String expected, StockMoveLine stockMoveLine) {
    Assertions.assertEquals(0, new BigDecimal(expected).compareTo(stockMoveLine.getQtyInvoiced()));
  }
}
