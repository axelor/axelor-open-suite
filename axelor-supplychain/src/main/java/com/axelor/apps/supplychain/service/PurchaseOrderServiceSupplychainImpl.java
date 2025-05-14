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

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.CurrencyScaleService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.purchase.db.PurchaseOrder;
import com.axelor.apps.purchase.db.repo.PurchaseOrderLineRepository;
import com.axelor.apps.purchase.db.repo.PurchaseOrderRepository;
import com.axelor.apps.purchase.service.PurchaseOrderLineService;
import com.axelor.apps.purchase.service.PurchaseOrderServiceImpl;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.repo.SaleOrderRepository;
import com.axelor.apps.stock.db.StockConfig;
import com.axelor.apps.stock.db.StockLocation;
import com.axelor.apps.stock.service.PartnerStockSettingsService;
import com.axelor.apps.stock.service.config.StockConfigService;
import com.axelor.apps.supplychain.db.Timetable;
import com.axelor.apps.supplychain.exception.SupplychainExceptionMessage;
import com.axelor.apps.supplychain.service.app.AppSupplychainService;
import com.axelor.apps.supplychain.service.invoice.AdvancePaymentRefundService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PurchaseOrderServiceSupplychainImpl extends PurchaseOrderServiceImpl
    implements PurchaseOrderSupplychainService {

  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  protected AppSupplychainService appSupplychainService;
  protected AccountConfigService accountConfigService;
  protected AppAccountService appAccountService;
  protected AppBaseService appBaseService;
  protected PurchaseOrderStockService purchaseOrderStockService;
  protected PurchaseOrderLineRepository purchaseOrderLineRepository;
  protected PurchaseOrderLineService purchaseOrderLineService;
  protected PartnerStockSettingsService partnerStockSettingsService;
  protected StockConfigService stockConfigService;
  protected CurrencyScaleService currencyScaleService;
  protected AdvancePaymentRefundService refundService;

  @Inject
  public PurchaseOrderServiceSupplychainImpl(
      AppSupplychainService appSupplychainService,
      AccountConfigService accountConfigService,
      AppAccountService appAccountService,
      AppBaseService appBaseService,
      PurchaseOrderStockService purchaseOrderStockService,
      PurchaseOrderLineRepository purchaseOrderLineRepository,
      PurchaseOrderLineService purchaseOrderLineService,
      PartnerStockSettingsService partnerStockSettingsService,
      StockConfigService stockConfigService,
      CurrencyScaleService currencyScaleService,
      AdvancePaymentRefundService refundService) {

    this.appSupplychainService = appSupplychainService;
    this.accountConfigService = accountConfigService;
    this.appAccountService = appAccountService;
    this.appBaseService = appBaseService;
    this.purchaseOrderStockService = purchaseOrderStockService;
    this.purchaseOrderLineRepository = purchaseOrderLineRepository;
    this.purchaseOrderLineService = purchaseOrderLineService;
    this.partnerStockSettingsService = partnerStockSettingsService;
    this.stockConfigService = stockConfigService;
    this.currencyScaleService = currencyScaleService;
    this.refundService = refundService;
  }

  @Override
  public void _computePurchaseOrder(PurchaseOrder purchaseOrder) throws AxelorException {
    super._computePurchaseOrder(purchaseOrder);

    if (appSupplychainService.isApp("supplychain")) {
      if (appAccountService.getAppAccount().getManageAdvancePaymentInvoice()) {
        purchaseOrder.setAdvanceTotal(computeTotalInvoiceAdvancePayment(purchaseOrder));
      }
    }
  }

  protected BigDecimal computeTotalInvoiceAdvancePayment(PurchaseOrder purchaseOrder) {
    BigDecimal total = BigDecimal.ZERO;

    if (purchaseOrder.getId() == null) {
      return total;
    }

    List<Invoice> advancePaymentInvoiceList =
        Beans.get(InvoiceRepository.class)
            .all()
            .filter(
                "self.purchaseOrder.id = :purchaseOrderId AND self.operationSubTypeSelect = :operationSubTypeSelect AND self.operationTypeSelect = :operationTypeSelect")
            .bind("purchaseOrderId", purchaseOrder.getId())
            .bind("operationSubTypeSelect", InvoiceRepository.OPERATION_SUB_TYPE_ADVANCE)
            .bind("operationTypeSelect", InvoiceRepository.OPERATION_TYPE_SUPPLIER_PURCHASE)
            .fetch();
    if (advancePaymentInvoiceList == null || advancePaymentInvoiceList.isEmpty()) {
      return total;
    }
    for (Invoice advance : advancePaymentInvoiceList) {
      BigDecimal advancePaymentAmount = advance.getAmountPaid();
      advancePaymentAmount =
          advancePaymentAmount.subtract(refundService.getRefundPaidAmount(advance));
      total = total.add(advancePaymentAmount);
    }
    return total;
  }

  @Override
  public void updateAmountToBeSpreadOverTheTimetable(PurchaseOrder purchaseOrder) {
    List<Timetable> timetableList = purchaseOrder.getTimetableList();
    BigDecimal totalHT = purchaseOrder.getExTaxTotal();
    BigDecimal sumTimetableAmount = BigDecimal.ZERO;
    if (timetableList != null) {
      for (Timetable timetable : timetableList) {
        sumTimetableAmount = sumTimetableAmount.add(timetable.getAmount());
      }
    }
    purchaseOrder.setAmountToBeSpreadOverTheTimetable(
        currencyScaleService.getScaledValue(
            totalHT.subtract(sumTimetableAmount),
            currencyScaleService.getCurrencyScale(purchaseOrder.getCurrency())));
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void requestPurchaseOrder(PurchaseOrder purchaseOrder) throws AxelorException {
    if (!appSupplychainService.isApp("supplychain")) {
      super.requestPurchaseOrder(purchaseOrder);
      return;
    }

    super.requestPurchaseOrder(purchaseOrder);
    int intercoPurchaseCreatingStatus =
        appSupplychainService.getAppSupplychain().getIntercoPurchaseCreatingStatusSelect();
    if (purchaseOrder.getInterco()
        && intercoPurchaseCreatingStatus == PurchaseOrderRepository.STATUS_REQUESTED) {
      Beans.get(IntercoService.class).generateIntercoSaleFromPurchase(purchaseOrder);
    }
    if (purchaseOrder.getCreatedByInterco()) {
      fillIntercompanySaleOrderCounterpart(purchaseOrder);
    }
  }

  /**
   * Fill interco sale order counterpart is the sale order exist.
   *
   * @param purchaseOrder
   */
  protected void fillIntercompanySaleOrderCounterpart(PurchaseOrder purchaseOrder) {
    SaleOrder saleOrder =
        Beans.get(SaleOrderRepository.class)
            .all()
            .filter("self.saleOrderSeq = :saleOrderSeq")
            .bind("saleOrderSeq", purchaseOrder.getExternalReference())
            .fetchOne();
    if (saleOrder != null) {
      saleOrder.setExternalReference(purchaseOrder.getPurchaseOrderSeq());
    }
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void updateToValidatedStatus(PurchaseOrder purchaseOrder) throws AxelorException {

    if (purchaseOrder.getStatusSelect() == null
        || purchaseOrder.getStatusSelect() != PurchaseOrderRepository.STATUS_FINISHED) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(SupplychainExceptionMessage.PURCHASE_ORDER_RETURN_TO_VALIDATE_WRONG_STATUS));
    }

    purchaseOrder.setStatusSelect(PurchaseOrderRepository.STATUS_VALIDATED);
    purchaseOrderRepo.save(purchaseOrder);
  }

  @Override
  public StockLocation getStockLocation(Partner supplierPartner, Company company)
      throws AxelorException {
    if (company == null) {
      return null;
    }
    StockLocation stockLocation =
        partnerStockSettingsService.getDefaultStockLocation(
            supplierPartner, company, StockLocation::getUsableOnPurchaseOrder);
    if (stockLocation == null) {
      StockConfig stockConfig = stockConfigService.getStockConfig(company);
      stockLocation = stockConfigService.getReceiptDefaultStockLocation(stockConfig);
    }
    return stockLocation;
  }

  @Override
  public StockLocation getFromStockLocation(Partner supplierPartner, Company company)
      throws AxelorException {
    if (company == null) {
      return null;
    }

    StockLocation fromStockLocation =
        partnerStockSettingsService.getDefaultExternalStockLocation(
            supplierPartner, company, StockLocation::getUsableOnPurchaseOrder);
    if (fromStockLocation == null) {
      StockConfig stockConfig = stockConfigService.getStockConfig(company);
      fromStockLocation = stockConfigService.getSupplierVirtualStockLocation(stockConfig);
    }
    return fromStockLocation;
  }
}
