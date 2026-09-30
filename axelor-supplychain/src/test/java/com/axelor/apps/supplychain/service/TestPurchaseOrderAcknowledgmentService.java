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

import com.axelor.apps.purchase.db.PurchaseOrderLine;
import com.axelor.apps.supplychain.db.PurchaseOrderAcknowledgment;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class TestPurchaseOrderAcknowledgmentService {

  private final PurchaseOrderAcknowledgmentService service =
      new PurchaseOrderAcknowledgmentServiceImpl();

  @Test
  void testComputeAcknowledgmentDataEmptyList() {
    PurchaseOrderLine purchaseOrderLine = new PurchaseOrderLine();

    PurchaseOrderAcknowledgmentService.AcknowledgmentData result =
        service.computeAcknowledgmentData(purchaseOrderLine);

    Assertions.assertNull(result.maxDeliveryDate());
    Assertions.assertFalse(result.qtyDiscrepancy());
  }

  @Test
  void testComputeAcknowledgmentDataSingleAcknowledgment() {
    LocalDate deliveryDate = LocalDate.of(2026, 4, 20);
    PurchaseOrderLine purchaseOrderLine =
        createPurchaseOrderLine(
            new BigDecimal("10"), List.of(createAcknowledgment(new BigDecimal("5"), deliveryDate)));

    PurchaseOrderAcknowledgmentService.AcknowledgmentData result =
        service.computeAcknowledgmentData(purchaseOrderLine);

    Assertions.assertEquals(deliveryDate, result.maxDeliveryDate());
    Assertions.assertTrue(result.qtyDiscrepancy());
  }

  @Test
  void testComputeAcknowledgmentDataKeepsMaxDeliveryDate() {
    LocalDate firstDate = LocalDate.of(2026, 4, 20);
    LocalDate secondDate = LocalDate.of(2026, 4, 24);
    PurchaseOrderLine purchaseOrderLine =
        createPurchaseOrderLine(
            new BigDecimal("7"),
            List.of(
                createAcknowledgment(new BigDecimal("3"), firstDate),
                createAcknowledgment(new BigDecimal("4"), secondDate)));

    PurchaseOrderAcknowledgmentService.AcknowledgmentData result =
        service.computeAcknowledgmentData(purchaseOrderLine);

    Assertions.assertEquals(secondDate, result.maxDeliveryDate());
    Assertions.assertFalse(result.qtyDiscrepancy());
  }

  @Test
  void testComputeAcknowledgmentDataIgnoresNullDeliveryDate() {
    LocalDate deliveryDate = LocalDate.of(2026, 4, 20);
    PurchaseOrderLine purchaseOrderLine =
        createPurchaseOrderLine(
            new BigDecimal("10"),
            List.of(
                createAcknowledgment(new BigDecimal("6"), null),
                createAcknowledgment(new BigDecimal("4"), deliveryDate)));

    PurchaseOrderAcknowledgmentService.AcknowledgmentData result =
        service.computeAcknowledgmentData(purchaseOrderLine);

    Assertions.assertEquals(deliveryDate, result.maxDeliveryDate());
    Assertions.assertFalse(result.qtyDiscrepancy());
  }

  @Test
  void testComputeAcknowledgmentDataQtyBelowOrderedQty() {
    PurchaseOrderLine purchaseOrderLine =
        createPurchaseOrderLine(
            new BigDecimal("10"),
            List.of(
                createAcknowledgment(new BigDecimal("3"), null),
                createAcknowledgment(new BigDecimal("4"), null)));

    PurchaseOrderAcknowledgmentService.AcknowledgmentData result =
        service.computeAcknowledgmentData(purchaseOrderLine);

    Assertions.assertTrue(result.qtyDiscrepancy());
  }

  @Test
  void testComputeAcknowledgmentDataQtyEqualOrderedQty() {
    PurchaseOrderLine purchaseOrderLine =
        createPurchaseOrderLine(
            new BigDecimal("10"),
            List.of(
                createAcknowledgment(new BigDecimal("6"), null),
                createAcknowledgment(new BigDecimal("4"), null)));

    PurchaseOrderAcknowledgmentService.AcknowledgmentData result =
        service.computeAcknowledgmentData(purchaseOrderLine);

    Assertions.assertFalse(result.qtyDiscrepancy());
  }

  @Test
  void testComputeAcknowledgmentDataQtyAboveOrderedQty() {
    PurchaseOrderLine purchaseOrderLine =
        createPurchaseOrderLine(
            new BigDecimal("10"),
            List.of(
                createAcknowledgment(new BigDecimal("6"), null),
                createAcknowledgment(new BigDecimal("5"), null)));

    PurchaseOrderAcknowledgmentService.AcknowledgmentData result =
        service.computeAcknowledgmentData(purchaseOrderLine);

    Assertions.assertTrue(result.qtyDiscrepancy());
  }

  @Test
  void testComputeAcknowledgmentDataTreatsNullQtyAsZero() {
    LocalDate deliveryDate = LocalDate.of(2026, 4, 25);
    PurchaseOrderLine purchaseOrderLine =
        createPurchaseOrderLine(
            new BigDecimal("2"),
            List.of(
                createAcknowledgment(null, deliveryDate),
                createAcknowledgment(new BigDecimal("2"), null)));

    PurchaseOrderAcknowledgmentService.AcknowledgmentData result =
        service.computeAcknowledgmentData(purchaseOrderLine);

    Assertions.assertEquals(deliveryDate, result.maxDeliveryDate());
    Assertions.assertFalse(result.qtyDiscrepancy());
  }

  @Test
  void testComputeAmountByDeliveryDateEmptyList() {
    PurchaseOrderLine purchaseOrderLine =
        createPurchaseOrderLine(new BigDecimal("10"), new BigDecimal("1216.00"), List.of());

    Map<LocalDate, BigDecimal> result =
        service.computeAmountByDeliveryDate(purchaseOrderLine, BigDecimal.ZERO);

    Assertions.assertTrue(result.isEmpty());
  }

  @Test
  void testComputeAmountByDeliveryDateUsesConfirmedQtyNotOrderedQty() {
    LocalDate firstDate = LocalDate.of(2026, 10, 15);
    LocalDate secondDate = LocalDate.of(2026, 11, 20);
    PurchaseOrderLine purchaseOrderLine =
        createPurchaseOrderLine(
            new BigDecimal("10"),
            new BigDecimal("1216.00"),
            List.of(
                createAcknowledgment(new BigDecimal("4"), firstDate),
                createAcknowledgment(new BigDecimal("4"), secondDate)));

    Map<LocalDate, BigDecimal> result =
        service.computeAmountByDeliveryDate(purchaseOrderLine, BigDecimal.ZERO);

    Assertions.assertEquals(2, result.size());
    Assertions.assertEquals(0, new BigDecimal("486.40").compareTo(result.get(firstDate)));
    Assertions.assertEquals(0, new BigDecimal("486.40").compareTo(result.get(secondDate)));
  }

  @Test
  void testComputeAmountByDeliveryDateDeductsInvoicedAmount() {
    LocalDate firstDate = LocalDate.of(2026, 10, 15);
    LocalDate secondDate = LocalDate.of(2026, 11, 20);
    PurchaseOrderLine purchaseOrderLine =
        createPurchaseOrderLine(
            new BigDecimal("10"),
            new BigDecimal("1216.00"),
            List.of(
                createAcknowledgment(new BigDecimal("4"), firstDate),
                createAcknowledgment(new BigDecimal("4"), secondDate)));

    Map<LocalDate, BigDecimal> result =
        service.computeAmountByDeliveryDate(purchaseOrderLine, new BigDecimal("200.00"));

    Assertions.assertEquals(0, new BigDecimal("386.40").compareTo(result.get(firstDate)));
    Assertions.assertEquals(0, new BigDecimal("386.40").compareTo(result.get(secondDate)));
  }

  @Test
  void testComputeAmountByDeliveryDateTreatsNullInvoicedAmountAsZero() {
    LocalDate deliveryDate = LocalDate.of(2026, 10, 15);
    PurchaseOrderLine purchaseOrderLine =
        createPurchaseOrderLine(
            new BigDecimal("10"),
            new BigDecimal("1216.00"),
            List.of(createAcknowledgment(new BigDecimal("4"), deliveryDate)));

    Map<LocalDate, BigDecimal> result =
        service.computeAmountByDeliveryDate(purchaseOrderLine, null);

    Assertions.assertEquals(0, new BigDecimal("486.40").compareTo(result.get(deliveryDate)));
  }

  @Test
  void testComputeAmountByDeliveryDateOverInvoicedKeepsDatesWithZeroAmount() {
    LocalDate firstDate = LocalDate.of(2026, 10, 15);
    LocalDate secondDate = LocalDate.of(2026, 11, 20);
    PurchaseOrderLine purchaseOrderLine =
        createPurchaseOrderLine(
            new BigDecimal("10"),
            new BigDecimal("1216.00"),
            List.of(
                createAcknowledgment(new BigDecimal("4"), firstDate),
                createAcknowledgment(new BigDecimal("4"), secondDate)));

    Map<LocalDate, BigDecimal> result =
        service.computeAmountByDeliveryDate(purchaseOrderLine, new BigDecimal("2000.00"));

    Assertions.assertEquals(2, result.size());
    Assertions.assertEquals(0, BigDecimal.ZERO.compareTo(result.get(firstDate)));
    Assertions.assertEquals(0, BigDecimal.ZERO.compareTo(result.get(secondDate)));
  }

  @Test
  void testComputeAmountByDeliveryDateZeroOrderedQty() {
    LocalDate deliveryDate = LocalDate.of(2026, 10, 15);
    PurchaseOrderLine purchaseOrderLine =
        createPurchaseOrderLine(
            BigDecimal.ZERO,
            new BigDecimal("1216.00"),
            List.of(createAcknowledgment(new BigDecimal("4"), deliveryDate)));

    Map<LocalDate, BigDecimal> result =
        service.computeAmountByDeliveryDate(purchaseOrderLine, BigDecimal.ZERO);

    Assertions.assertTrue(result.isEmpty());
  }

  @Test
  void testComputeAmountByDeliveryDateLastShareTakesRemainder() {
    LocalDate firstDate = LocalDate.of(2026, 10, 15);
    LocalDate secondDate = LocalDate.of(2026, 10, 20);
    LocalDate thirdDate = LocalDate.of(2026, 10, 25);
    PurchaseOrderLine purchaseOrderLine =
        createPurchaseOrderLine(
            new BigDecimal("3"),
            new BigDecimal("100.00"),
            List.of(
                createAcknowledgment(BigDecimal.ONE, firstDate),
                createAcknowledgment(BigDecimal.ONE, secondDate),
                createAcknowledgment(BigDecimal.ONE, thirdDate)));

    Map<LocalDate, BigDecimal> result =
        service.computeAmountByDeliveryDate(purchaseOrderLine, BigDecimal.ZERO);

    BigDecimal total = result.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
    Assertions.assertEquals(0, new BigDecimal("100.00").compareTo(total));
    Assertions.assertEquals(0, new BigDecimal("33.33").compareTo(result.get(firstDate)));
    Assertions.assertEquals(0, new BigDecimal("33.34").compareTo(result.get(thirdDate)));
  }

  @Test
  void testComputeAmountByDeliveryDateMergesSameDateAndSkipsInvalid() {
    LocalDate deliveryDate = LocalDate.of(2026, 10, 15);
    PurchaseOrderLine purchaseOrderLine =
        createPurchaseOrderLine(
            new BigDecimal("10"),
            new BigDecimal("500.00"),
            List.of(
                createAcknowledgment(new BigDecimal("2"), deliveryDate),
                createAcknowledgment(new BigDecimal("3"), deliveryDate),
                createAcknowledgment(new BigDecimal("4"), null),
                createAcknowledgment(null, LocalDate.of(2026, 10, 30)),
                createAcknowledgment(BigDecimal.ZERO, LocalDate.of(2026, 10, 30))));

    Map<LocalDate, BigDecimal> result =
        service.computeAmountByDeliveryDate(purchaseOrderLine, BigDecimal.ZERO);

    Assertions.assertEquals(1, result.size());
    Assertions.assertEquals(0, new BigDecimal("250.00").compareTo(result.get(deliveryDate)));
  }

  protected PurchaseOrderLine createPurchaseOrderLine(
      BigDecimal qty, List<PurchaseOrderAcknowledgment> acknowledgmentList) {
    return createPurchaseOrderLine(qty, BigDecimal.ZERO, acknowledgmentList);
  }

  protected PurchaseOrderLine createPurchaseOrderLine(
      BigDecimal qty, BigDecimal inTaxTotal, List<PurchaseOrderAcknowledgment> acknowledgmentList) {
    PurchaseOrderLine purchaseOrderLine = new PurchaseOrderLine();
    purchaseOrderLine.setQty(qty);
    purchaseOrderLine.setInTaxTotal(inTaxTotal);
    purchaseOrderLine.setPurchaseOrderAcknowledgmentList(acknowledgmentList);
    return purchaseOrderLine;
  }

  protected PurchaseOrderAcknowledgment createAcknowledgment(
      BigDecimal qty, LocalDate deliveryDate) {
    PurchaseOrderAcknowledgment acknowledgment = new PurchaseOrderAcknowledgment();
    acknowledgment.setQty(qty);
    acknowledgment.setDeliveryDate(deliveryDate);
    return acknowledgment;
  }
}
