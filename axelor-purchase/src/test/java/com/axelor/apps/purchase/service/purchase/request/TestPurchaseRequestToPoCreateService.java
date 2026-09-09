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
package com.axelor.apps.purchase.service.purchase.request;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.Unit;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.purchase.db.PurchaseOrder;
import com.axelor.apps.purchase.db.PurchaseOrderLine;
import com.axelor.apps.purchase.db.PurchaseRequest;
import com.axelor.apps.purchase.db.PurchaseRequestLine;
import com.axelor.apps.purchase.db.repo.PurchaseOrderRepository;
import com.axelor.apps.purchase.db.repo.PurchaseRequestRepository;
import com.axelor.apps.purchase.service.PurchaseOrderCreateService;
import com.axelor.apps.purchase.service.PurchaseOrderLineService;
import com.axelor.apps.purchase.service.PurchaseOrderService;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;

class TestPurchaseRequestToPoCreateService {
  private PurchaseRequestToPoCreateServiceImpl service;
  private PurchaseOrderLineService lineService;
  private PurchaseOrderService orderService;
  private Partner supplier;
  private Product product;
  private Unit unit;
  private long nextId;

  @BeforeEach
  void setUp() throws AxelorException {
    supplier = new Partner();
    supplier.setId(1L);
    product = new Product();
    product.setId(1L);
    unit = new Unit();
    unit.setId(1L);
    lineService = mock(PurchaseOrderLineService.class);
    orderService = mock(PurchaseOrderService.class);
    when(lineService.fillPrice(any(), any())).thenAnswer(invocation -> invocation.getArgument(0));
    when(lineService.createPurchaseOrderLine(any(), any(), any(), any(), any(), any()))
        .thenAnswer(
            invocation -> {
              PurchaseOrderLine line = new PurchaseOrderLine();
              line.setPurchaseOrder(invocation.getArgument(0));
              line.setProduct(invocation.getArgument(1));
              line.setProductName(invocation.getArgument(2));
              line.setQty(invocation.getArgument(4));
              line.setUnit(invocation.getArgument(5));
              return line;
            });
    service =
        new PurchaseRequestToPoCreateServiceImpl(
            orderService,
            mock(PurchaseOrderCreateService.class),
            lineService,
            mock(PurchaseOrderRepository.class),
            mock(PurchaseRequestRepository.class),
            mock(AppBaseService.class)) {
          @Override
          protected PurchaseOrder createPurchaseOrder(
              PurchaseRequest request, Company company, Partner defaultSupplier) {
            PurchaseOrder order = new PurchaseOrder();
            order.setId(++nextId);
            order.setSupplierPartner(request.getSupplierPartner());
            order.setPurchaseOrderLineList(new ArrayList<>());
            return order;
          }

          @Override
          protected String getGroupBySupplierKey(PurchaseRequest request, Partner supplier) {
            return super.getGroupBySupplierKey(request, supplier) + "_stockLocation";
          }
        };
  }

  @Test
  void aggregatesAcrossRequestsWithoutChangingSourceQuantitiesOrStatuses() throws AxelorException {
    PurchaseRequest first = request(line(product, unit, "1"));
    PurchaseRequest second = request(line(product, unit, "1"));
    var result = service.createFromRequests(List.of(first, second), true, false, null, null);
    assertFalse(result.hasWarnings());
    assertEquals(1, result.getPurchaseOrders().size());
    PurchaseOrder order = result.getPurchaseOrders().get(0);
    assertEquals(1, order.getPurchaseOrderLineList().size());
    assertEquals(new BigDecimal("2"), order.getPurchaseOrderLineList().get(0).getQty());
    assertSame(order, first.getPurchaseOrder());
    assertSame(order, second.getPurchaseOrder());
    assertEquals(PurchaseRequestRepository.STATUS_ACCEPTED, first.getStatusSelect());
    assertEquals(PurchaseRequestRepository.STATUS_ACCEPTED, second.getStatusSelect());
    assertEquals(BigDecimal.ONE, first.getPurchaseRequestLineList().get(0).getQuantity());
    assertEquals(BigDecimal.ONE, second.getPurchaseRequestLineList().get(0).getQuantity());
    verify(orderService).computePurchaseOrder(order);
  }

  @Test
  void keepsDifferentProductsUnitsMissingUnitsAndFreeTextSeparate() throws AxelorException {
    Unit box = new Unit();
    box.setId(2L);
    Product otherProduct = new Product();
    otherProduct.setId(2L);
    PurchaseRequestLine freeText = line(null, unit, "4");
    freeText.setNewProduct(true);
    freeText.setProductTitle("Custom product");
    PurchaseRequestLine otherFreeText = line(null, unit, "5");
    otherFreeText.setNewProduct(true);
    otherFreeText.setProductTitle("Custom product");
    PurchaseRequest first =
        request(
            line(product, unit, "1"), line(product, box, "2"), line(product, null, "3"), freeText);
    PurchaseRequest second =
        request(
            line(product, unit, "6"),
            line(product, null, "7"),
            otherFreeText,
            line(otherProduct, unit, "8"));
    var result = service.createFromRequests(List.of(first, second), true, false, null, null);
    var lines = result.getPurchaseOrders().get(0).getPurchaseOrderLineList();
    assertEquals(7, lines.size());
    assertEquals(
        List.of("7", "2", "3", "4", "7", "5", "8"),
        lines.stream().map(l -> l.getQty().toPlainString()).toList());
    assertSame(unit, lines.get(0).getUnit());
    assertSame(box, lines.get(1).getUnit());
    assertEquals("Custom product", lines.get(3).getProductName());
    assertEquals("Custom product", lines.get(5).getProductName());
  }

  @Test
  void neverAggregatesAcrossSupplierOrders() throws AxelorException {
    PurchaseRequest first = request(line(product, unit, "1"));
    PurchaseRequest second = request(line(product, unit, "2"));
    Partner otherSupplier = new Partner();
    otherSupplier.setId(2L);
    second.setSupplierPartner(otherSupplier);
    var orders =
        service
            .createFromRequests(List.of(first, second), true, false, null, null)
            .getPurchaseOrders();
    assertEquals(2, orders.size());
    assertNotSame(first.getPurchaseOrder(), second.getPurchaseOrder());
    assertEquals(
        BigDecimal.ONE, first.getPurchaseOrder().getPurchaseOrderLineList().get(0).getQty());
    assertEquals(
        new BigDecimal("2"), second.getPurchaseOrder().getPurchaseOrderLineList().get(0).getQty());
  }

  @Test
  void leavesUngroupedRequestsAndDuplicateLinesUnchanged() throws AxelorException {
    PurchaseRequest first = request(line(product, unit, "1"), line(product, unit, "2"));
    PurchaseRequest second = request(line(product, unit, "3"));
    var orders =
        service
            .createFromRequests(List.of(first, second), false, false, null, null)
            .getPurchaseOrders();
    assertEquals(2, orders.size());
    assertEquals(2, first.getPurchaseOrder().getPurchaseOrderLineList().size());
    assertEquals(
        BigDecimal.ONE, first.getPurchaseOrder().getPurchaseOrderLineList().get(0).getQty());
  }

  @Test
  void excludesAlreadyLinkedAndUnacceptedRequestsFromTotals() throws AxelorException {
    PurchaseRequest accepted = request(line(product, unit, "1"));
    PurchaseRequest linked = request(line(product, unit, "2"));
    PurchaseOrder existing = new PurchaseOrder();
    linked.setPurchaseOrder(existing);
    PurchaseRequest draft = request(line(product, unit, "3"));
    draft.setStatusSelect(PurchaseRequestRepository.STATUS_DRAFT);
    var result =
        service.createFromRequests(List.of(accepted, linked, draft), true, false, null, null);
    assertTrue(result.hasWarnings());
    assertEquals(1, result.getPurchaseOrders().size());
    assertEquals(
        BigDecimal.ONE, accepted.getPurchaseOrder().getPurchaseOrderLineList().get(0).getQty());
    assertSame(existing, linked.getPurchaseOrder());
    assertNull(draft.getPurchaseOrder());
  }

  @Test
  void pricesAggregatedLineOnCombinedQuantityBeforeComputingTotals() throws AxelorException {
    PurchaseRequest first = request(line(product, unit, "1"));
    PurchaseRequest second = request(line(product, unit, "2"));
    PurchaseOrder order =
        service
            .createFromRequests(List.of(first, second), true, false, null, null)
            .getPurchaseOrders()
            .get(0);
    ArgumentCaptor<PurchaseOrderLine> captor = ArgumentCaptor.forClass(PurchaseOrderLine.class);
    verify(lineService).fillPrice(captor.capture(), same(order));
    PurchaseOrderLine priced = captor.getValue();
    assertEquals(new BigDecimal("3"), priced.getQty());
    assertSame(unit, priced.getUnit());
    InOrder inOrder = inOrder(lineService);
    inOrder.verify(lineService).fillPrice(same(priced), same(order));
    inOrder.verify(lineService).compute(same(priced), same(order));
  }

  @Test
  void pricesEachUngroupedLineOnRequestedQuantity() throws AxelorException {
    PurchaseRequest request = request(line(product, unit, "1"), line(product, unit, "2"));
    service.createFromRequests(List.of(request), false, false, null, null);
    ArgumentCaptor<PurchaseOrderLine> captor = ArgumentCaptor.forClass(PurchaseOrderLine.class);
    verify(lineService, times(2)).fillPrice(captor.capture(), any());
    assertEquals(
        List.of("1", "2"),
        captor.getAllValues().stream().map(l -> l.getQty().toPlainString()).toList());
  }

  @Test
  void skipsPricingForFreeTextLines() throws AxelorException {
    PurchaseRequestLine freeText = line(null, unit, "4");
    freeText.setNewProduct(true);
    freeText.setProductTitle("Custom product");
    service.createFromRequests(List.of(request(freeText)), true, false, null, null);
    verify(lineService, never()).fillPrice(any(), any());
  }

  @Test
  void groupsNoSupplierRequestWithMatchingSupplierRequestUnderOneOrder() throws AxelorException {
    PurchaseRequest withSupplier = request(line(product, unit, "1"));
    PurchaseRequest withoutSupplier = request(line(product, unit, "2"));
    withoutSupplier.setSupplierPartner(null);
    var orders =
        service
            .createFromRequests(List.of(withSupplier, withoutSupplier), true, false, null, supplier)
            .getPurchaseOrders();
    assertEquals(1, orders.size());
    assertSame(orders.get(0), withoutSupplier.getPurchaseOrder());
    assertEquals(new BigDecimal("3"), orders.get(0).getPurchaseOrderLineList().get(0).getQty());
  }

  private PurchaseRequest request(PurchaseRequestLine... lines) {
    PurchaseRequest request = new PurchaseRequest();
    request.setId(++nextId);
    request.setSupplierPartner(supplier);
    request.setStatusSelect(PurchaseRequestRepository.STATUS_ACCEPTED);
    for (PurchaseRequestLine line : lines) {
      request.addPurchaseRequestLineListItem(line);
    }
    return request;
  }

  private PurchaseRequestLine line(Product product, Unit unit, String quantity) {
    PurchaseRequestLine line = new PurchaseRequestLine();
    line.setProduct(product);
    line.setUnit(unit);
    line.setQuantity(new BigDecimal(quantity));
    return line;
  }
}
