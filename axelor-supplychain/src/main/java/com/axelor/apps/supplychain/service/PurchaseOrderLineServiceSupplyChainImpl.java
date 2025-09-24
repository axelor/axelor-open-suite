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
package com.axelor.apps.supplychain.service;

import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.account.db.repo.InvoiceLineRepository;
import com.axelor.apps.account.service.analytic.AnalyticMoveLineService;
import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.Unit;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.UnitConversionService;
import com.axelor.apps.base.service.publicHoliday.PublicHolidayService;
import com.axelor.apps.purchase.db.PurchaseOrder;
import com.axelor.apps.purchase.db.PurchaseOrderLine;
import com.axelor.apps.purchase.service.PurchaseOrderLineServiceImpl;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.db.repo.SaleOrderLineRepository;
import com.axelor.apps.stock.db.StockMoveLine;
import com.axelor.apps.stock.db.repo.StockMoveLineRepository;
import com.axelor.apps.stock.db.repo.StockMoveRepository;
import com.axelor.apps.supplychain.exception.SupplychainExceptionMessage;
import com.axelor.apps.supplychain.model.AnalyticLineModel;
import com.axelor.apps.supplychain.service.app.AppSupplychainService;
import com.axelor.i18n.I18n;
import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.util.List;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PurchaseOrderLineServiceSupplyChainImpl extends PurchaseOrderLineServiceImpl
    implements PurchaseOrderLineServiceSupplyChain {

  protected AnalyticMoveLineService analyticMoveLineService;

  protected UnitConversionService unitConversionService;

  protected AppAccountService appAccountService;

  protected AccountConfigService accountConfigService;
  protected AnalyticLineModelService analyticLineModelService;
  protected final PublicHolidayService publicHolidayService;
  protected final AppSupplychainService appSupplychainService;
  protected StockMoveLineRepository stockMoveLineRepository;
  protected InvoiceLineRepository invoiceLineRepository;

  @Inject
  public PurchaseOrderLineServiceSupplyChainImpl(
      AnalyticMoveLineService analyticMoveLineService,
      UnitConversionService unitConversionService,
      AppAccountService appAccountService,
      AccountConfigService accountConfigService,
      AnalyticLineModelService analyticLineModelService,
      PublicHolidayService publicHolidayService,
      AppSupplychainService appSupplychainService,
      StockMoveLineRepository stockMoveLineRepository,
      InvoiceLineRepository invoiceLineRepository) {
    this.analyticMoveLineService = analyticMoveLineService;
    this.unitConversionService = unitConversionService;
    this.appAccountService = appAccountService;
    this.accountConfigService = accountConfigService;
    this.analyticLineModelService = analyticLineModelService;
    this.publicHolidayService = publicHolidayService;
    this.appSupplychainService = appSupplychainService;
    this.stockMoveLineRepository = stockMoveLineRepository;
    this.invoiceLineRepository = invoiceLineRepository;
  }

  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  public PurchaseOrderLine fill(PurchaseOrderLine purchaseOrderLine, PurchaseOrder purchaseOrder)
      throws AxelorException {

    purchaseOrderLine = super.fill(purchaseOrderLine, purchaseOrder);

    if (!appSupplychainService.isApp("supplychain")) {
      return purchaseOrderLine;
    }

    var company = purchaseOrder.getCompany();
    var product = purchaseOrderLine.getProduct();
    AnalyticLineModel analyticLineModel = new AnalyticLineModel(purchaseOrderLine, purchaseOrder);
    analyticLineModelService.getAndComputeAnalyticDistribution(analyticLineModel);

    var supplierDeliveryTime =
        getSupplierDeliveryTime(product, purchaseOrder.getSupplierPartner(), company);
    if (purchaseOrder.getEstimatedReceiptDate() == null && supplierDeliveryTime != null) {

      var todayDate = appBaseService.getTodayDate(company);
      var freeDateDay =
          publicHolidayService.getFreeDay(todayDate.plusDays(supplierDeliveryTime), company);
      purchaseOrderLine.setEstimatedReceiptDate(freeDateDay);
    }

    return purchaseOrderLine;
  }

  protected Integer getSupplierDeliveryTime(
      Product product, Partner supplierPartner, Company company) throws AxelorException {

    if (appPurchaseService.getAppPurchase().getManageSupplierCatalog()) {
      var supplierCatalog =
          supplierCatalogService.getSupplierCatalog(product, supplierPartner, company);
      if (supplierCatalog != null) {
        return supplierCatalog.getSupplierDeliveryTime();
      } else if (product.getSupplierDeliveryTime() != null) {
        return product.getSupplierDeliveryTime();
      }
    }

    return null;
  }

  @Override
  public PurchaseOrderLine createPurchaseOrderLine(
      PurchaseOrder purchaseOrder, SaleOrderLine saleOrderLine) throws AxelorException {

    LOG.debug(
        "Creation of a purchase order line for the product : {}", saleOrderLine.getProductName());

    Unit unit = null;
    BigDecimal qty = BigDecimal.ZERO;

    boolean isNormalLine = saleOrderLine.getTypeSelect() == SaleOrderLineRepository.TYPE_NORMAL;
    if (isNormalLine) {

      if (saleOrderLine.getProduct() != null) {
        unit = saleOrderLine.getProduct().getPurchasesUnit();
      }
      qty = saleOrderLine.getQty();
      if (unit == null) {
        unit = saleOrderLine.getUnit();
      } else {
        qty =
            unitConversionService.convert(
                saleOrderLine.getUnit(), unit, qty, qty.scale(), saleOrderLine.getProduct());
      }
    }

    PurchaseOrderLine purchaseOrderLine =
        super.createPurchaseOrderLine(
            purchaseOrder,
            saleOrderLine.getProduct(),
            isNormalLine ? null : saleOrderLine.getProductName(),
            null,
            qty,
            unit);

    purchaseOrderLine.setIsTitleLine(!isNormalLine);

    AnalyticLineModel analyticLineModel = new AnalyticLineModel(purchaseOrderLine, purchaseOrder);
    analyticLineModelService.getAndComputeAnalyticDistribution(analyticLineModel);

    return purchaseOrderLine;
  }

  public BigDecimal computeUndeliveredQty(PurchaseOrderLine purchaseOrderLine) {
    Preconditions.checkNotNull(purchaseOrderLine);

    BigDecimal undeliveryQty =
        purchaseOrderLine.getQty().subtract(purchaseOrderLine.getReceivedQty());

    if (undeliveryQty.signum() > 0) {
      return undeliveryQty;
    }
    return BigDecimal.ZERO;
  }

  @Override
  public void validateDeletion(PurchaseOrderLine purchaseOrderLine) throws AxelorException {
    super.validateDeletion(purchaseOrderLine);
    if (!isEditable(purchaseOrderLine.getPurchaseOrder())) {
      return;
    }
    StockMoveLine stockMoveLine =
        stockMoveLineRepository
            .all()
            .autoFlush(false)
            .filter(
                "self.purchaseOrderLine = :purchaseOrderLine AND self.stockMove.statusSelect = :realizedStatus")
            .bind("purchaseOrderLine", purchaseOrderLine)
            .bind("realizedStatus", StockMoveRepository.STATUS_REALIZED)
            .fetchOne();
    if (stockMoveLine != null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(SupplychainExceptionMessage.PURCHASE_ORDER_LINE_DELETE_NOT_ALLOWED_STOCK_MOVE));
    }
    List<InvoiceLine> invoiceLines = getInvoiceLines(purchaseOrderLine);
    if (CollectionUtils.isNotEmpty(invoiceLines)) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(SupplychainExceptionMessage.PURCHASE_ORDER_LINE_DELETE_NOT_ALLOWED_INVOICE));
    }
  }

  @Override
  public boolean validateRealizedQty(
      PurchaseOrder purchaseOrder, PurchaseOrderLine purchaseOrderLine) {
    if (!isEditable(purchaseOrder)) {
      return true;
    }

    if (purchaseOrderLine.getId() == null) {
      return true;
    }
    StockMoveLine stockMoveLine =
        stockMoveLineRepository
            .all()
            .filter(
                "self.purchaseOrderLine = :purchaseOrderLine AND self.stockMove.statusSelect = :realized")
            .bind("purchaseOrderLine", purchaseOrderLine)
            .bind("realized", StockMoveRepository.STATUS_REALIZED)
            .fetchOne();
    return stockMoveLine == null
        || purchaseOrderLine.getQty().compareTo(stockMoveLine.getRealQty()) >= 0;
  }

  @Override
  public boolean validateInvoicedQty(
      PurchaseOrder purchaseOrder, PurchaseOrderLine purchaseOrderLine) {
    if (!isEditable(purchaseOrder)) {
      return true;
    }

    if (purchaseOrderLine.getId() == null) {
      return true;
    }

    List<InvoiceLine> invoiceLines = getInvoiceLines(purchaseOrderLine);
    return CollectionUtils.isEmpty(invoiceLines)
        || purchaseOrderLine
                .getQty()
                .compareTo(
                    invoiceLines.stream()
                        .map(InvoiceLine::getQty)
                        .reduce(BigDecimal.ZERO, BigDecimal::add))
            >= 0;
  }

  protected boolean isEditable(PurchaseOrder purchaseOrder) {
    return purchaseOrder != null && purchaseOrder.getOrderBeingEdited();
  }

  protected List<InvoiceLine> getInvoiceLines(PurchaseOrderLine purchaseOrderLine) {
    return invoiceLineRepository
        .all()
        .autoFlush(false)
        .filter("self.purchaseOrderLine = :purchaseOrderLine")
        .bind("purchaseOrderLine", purchaseOrderLine)
        .fetch();
  }
}
