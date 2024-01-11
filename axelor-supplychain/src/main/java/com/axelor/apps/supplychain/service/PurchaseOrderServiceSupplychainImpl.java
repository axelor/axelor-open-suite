/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2024 Axelor (<http://axelor.com>).
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

import com.axelor.apps.account.db.Budget;
import com.axelor.apps.account.db.BudgetDistribution;
import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.repo.BudgetDistributionRepository;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Currency;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.PriceList;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.TradingName;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.purchase.db.PurchaseOrder;
import com.axelor.apps.purchase.db.PurchaseOrderLine;
import com.axelor.apps.purchase.db.repo.PurchaseOrderLineRepository;
import com.axelor.apps.purchase.db.repo.PurchaseOrderRepository;
import com.axelor.apps.purchase.service.PurchaseOrderLineService;
import com.axelor.apps.purchase.service.PurchaseOrderServiceImpl;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.repo.SaleOrderRepository;
import com.axelor.apps.stock.db.ShipmentMode;
import com.axelor.apps.stock.db.StockConfig;
import com.axelor.apps.stock.db.StockLocation;
import com.axelor.apps.stock.service.PartnerStockSettingsService;
import com.axelor.apps.stock.service.config.StockConfigService;
import com.axelor.apps.supplychain.db.Timetable;
import com.axelor.apps.supplychain.exception.SupplychainExceptionMessage;
import com.axelor.apps.supplychain.service.app.AppSupplychainService;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
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
  protected BudgetSupplychainService budgetSupplychainService;
  protected PurchaseOrderLineRepository purchaseOrderLineRepository;
  protected PurchaseOrderLineService purchaseOrderLineService;
  protected PartnerStockSettingsService partnerStockSettingsService;
  protected StockConfigService stockConfigService;

  @Inject
  public PurchaseOrderServiceSupplychainImpl(
      AppSupplychainService appSupplychainService,
      AccountConfigService accountConfigService,
      AppAccountService appAccountService,
      AppBaseService appBaseService,
      PurchaseOrderStockService purchaseOrderStockService,
      BudgetSupplychainService budgetSupplychainService,
      PurchaseOrderLineRepository purchaseOrderLineRepository,
      PurchaseOrderLineService purchaseOrderLineService,
      PartnerStockSettingsService partnerStockSettingsService,
      StockConfigService stockConfigService) {

    this.appSupplychainService = appSupplychainService;
    this.accountConfigService = accountConfigService;
    this.appAccountService = appAccountService;
    this.appBaseService = appBaseService;
    this.purchaseOrderStockService = purchaseOrderStockService;
    this.budgetSupplychainService = budgetSupplychainService;
    this.purchaseOrderLineRepository = purchaseOrderLineRepository;
    this.purchaseOrderLineService = purchaseOrderLineService;
    this.partnerStockSettingsService = partnerStockSettingsService;
    this.stockConfigService = stockConfigService;
  }

  @Override
  public PurchaseOrder createPurchaseOrder(
      User buyerUser,
      Company company,
      Partner contactPartner,
      Currency currency,
      LocalDate deliveryDate,
      String internalReference,
      String externalReference,
      StockLocation stockLocation,
      LocalDate orderDate,
      PriceList priceList,
      Partner supplierPartner,
      TradingName tradingName)
      throws AxelorException {

    LOG.debug(
        "Creation of a purchase order : Company = {},  External reference = {}, Supplier = {}",
        company.getName(),
        externalReference,
        supplierPartner.getFullName());

    PurchaseOrder purchaseOrder =
        super.createPurchaseOrder(
            buyerUser,
            company,
            contactPartner,
            currency,
            deliveryDate,
            internalReference,
            externalReference,
            orderDate,
            priceList,
            supplierPartner,
            tradingName);

    purchaseOrder.setStockLocation(stockLocation);

    purchaseOrder.setPaymentMode(supplierPartner.getOutPaymentMode());
    purchaseOrder.setPaymentCondition(supplierPartner.getPaymentCondition());

    if (purchaseOrder.getPaymentMode() == null) {
      purchaseOrder.setPaymentMode(
          this.accountConfigService.getAccountConfig(company).getOutPaymentMode());
    }

    if (purchaseOrder.getPaymentCondition() == null) {
      purchaseOrder.setPaymentCondition(
          this.accountConfigService.getAccountConfig(company).getDefPaymentCondition());
    }

    purchaseOrder.setTradingName(tradingName);

    return purchaseOrder;
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
                "self.purchaseOrder.id = :purchaseOrderId AND self.operationSubTypeSelect = :operationSubTypeSelect")
            .bind("purchaseOrderId", purchaseOrder.getId())
            .bind("operationSubTypeSelect", InvoiceRepository.OPERATION_SUB_TYPE_ADVANCE)
            .fetch();
    if (advancePaymentInvoiceList == null || advancePaymentInvoiceList.isEmpty()) {
      return total;
    }
    for (Invoice advance : advancePaymentInvoiceList) {
      total = total.add(advance.getAmountPaid());
    }
    return total;
  }

  @Transactional
  @Override
  public void generateBudgetDistribution(PurchaseOrder purchaseOrder) {
    if (purchaseOrder.getPurchaseOrderLineList() != null) {
      for (PurchaseOrderLine purchaseOrderLine : purchaseOrder.getPurchaseOrderLineList()) {
        Budget budget = purchaseOrderLine.getBudget();
        if (purchaseOrder.getStatusSelect().equals(PurchaseOrderRepository.STATUS_REQUESTED)
            && budget != null
            && (purchaseOrderLine.getBudgetDistributionList() == null
                || purchaseOrderLine.getBudgetDistributionList().isEmpty())) {
          BudgetDistribution budgetDistribution = new BudgetDistribution();
          budgetDistribution.setBudget(budget);
          budgetDistribution.setAmount(purchaseOrderLine.getCompanyExTaxTotal());
          budgetDistribution.setBudgetAmountAvailable(
              budget.getTotalAmountExpected().subtract(budget.getTotalAmountCommitted()));
          purchaseOrderLine.addBudgetDistributionListItem(budgetDistribution);
        }
      }
      // purchaseOrderRepo.save(purchaseOrder);
    }
  }

  @Transactional(rollbackOn = {Exception.class})
  @Override
  public PurchaseOrder mergePurchaseOrders(
      List<PurchaseOrder> purchaseOrderList,
      Currency currency,
      Partner supplierPartner,
      Company company,
      StockLocation stockLocation,
      Partner contactPartner,
      PriceList priceList,
      TradingName tradingName)
      throws AxelorException {
    StringBuilder numSeq = new StringBuilder();
    StringBuilder externalRef = new StringBuilder();
    for (PurchaseOrder purchaseOrderLocal : purchaseOrderList) {
      if (numSeq.length() > 0) {
        numSeq.append("-");
      }
      numSeq.append(purchaseOrderLocal.getPurchaseOrderSeq());

      if (externalRef.length() > 0) {
        externalRef.append("|");
      }
      if (purchaseOrderLocal.getExternalReference() != null) {
        externalRef.append(purchaseOrderLocal.getExternalReference());
      }
    }

    PurchaseOrder purchaseOrderMerged =
        this.createPurchaseOrder(
            AuthUtils.getUser(),
            company,
            contactPartner,
            currency,
            null,
            numSeq.toString(),
            externalRef.toString(),
            stockLocation,
            appBaseService.getTodayDate(company),
            priceList,
            supplierPartner,
            tradingName);

    super.attachToNewPurchaseOrder(purchaseOrderList, purchaseOrderMerged);

    this.computePurchaseOrder(purchaseOrderMerged);

    purchaseOrderRepo.save(purchaseOrderMerged);

    super.removeOldPurchaseOrders(purchaseOrderList);

    return purchaseOrderMerged;
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
    purchaseOrder.setAmountToBeSpreadOverTheTimetable(totalHT.subtract(sumTimetableAmount));
  }

  @Transactional
  @Override
  public void applyToallBudgetDistribution(PurchaseOrder purchaseOrder) {

    for (PurchaseOrderLine purchaseOrderLine : purchaseOrder.getPurchaseOrderLineList()) {
      BudgetDistribution newBudgetDistribution = new BudgetDistribution();
      newBudgetDistribution.setAmount(purchaseOrderLine.getCompanyExTaxTotal());
      newBudgetDistribution.setBudget(purchaseOrder.getBudget());
      newBudgetDistribution.setPurchaseOrderLine(purchaseOrderLine);
      Beans.get(BudgetDistributionRepository.class).save(newBudgetDistribution);
      Beans.get(PurchaseOrderLineServiceSupplychainImpl.class)
          .computeBudgetDistributionSumAmount(purchaseOrderLine, purchaseOrder);
    }
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
  public void setPurchaseOrderLineBudget(PurchaseOrder purchaseOrder) {

    Budget budget = purchaseOrder.getBudget();
    for (PurchaseOrderLine purchaseOrderLine : purchaseOrder.getPurchaseOrderLineList()) {
      purchaseOrderLine.setBudget(budget);
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
  public String createShipmentCostLine(PurchaseOrder purchaseOrder) throws AxelorException {
    List<PurchaseOrderLine> purchaseOrderLines = purchaseOrder.getPurchaseOrderLineList();
    ShipmentMode shipmentMode = purchaseOrder.getShipmentMode();
    if (shipmentMode == null) {
      return null;
    }
    Product shippingCostProduct = shipmentMode.getShippingCostsProduct();
    if (shippingCostProduct == null) {
      return null;
    }
    if (shipmentMode.getHasCarriagePaidPossibility()) {
      BigDecimal carriagePaidThreshold = shipmentMode.getCarriagePaidThreshold();
      if (computeExTaxTotalWithoutShippingLines(purchaseOrder).compareTo(carriagePaidThreshold)
          >= 0) {
        String message = removeShipmentCostLine(purchaseOrder);
        this.computePurchaseOrder(purchaseOrder);
        return message;
      }
    }
    if (alreadyHasShippingCostLine(purchaseOrder, shippingCostProduct)) {
      return null;
    }
    PurchaseOrderLine shippingCostLine = createShippingCostLine(purchaseOrder, shippingCostProduct);
    purchaseOrderLines.add(shippingCostLine);
    this.computePurchaseOrder(purchaseOrder);
    return null;
  }

  @Override
  public PurchaseOrderLine createShippingCostLine(
      PurchaseOrder purchaseOrder, Product shippingCostProduct) throws AxelorException {
    PurchaseOrderLine shippingCostLine = new PurchaseOrderLine();
    shippingCostLine.setPurchaseOrder(purchaseOrder);
    shippingCostLine.setProduct(shippingCostProduct);
    purchaseOrderLineService.fill(shippingCostLine, purchaseOrder);
    purchaseOrderLineService.compute(shippingCostLine, purchaseOrder);
    return shippingCostLine;
  }

  @Override
  public boolean alreadyHasShippingCostLine(
      PurchaseOrder purchaseOrder, Product shippingCostProduct) {
    List<PurchaseOrderLine> purchaseOrderLines = purchaseOrder.getPurchaseOrderLineList();
    if (purchaseOrderLines == null) {
      return false;
    }
    for (PurchaseOrderLine purchaseOrderLine : purchaseOrderLines) {
      if (shippingCostProduct.equals(purchaseOrderLine.getProduct())) {
        return true;
      }
    }
    return false;
  }

  @Override
  @Transactional(rollbackOn = Exception.class)
  public String removeShipmentCostLine(PurchaseOrder purchaseOrder) {
    List<PurchaseOrderLine> purchaseOrderLines = purchaseOrder.getPurchaseOrderLineList();
    if (purchaseOrderLines == null) {
      return null;
    }
    List<PurchaseOrderLine> linesToRemove = new ArrayList<>();
    for (PurchaseOrderLine purchaseOrderLine : purchaseOrderLines) {
      if (purchaseOrderLine.getProduct() != null
          && purchaseOrderLine.getProduct().getIsShippingCostsProduct()) {
        linesToRemove.add(purchaseOrderLine);
      }
    }
    if (linesToRemove.isEmpty()) {
      return null;
    }
    for (PurchaseOrderLine lineToRemove : linesToRemove) {
      purchaseOrderLines.remove(lineToRemove);
      if (lineToRemove.getId() != null) {
        purchaseOrderLineRepository.remove(lineToRemove);
      }
    }
    purchaseOrder.setPurchaseOrderLineList(purchaseOrderLines);
    return I18n.get("Carriage paid threshold is exceeded, all shipment cost lines are removed");
  }

  @Override
  public BigDecimal computeExTaxTotalWithoutShippingLines(PurchaseOrder purchaseOrder) {
    List<PurchaseOrderLine> purchaseOrderLines = purchaseOrder.getPurchaseOrderLineList();
    if (purchaseOrderLines == null) {
      return BigDecimal.ZERO;
    }
    BigDecimal exTaxTotal = BigDecimal.ZERO;
    for (PurchaseOrderLine purchaseOrderLine : purchaseOrderLines) {
      if (purchaseOrderLine.getProduct() != null
          && !purchaseOrderLine.getProduct().getIsShippingCostsProduct()) {
        exTaxTotal = exTaxTotal.add(purchaseOrderLine.getExTaxTotal());
      }
    }
    return exTaxTotal;
  }

  @Transactional
  @Override
  public void updateBudgetDistributionAmountAvailable(PurchaseOrder purchaseOrder) {
    if (purchaseOrder.getPurchaseOrderLineList() != null) {
      for (PurchaseOrderLine purchaseOrderLine : purchaseOrder.getPurchaseOrderLineList()) {
        List<BudgetDistribution> budgetDistributionList =
            purchaseOrderLine.getBudgetDistributionList();
        Budget budget = purchaseOrderLine.getBudget();
        if (!budgetDistributionList.isEmpty() && budget != null) {
          for (BudgetDistribution budgetDistribution : budgetDistributionList) {
            budgetDistribution.setBudgetAmountAvailable(
                budget.getTotalAmountExpected().subtract(budget.getTotalAmountCommitted()));
          }
        }
      }
    }
  }

  @Override
  public boolean isGoodAmountBudgetDistribution(PurchaseOrder purchaseOrder) {
    if (purchaseOrder.getPurchaseOrderLineList() != null) {
      for (PurchaseOrderLine purchaseOrderLine : purchaseOrder.getPurchaseOrderLineList()) {
        if (purchaseOrderLine.getBudgetDistributionList() != null
            && !purchaseOrderLine.getBudgetDistributionList().isEmpty()) {
          BigDecimal budgetDistributionTotalAmount =
              purchaseOrderLine.getBudgetDistributionList().stream()
                  .map(BudgetDistribution::getAmount)
                  .reduce(BigDecimal.ZERO, BigDecimal::add);
          if (budgetDistributionTotalAmount.compareTo(purchaseOrderLine.getCompanyExTaxTotal())
              != 0) {
            return false;
          }
        }
      }
    }
    return true;
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
