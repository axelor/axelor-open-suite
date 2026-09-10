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
package com.axelor.apps.quality.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.repo.ProductRepository;
import com.axelor.apps.purchase.db.PurchaseOrder;
import com.axelor.apps.purchase.db.PurchaseOrderLine;
import com.axelor.apps.quality.db.ControlEntry;
import com.axelor.apps.quality.db.ControlEntrySample;
import com.axelor.apps.quality.db.QIDetection;
import com.axelor.apps.quality.db.QIIdentification;
import com.axelor.apps.quality.db.QualityConfig;
import com.axelor.apps.quality.db.repo.ControlEntrySampleRepository;
import com.axelor.apps.quality.db.repo.QIDetectionRepository;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.stock.db.StockMove;
import com.axelor.apps.stock.db.StockMoveLine;
import com.axelor.apps.stock.db.TrackingNumber;
import com.axelor.apps.stock.db.repo.StockMoveLineRepository;
import com.axelor.apps.stock.db.repo.StockMoveRepository;
import com.axelor.apps.stock.db.repo.TrackingNumberRepository;
import com.axelor.auth.db.User;
import com.axelor.db.Query;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TestQualityImprovementPrefillServiceImpl {

  private static final long STOCK_MOVE_LINE_ID = 1L;
  private static final long PRODUCT_ID = 2L;
  private static final long TRACKING_NUMBER_ID = 3L;

  private StockMoveLineRepository stockMoveLineRepository;
  private ProductRepository productRepository;
  private TrackingNumberRepository trackingNumberRepository;
  private QIDetectionRepository qiDetectionRepository;
  private QualityImprovementPrefillService service;
  private QIIdentification qiIdentification;
  private Product product;
  private QIDetection supplierDetection;
  private QIDetection internalDetection;
  private QIDetection customerDetection;

  @BeforeEach
  void setUp() {
    stockMoveLineRepository = mock(StockMoveLineRepository.class);
    productRepository = mock(ProductRepository.class);
    trackingNumberRepository = mock(TrackingNumberRepository.class);
    qiDetectionRepository = mock(QIDetectionRepository.class);
    service =
        new QualityImprovementPrefillServiceImpl(
            stockMoveLineRepository,
            productRepository,
            trackingNumberRepository,
            qiDetectionRepository);
    qiIdentification = new QIIdentification();
    product = new Product();
    product.setCode("COMP-0001");
    supplierDetection = detection(QIDetectionRepository.ORIGIN_SUPPLIER);
    internalDetection = detection(QIDetectionRepository.ORIGIN_INTERNAL);
    customerDetection = detection(QIDetectionRepository.ORIGIN_CUSTOMER);
  }

  @Test
  void receptionLineFillsSupplierChain() {
    Partner supplier = partner("Supplier");
    PurchaseOrder purchaseOrder = new PurchaseOrder();
    purchaseOrder.setSupplierPartner(supplier);
    PurchaseOrderLine purchaseOrderLine = new PurchaseOrderLine();
    purchaseOrderLine.setPurchaseOrder(purchaseOrder);
    StockMoveLine stockMoveLine =
        stockMoveLine(StockMoveRepository.TYPE_INCOMING, partner("Other"), new BigDecimal("10"));
    stockMoveLine.setPurchaseOrderLine(purchaseOrderLine);
    ControlEntry controlEntry = controlEntryOn(StockMoveLine.class, STOCK_MOVE_LINE_ID);

    service.fillFromControlEntry(qiIdentification, controlEntry, supplierDetection);

    assertSame(product, qiIdentification.getProduct());
    assertEquals(new BigDecimal("10"), qiIdentification.getQuantity());
    assertSame(stockMoveLine, qiIdentification.getStockMoveLine());
    assertSame(stockMoveLine.getStockMove(), qiIdentification.getStockMove());
    assertSame(purchaseOrderLine, qiIdentification.getSupplierPurchaseOrderLine());
    assertSame(purchaseOrder, qiIdentification.getSupplierPurchaseOrder());
    assertSame(supplier, qiIdentification.getSupplierPartner());
    assertNull(qiIdentification.getCustomerPartner());
    assertEquals(QIDetectionRepository.ORIGIN_SUPPLIER, service.getDetectionOrigin(controlEntry));
  }

  @Test
  void receptionLineWithoutPurchaseOrderUsesStockMovePartner() {
    Partner supplier = partner("Supplier");
    stockMoveLine(StockMoveRepository.TYPE_INCOMING, supplier, BigDecimal.ONE);
    ControlEntry controlEntry = controlEntryOn(StockMoveLine.class, STOCK_MOVE_LINE_ID);

    service.fillFromControlEntry(qiIdentification, controlEntry, supplierDetection);

    assertSame(supplier, qiIdentification.getSupplierPartner());
    assertNull(qiIdentification.getSupplierPurchaseOrder());
  }

  @Test
  void deliveryLineFillsCustomerChain() {
    Partner customer = partner("Customer");
    SaleOrder saleOrder = new SaleOrder();
    saleOrder.setClientPartner(customer);
    SaleOrderLine saleOrderLine = new SaleOrderLine();
    saleOrderLine.setSaleOrder(saleOrder);
    StockMoveLine stockMoveLine =
        stockMoveLine(StockMoveRepository.TYPE_OUTGOING, partner("Other"), BigDecimal.ONE);
    stockMoveLine.setSaleOrderLine(saleOrderLine);
    ControlEntry controlEntry = controlEntryOn(StockMoveLine.class, STOCK_MOVE_LINE_ID);

    service.fillFromControlEntry(qiIdentification, controlEntry, customerDetection);

    assertSame(saleOrderLine, qiIdentification.getCustomerSaleOrderLine());
    assertSame(saleOrder, qiIdentification.getCustomerSaleOrder());
    assertSame(customer, qiIdentification.getCustomerPartner());
    assertNull(qiIdentification.getSupplierPartner());
    assertEquals(QIDetectionRepository.ORIGIN_CUSTOMER, service.getDetectionOrigin(controlEntry));
  }

  @Test
  void internalMoveLineIsInternalOrigin() {
    stockMoveLine(StockMoveRepository.TYPE_INTERNAL, null, BigDecimal.ONE);
    ControlEntry controlEntry = controlEntryOn(StockMoveLine.class, STOCK_MOVE_LINE_ID);

    assertEquals(QIDetectionRepository.ORIGIN_INTERNAL, service.getDetectionOrigin(controlEntry));
  }

  @Test
  void productSourceFillsProductOnly() {
    when(productRepository.find(PRODUCT_ID)).thenReturn(product);
    ControlEntry controlEntry = controlEntryOn(Product.class, PRODUCT_ID);

    service.fillFromControlEntry(qiIdentification, controlEntry, internalDetection);

    assertSame(product, qiIdentification.getProduct());
    assertNull(qiIdentification.getStockMoveLine());
    assertEquals(BigDecimal.ZERO, qiIdentification.getQuantity());
    assertEquals(QIDetectionRepository.ORIGIN_INTERNAL, service.getDetectionOrigin(controlEntry));
  }

  @Test
  void trackingNumberSourceFillsItsProduct() {
    TrackingNumber trackingNumber = new TrackingNumber();
    trackingNumber.setProduct(product);
    when(trackingNumberRepository.find(TRACKING_NUMBER_ID)).thenReturn(trackingNumber);
    ControlEntry controlEntry = controlEntryOn(TrackingNumber.class, TRACKING_NUMBER_ID);

    service.fillFromControlEntry(qiIdentification, controlEntry, internalDetection);

    assertSame(product, qiIdentification.getProduct());
  }

  @Test
  void nonCompliantSamplesAreCounted() {
    ControlEntry controlEntry = new ControlEntry();
    controlEntry.addControlEntrySamplesListItem(
        sample(ControlEntrySampleRepository.RESULT_NOT_COMPLIANT));
    controlEntry.addControlEntrySamplesListItem(
        sample(ControlEntrySampleRepository.RESULT_COMPLIANT));
    controlEntry.addControlEntrySamplesListItem(
        sample(ControlEntrySampleRepository.RESULT_NOT_COMPLIANT));

    service.fillFromControlEntry(qiIdentification, controlEntry, internalDetection);

    assertEquals(BigDecimal.valueOf(2), qiIdentification.getNonConformingQuantity());
  }

  @Test
  void inspectorPartnerIsDetectedByInternal() {
    Partner inspectorPartner = partner("Inspector");
    User inspector = new User();
    inspector.setPartner(inspectorPartner);
    ControlEntry controlEntry = new ControlEntry();
    controlEntry.setInspector(inspector);

    service.fillFromControlEntry(qiIdentification, controlEntry, internalDetection);

    assertSame(inspectorPartner, qiIdentification.getDetectedByInternal());
  }

  @Test
  void fillDetectedBySetsInspectorPartnerOnly() {
    Partner inspectorPartner = partner("Inspector");
    User inspector = new User();
    inspector.setPartner(inspectorPartner);
    ControlEntry controlEntry = controlEntryOn(Product.class, PRODUCT_ID);
    controlEntry.setInspector(inspector);
    controlEntry.addControlEntrySamplesListItem(
        sample(ControlEntrySampleRepository.RESULT_NOT_COMPLIANT));

    service.fillDetectedBy(qiIdentification, controlEntry);

    assertSame(inspectorPartner, qiIdentification.getDetectedByInternal());
    assertNull(qiIdentification.getProduct());
    assertEquals(BigDecimal.ZERO, qiIdentification.getNonConformingQuantity());
  }

  @Test
  void noSourceFillsNothingAndOriginIsInternal() {
    ControlEntry controlEntry = new ControlEntry();

    service.fillFromControlEntry(qiIdentification, controlEntry, internalDetection);

    assertNull(qiIdentification.getProduct());
    assertNull(qiIdentification.getStockMove());
    assertEquals(BigDecimal.ZERO, qiIdentification.getNonConformingQuantity());
    assertEquals(QIDetectionRepository.ORIGIN_INTERNAL, service.getDetectionOrigin(controlEntry));
  }

  @Test
  void defaultDetectionIsTheSingleCandidate() {
    QIDetection qiDetection = new QIDetection();
    mockDetections(List.of(qiDetection));

    assertSame(qiDetection, service.getDefaultDetection(new ControlEntry()));
  }

  @Test
  void defaultDetectionIsNullWhenSeveralCandidates() {
    mockDetections(List.of(new QIDetection(), new QIDetection()));

    assertNull(service.getDefaultDetection(new ControlEntry()));
  }

  @Test
  void defaultDetectionIsNullWhenNoCandidate() {
    mockDetections(List.of());

    assertNull(service.getDefaultDetection(new ControlEntry()));
  }

  @Test
  void receptionLineDefaultDetectionIsTheConfiguredReceptionDetection() {
    StockMoveLine stockMoveLine =
        stockMoveLine(StockMoveRepository.TYPE_INCOMING, partner("Supplier"), BigDecimal.ONE);
    QIDetection receptionQiDetection = detection(QIDetectionRepository.ORIGIN_SUPPLIER);
    stockMoveLine.getStockMove().setCompany(companyWithReceptionDetection(receptionQiDetection));
    mockDetections(List.of(supplierDetection));

    assertSame(receptionQiDetection, service.getDefaultDetection(stockMoveLine));
  }

  @Test
  void receptionLineDefaultDetectionFallsBackToTheSingleSupplierCandidate() {
    StockMoveLine stockMoveLine =
        stockMoveLine(StockMoveRepository.TYPE_INCOMING, partner("Supplier"), BigDecimal.ONE);
    stockMoveLine.getStockMove().setCompany(companyWithReceptionDetection(null));
    mockDetections(List.of(supplierDetection));

    assertSame(supplierDetection, service.getDefaultDetection(stockMoveLine));
  }

  @Test
  void deliveryLineDefaultDetectionIgnoresTheReceptionDetection() {
    StockMoveLine stockMoveLine =
        stockMoveLine(StockMoveRepository.TYPE_OUTGOING, partner("Customer"), BigDecimal.ONE);
    stockMoveLine
        .getStockMove()
        .setCompany(
            companyWithReceptionDetection(detection(QIDetectionRepository.ORIGIN_SUPPLIER)));
    mockDetections(List.of(customerDetection));

    assertSame(customerDetection, service.getDefaultDetection(stockMoveLine));
  }

  @Test
  void customerDetectionOnReceptionLineLeavesSupplierChainEmpty() {
    PurchaseOrder purchaseOrder = new PurchaseOrder();
    purchaseOrder.setSupplierPartner(partner("Supplier"));
    PurchaseOrderLine purchaseOrderLine = new PurchaseOrderLine();
    purchaseOrderLine.setPurchaseOrder(purchaseOrder);
    StockMoveLine stockMoveLine =
        stockMoveLine(StockMoveRepository.TYPE_INCOMING, partner("Supplier"), BigDecimal.TEN);
    stockMoveLine.setPurchaseOrderLine(purchaseOrderLine);
    ControlEntry controlEntry = controlEntryOn(StockMoveLine.class, STOCK_MOVE_LINE_ID);

    service.fillFromControlEntry(qiIdentification, controlEntry, customerDetection);

    assertSame(product, qiIdentification.getProduct());
    assertSame(stockMoveLine, qiIdentification.getStockMoveLine());
    assertNull(qiIdentification.getSupplierPartner());
    assertNull(qiIdentification.getSupplierPurchaseOrder());
    assertNull(qiIdentification.getSupplierPurchaseOrderLine());
    assertNull(qiIdentification.getCustomerPartner());
  }

  @Test
  void supplierDetectionOnDeliveryLineLeavesCustomerChainEmpty() {
    SaleOrder saleOrder = new SaleOrder();
    saleOrder.setClientPartner(partner("Customer"));
    SaleOrderLine saleOrderLine = new SaleOrderLine();
    saleOrderLine.setSaleOrder(saleOrder);
    StockMoveLine stockMoveLine =
        stockMoveLine(StockMoveRepository.TYPE_OUTGOING, partner("Customer"), BigDecimal.ONE);
    stockMoveLine.setSaleOrderLine(saleOrderLine);
    ControlEntry controlEntry = controlEntryOn(StockMoveLine.class, STOCK_MOVE_LINE_ID);

    service.fillFromControlEntry(qiIdentification, controlEntry, supplierDetection);

    assertNull(qiIdentification.getCustomerSaleOrderLine());
    assertNull(qiIdentification.getCustomerSaleOrder());
    assertNull(qiIdentification.getCustomerPartner());
    assertNull(qiIdentification.getSupplierPartner());
  }

  @Test
  void internalDetectionOnReceptionLineKeepsOnlyProductAndMove() {
    stockMoveLine(StockMoveRepository.TYPE_INCOMING, partner("Supplier"), BigDecimal.TEN);
    ControlEntry controlEntry = controlEntryOn(StockMoveLine.class, STOCK_MOVE_LINE_ID);

    service.fillFromControlEntry(qiIdentification, controlEntry, internalDetection);

    assertSame(product, qiIdentification.getProduct());
    assertEquals(BigDecimal.TEN, qiIdentification.getQuantity());
    assertNull(qiIdentification.getSupplierPartner());
    assertNull(qiIdentification.getCustomerPartner());
  }

  @Test
  void reversedDeliveryWithSaleOrderLineIsCustomerOrigin() {
    SaleOrderLine saleOrderLine = new SaleOrderLine();
    saleOrderLine.setSaleOrder(new SaleOrder());
    StockMoveLine stockMoveLine =
        stockMoveLine(StockMoveRepository.TYPE_INCOMING, partner("Customer"), BigDecimal.ONE);
    stockMoveLine.setSaleOrderLine(saleOrderLine);
    stockMoveLine.getStockMove().setIsReversion(true);
    ControlEntry controlEntry = controlEntryOn(StockMoveLine.class, STOCK_MOVE_LINE_ID);

    assertEquals(QIDetectionRepository.ORIGIN_CUSTOMER, service.getDetectionOrigin(controlEntry));
  }

  @Test
  void reversedDeliveryWithoutOrderLineIsCustomerOrigin() {
    Partner customer = partner("Customer");
    stockMoveLine(StockMoveRepository.TYPE_INCOMING, customer, BigDecimal.ONE)
        .getStockMove()
        .setIsReversion(true);
    ControlEntry controlEntry = controlEntryOn(StockMoveLine.class, STOCK_MOVE_LINE_ID);

    service.fillFromControlEntry(qiIdentification, controlEntry, customerDetection);

    assertEquals(QIDetectionRepository.ORIGIN_CUSTOMER, service.getDetectionOrigin(controlEntry));
    assertSame(customer, qiIdentification.getCustomerPartner());
    assertNull(qiIdentification.getSupplierPartner());
  }

  @Test
  void incomingWithoutPartnerButWithPurchaseOrderLineIsSupplierOrigin() {
    PurchaseOrderLine purchaseOrderLine = new PurchaseOrderLine();
    purchaseOrderLine.setPurchaseOrder(new PurchaseOrder());
    StockMoveLine stockMoveLine =
        stockMoveLine(StockMoveRepository.TYPE_INCOMING, null, BigDecimal.ONE);
    stockMoveLine.setPurchaseOrderLine(purchaseOrderLine);
    ControlEntry controlEntry = controlEntryOn(StockMoveLine.class, STOCK_MOVE_LINE_ID);

    assertEquals(QIDetectionRepository.ORIGIN_SUPPLIER, service.getDetectionOrigin(controlEntry));
  }

  @SuppressWarnings("unchecked")
  private void mockDetections(List<QIDetection> qiDetectionList) {
    Query<QIDetection> query = mock(Query.class);
    when(qiDetectionRepository.all()).thenReturn(query);
    when(query.filter(anyString())).thenReturn(query);
    when(query.bind(anyString(), any())).thenReturn(query);
    when(query.fetch(2)).thenReturn(qiDetectionList);
  }

  private StockMoveLine stockMoveLine(int typeSelect, Partner partner, BigDecimal realQty) {
    StockMove stockMove = new StockMove();
    stockMove.setTypeSelect(typeSelect);
    stockMove.setPartner(partner);
    StockMoveLine stockMoveLine = new StockMoveLine();
    stockMoveLine.setStockMove(stockMove);
    stockMoveLine.setProduct(product);
    stockMoveLine.setRealQty(realQty);
    when(stockMoveLineRepository.find(STOCK_MOVE_LINE_ID)).thenReturn(stockMoveLine);
    return stockMoveLine;
  }

  private ControlEntry controlEntryOn(Class<?> relatedToClass, long relatedToSelectId) {
    ControlEntry controlEntry = new ControlEntry();
    controlEntry.setRelatedToSelect(relatedToClass.getName());
    controlEntry.setRelatedToSelectId(relatedToSelectId);
    return controlEntry;
  }

  private ControlEntrySample sample(int resultSelect) {
    ControlEntrySample controlEntrySample = new ControlEntrySample();
    controlEntrySample.setResultSelect(resultSelect);
    return controlEntrySample;
  }

  private Company companyWithReceptionDetection(QIDetection receptionQiDetection) {
    Company company = new Company();
    QualityConfig qualityConfig = new QualityConfig();
    qualityConfig.setReceptionQiDetection(receptionQiDetection);
    company.setQualityConfig(qualityConfig);
    return company;
  }

  private QIDetection detection(int origin) {
    QIDetection qiDetection = new QIDetection();
    qiDetection.setOrigin(origin);
    return qiDetection;
  }

  private Partner partner(String name) {
    Partner partner = new Partner();
    partner.setName(name);
    return partner;
  }
}
