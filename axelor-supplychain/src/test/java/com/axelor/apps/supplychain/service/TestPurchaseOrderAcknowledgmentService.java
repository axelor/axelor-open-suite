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
    Assertions.assertFalse(result.qtyExceeded());
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
    Assertions.assertFalse(result.qtyExceeded());
  }

  @Test
  void testComputeAcknowledgmentDataKeepsMaxDeliveryDate() {
    LocalDate firstDate = LocalDate.of(2026, 4, 20);
    LocalDate secondDate = LocalDate.of(2026, 4, 24);
    PurchaseOrderLine purchaseOrderLine =
        createPurchaseOrderLine(
            new BigDecimal("10"),
            List.of(
                createAcknowledgment(new BigDecimal("3"), firstDate),
                createAcknowledgment(new BigDecimal("4"), secondDate)));

    PurchaseOrderAcknowledgmentService.AcknowledgmentData result =
        service.computeAcknowledgmentData(purchaseOrderLine);

    Assertions.assertEquals(secondDate, result.maxDeliveryDate());
    Assertions.assertFalse(result.qtyExceeded());
  }

  @Test
  void testComputeAcknowledgmentDataIgnoresNullDeliveryDate() {
    PurchaseOrderLine purchaseOrderLine =
        createPurchaseOrderLine(
            new BigDecimal("10"), List.of(createAcknowledgment(new BigDecimal("3"), null)));

    PurchaseOrderAcknowledgmentService.AcknowledgmentData result =
        service.computeAcknowledgmentData(purchaseOrderLine);

    Assertions.assertNull(result.maxDeliveryDate());
    Assertions.assertFalse(result.qtyExceeded());
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

    Assertions.assertFalse(result.qtyExceeded());
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

    Assertions.assertFalse(result.qtyExceeded());
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

    Assertions.assertTrue(result.qtyExceeded());
  }

  @Test
  void testComputeAcknowledgmentDataTreatsNullQtyAsZero() {
    LocalDate deliveryDate = LocalDate.of(2026, 4, 25);
    PurchaseOrderLine purchaseOrderLine =
        createPurchaseOrderLine(
            new BigDecimal("10"),
            List.of(
                createAcknowledgment(null, deliveryDate),
                createAcknowledgment(new BigDecimal("2"), null)));

    PurchaseOrderAcknowledgmentService.AcknowledgmentData result =
        service.computeAcknowledgmentData(purchaseOrderLine);

    Assertions.assertEquals(deliveryDate, result.maxDeliveryDate());
    Assertions.assertFalse(result.qtyExceeded());
  }

  protected PurchaseOrderLine createPurchaseOrderLine(
      BigDecimal qty, List<PurchaseOrderAcknowledgment> acknowledgmentList) {
    PurchaseOrderLine purchaseOrderLine = new PurchaseOrderLine();
    purchaseOrderLine.setQty(qty);
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
