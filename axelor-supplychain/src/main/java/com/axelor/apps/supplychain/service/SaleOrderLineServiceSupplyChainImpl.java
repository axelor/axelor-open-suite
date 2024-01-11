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

import com.axelor.apps.account.db.AccountConfig;
import com.axelor.apps.account.db.AnalyticDistributionTemplate;
import com.axelor.apps.account.db.AnalyticMoveLine;
import com.axelor.apps.account.db.repo.AccountConfigRepository;
import com.axelor.apps.account.db.repo.AnalyticMoveLineRepository;
import com.axelor.apps.account.db.repo.InvoiceLineRepository;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.account.service.analytic.AnalyticMoveLineService;
import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.service.CurrencyService;
import com.axelor.apps.base.service.PriceListService;
import com.axelor.apps.base.service.ProductMultipleQtyService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.base.service.pricing.PricingService;
import com.axelor.apps.base.service.tax.AccountManagementService;
import com.axelor.apps.base.service.tax.TaxService;
import com.axelor.apps.purchase.db.SupplierCatalog;
import com.axelor.apps.purchase.service.app.AppPurchaseService;
import com.axelor.apps.sale.db.PackLine;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.db.repo.SaleOrderLineRepository;
import com.axelor.apps.sale.db.repo.SaleOrderRepository;
import com.axelor.apps.sale.service.app.AppSaleService;
import com.axelor.apps.sale.service.saleorder.SaleOrderLineServiceImpl;
import com.axelor.apps.sale.service.saleorder.SaleOrderMarginService;
import com.axelor.apps.sale.service.saleorder.SaleOrderService;
import com.axelor.apps.stock.db.StockLocation;
import com.axelor.apps.stock.db.StockLocationLine;
import com.axelor.apps.stock.db.repo.StockLocationRepository;
import com.axelor.apps.stock.db.repo.StockMoveLineRepository;
import com.axelor.apps.stock.db.repo.StockMoveRepository;
import com.axelor.apps.stock.service.StockLocationLineService;
import com.axelor.apps.stock.service.StockLocationService;
import com.axelor.apps.supplychain.db.SupplyChainConfig;
import com.axelor.apps.supplychain.db.repo.SupplyChainConfigRepository;
import com.axelor.apps.supplychain.service.app.AppSupplychainService;
import com.axelor.apps.supplychain.service.config.SupplyChainConfigService;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.axelor.common.ObjectUtils;
import com.axelor.common.StringUtils;
import com.axelor.db.JPA;
import com.axelor.inject.Beans;
import com.axelor.utils.StringTool;
import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.persistence.TypedQuery;

public class SaleOrderLineServiceSupplyChainImpl extends SaleOrderLineServiceImpl
    implements SaleOrderLineServiceSupplyChain {

  protected AppAccountService appAccountService;
  protected AnalyticMoveLineService analyticMoveLineService;
  protected AppSupplychainService appSupplychainService;
  protected AccountConfigService accountConfigService;
  protected InvoiceLineRepository invoiceLineRepository;
  protected SaleInvoicingStateService saleInvoicingStateService;

  @Inject
  public SaleOrderLineServiceSupplyChainImpl(
      CurrencyService currencyService,
      PriceListService priceListService,
      ProductMultipleQtyService productMultipleQtyService,
      AppBaseService appBaseService,
      AppSaleService appSaleService,
      AccountManagementService accountManagementService,
      SaleOrderLineRepository saleOrderLineRepo,
      SaleOrderService saleOrderService,
      AppAccountService appAccountService,
      AnalyticMoveLineService analyticMoveLineService,
      AppSupplychainService appSupplychainService,
      AccountConfigService accountConfigService,
      PricingService pricingService,
      TaxService taxService,
      SaleOrderMarginService saleOrderMarginService,
      InvoiceLineRepository invoiceLineRepository,
      SaleInvoicingStateService saleInvoicingStateService) {
    super(
        currencyService,
        priceListService,
        productMultipleQtyService,
        appBaseService,
        appSaleService,
        accountManagementService,
        saleOrderLineRepo,
        saleOrderService,
        pricingService,
        taxService,
        saleOrderMarginService);
    this.appAccountService = appAccountService;
    this.analyticMoveLineService = analyticMoveLineService;
    this.appSupplychainService = appSupplychainService;
    this.accountConfigService = accountConfigService;
    this.invoiceLineRepository = invoiceLineRepository;
    this.saleInvoicingStateService = saleInvoicingStateService;
  }

  @Override
  public void computeProductInformation(SaleOrderLine saleOrderLine, SaleOrder saleOrder)
      throws AxelorException {
    super.computeProductInformation(saleOrderLine, saleOrder);
    saleOrderLine.setSaleSupplySelect(saleOrderLine.getProduct().getSaleSupplySelect());

    if (appAccountService.isApp("supplychain")) {
      saleOrderLine.setSaleSupplySelect(saleOrderLine.getProduct().getSaleSupplySelect());

      this.getAndComputeAnalyticDistribution(saleOrderLine, saleOrder);
    }
  }

  public SaleOrderLine getAndComputeAnalyticDistribution(
      SaleOrderLine saleOrderLine, SaleOrder saleOrder) throws AxelorException {

    AccountConfig accountConfig = accountConfigService.getAccountConfig(saleOrder.getCompany());

    if (!accountConfig.getManageAnalyticAccounting()
        || accountConfig.getAnalyticDistributionTypeSelect()
            == AccountConfigRepository.DISTRIBUTION_TYPE_FREE) {
      return saleOrderLine;
    }

    AnalyticDistributionTemplate analyticDistributionTemplate =
        analyticMoveLineService.getAnalyticDistributionTemplate(
            saleOrder.getClientPartner(),
            saleOrderLine.getProduct(),
            saleOrder.getCompany(),
            false);

    saleOrderLine.setAnalyticDistributionTemplate(analyticDistributionTemplate);

    if (saleOrderLine.getAnalyticMoveLineList() != null) {
      saleOrderLine.getAnalyticMoveLineList().clear();
    }

    this.computeAnalyticDistribution(saleOrderLine);

    return saleOrderLine;
  }

  @Override
  public SaleOrderLine computeAnalyticDistribution(SaleOrderLine saleOrderLine) {

    List<AnalyticMoveLine> analyticMoveLineList = saleOrderLine.getAnalyticMoveLineList();

    if ((analyticMoveLineList == null || analyticMoveLineList.isEmpty())) {
      createAnalyticDistributionWithTemplate(saleOrderLine);
    }
    if (analyticMoveLineList != null) {
      LocalDate date =
          appAccountService.getTodayDate(
              saleOrderLine.getSaleOrder() != null
                  ? saleOrderLine.getSaleOrder().getCompany()
                  : Optional.ofNullable(AuthUtils.getUser())
                      .map(User::getActiveCompany)
                      .orElse(null));
      for (AnalyticMoveLine analyticMoveLine : analyticMoveLineList) {
        analyticMoveLineService.updateAnalyticMoveLine(
            analyticMoveLine, saleOrderLine.getCompanyExTaxTotal(), date);
      }
    }
    return saleOrderLine;
  }

  public SaleOrderLine createAnalyticDistributionWithTemplate(SaleOrderLine saleOrderLine) {
    List<AnalyticMoveLine> analyticMoveLineList =
        analyticMoveLineService.generateLines(
            saleOrderLine.getAnalyticDistributionTemplate(),
            saleOrderLine.getCompanyExTaxTotal(),
            AnalyticMoveLineRepository.STATUS_FORECAST_ORDER,
            appAccountService.getTodayDate(
                saleOrderLine.getSaleOrder() != null
                    ? saleOrderLine.getSaleOrder().getCompany()
                    : Optional.ofNullable(AuthUtils.getUser())
                        .map(User::getActiveCompany)
                        .orElse(null)));

    if (ObjectUtils.isEmpty(analyticMoveLineList)) {
      saleOrderLine.clearAnalyticMoveLineList();
    } else {
      analyticMoveLineList.forEach(saleOrderLine::addAnalyticMoveLineListItem);
    }
    return saleOrderLine;
  }

  @Override
  public int getSaleOrderLineInvoicingState(SaleOrderLine saleOrderLine) {
    return saleInvoicingStateService.getInvoicingState(
        saleOrderLine.getAmountInvoiced(),
        saleOrderLine.getExTaxTotal(),
        atLeastOneInvoiceVentilated(saleOrderLine));
  }

  protected boolean atLeastOneInvoiceVentilated(SaleOrderLine saleOrderLine) {
    return invoiceLineRepository
            .all()
            .filter(
                "self.saleOrderLine = :saleOrderLine AND self.invoice.statusSelect = :statusSelect")
            .bind("saleOrderLine", saleOrderLine.getId())
            .bind("statusSelect", InvoiceRepository.STATUS_VENTILATED)
            .count()
        > 0;
  }

  @Override
  public BigDecimal getAvailableStock(SaleOrder saleOrder, SaleOrderLine saleOrderLine) {

    if (!appAccountService.isApp("supplychain")) {
      return super.getAvailableStock(saleOrder, saleOrderLine);
    }

    StockLocationLine stockLocationLine =
        Beans.get(StockLocationLineService.class)
            .getStockLocationLine(saleOrder.getStockLocation(), saleOrderLine.getProduct());

    if (stockLocationLine == null) {
      return BigDecimal.ZERO;
    }
    return stockLocationLine.getCurrentQty().subtract(stockLocationLine.getReservedQty());
  }

  @Override
  public BigDecimal getAllocatedStock(SaleOrder saleOrder, SaleOrderLine saleOrderLine) {

    if (!appAccountService.isApp("supplychain")) {
      return super.getAllocatedStock(saleOrder, saleOrderLine);
    }

    StockLocationLine stockLocationLine =
        Beans.get(StockLocationLineService.class)
            .getStockLocationLine(saleOrder.getStockLocation(), saleOrderLine.getProduct());

    if (stockLocationLine == null) {
      return BigDecimal.ZERO;
    }
    return stockLocationLine.getReservedQty();
  }

  @Override
  public BigDecimal computeUndeliveredQty(SaleOrderLine saleOrderLine) {
    Preconditions.checkNotNull(saleOrderLine);

    BigDecimal undeliveryQty = saleOrderLine.getQty().subtract(saleOrderLine.getDeliveredQty());

    if (undeliveryQty.signum() > 0) {
      return undeliveryQty;
    }
    return BigDecimal.ZERO;
  }

  @Override
  public List<Long> getSupplierPartnerList(SaleOrderLine saleOrderLine) {
    Product product = saleOrderLine.getProduct();
    if (!Beans.get(AppPurchaseService.class).getAppPurchase().getManageSupplierCatalog()
        || product == null
        || product.getSupplierCatalogList() == null) {
      return new ArrayList<>();
    }
    return product.getSupplierCatalogList().stream()
        .map(SupplierCatalog::getSupplierPartner)
        .filter(Objects::nonNull)
        .map(Partner::getId)
        .collect(Collectors.toList());
  }

  @Override
  public void updateDeliveryStates(List<SaleOrderLine> saleOrderLineList) {
    if (ObjectUtils.isEmpty(saleOrderLineList)) {
      return;
    }

    for (SaleOrderLine saleOrderLine : saleOrderLineList) {
      updateDeliveryState(saleOrderLine);
    }
  }

  @Override
  public void updateDeliveryState(SaleOrderLine saleOrderLine) {
    if (saleOrderLine.getDeliveredQty().signum() == 0) {
      saleOrderLine.setDeliveryState(SaleOrderLineRepository.DELIVERY_STATE_NOT_DELIVERED);
    } else if (saleOrderLine.getDeliveredQty().compareTo(saleOrderLine.getQty()) < 0) {
      saleOrderLine.setDeliveryState(SaleOrderLineRepository.DELIVERY_STATE_PARTIALLY_DELIVERED);
    } else {
      saleOrderLine.setDeliveryState(SaleOrderLineRepository.DELIVERY_STATE_DELIVERED);
    }
  }

  @Override
  public String getSaleOrderLineListForAProduct(
      Long productId, Long companyId, Long stockLocationId) {
    List<Integer> statusList = new ArrayList<>();
    statusList.add(SaleOrderRepository.STATUS_ORDER_CONFIRMED);
    String status =
        appSupplychainService.getAppSupplychain().getsOFilterOnStockDetailStatusSelect();
    if (!StringUtils.isBlank(status)) {
      statusList = StringTool.getIntegerList(status);
    }
    String statusListQuery =
        statusList.stream().map(String::valueOf).collect(Collectors.joining(","));
    String query =
        "self.product.id = "
            + productId
            + " AND self.deliveryState != "
            + SaleOrderLineRepository.DELIVERY_STATE_DELIVERED
            + " AND self.saleOrder.statusSelect IN ("
            + statusListQuery
            + ")";

    if (companyId != 0L) {
      query += " AND self.saleOrder.company.id = " + companyId;
      if (stockLocationId != 0L) {
        StockLocation stockLocation =
            Beans.get(StockLocationRepository.class).find(stockLocationId);
        List<StockLocation> stockLocationList =
            Beans.get(StockLocationService.class)
                .getAllLocationAndSubLocation(stockLocation, false);
        if (!stockLocationList.isEmpty() && stockLocation.getCompany().getId().equals(companyId)) {
          query +=
              " AND self.saleOrder.stockLocation.id IN ("
                  + StringTool.getIdListString(stockLocationList)
                  + ") ";
        }
      }
    }
    return query;
  }

  @Override
  public BigDecimal checkInvoicedOrDeliveredOrderQty(SaleOrderLine saleOrderLine) {
    BigDecimal qty = saleOrderLine.getQty();
    BigDecimal deliveredQty = saleOrderLine.getDeliveredQty();
    BigDecimal invoicedQty = getInvoicedQty(saleOrderLine);

    if (qty.compareTo(invoicedQty) < 0 && invoicedQty.compareTo(deliveredQty) > 0) {
      return invoicedQty;
    } else if (deliveredQty.compareTo(BigDecimal.ZERO) > 0 && qty.compareTo(deliveredQty) < 0) {
      return deliveredQty;
    }

    return qty;
  }

  @Transactional(rollbackOn = {Exception.class})
  public void updateStockMoveReservationDateTime(SaleOrderLine saleOrderLine)
      throws AxelorException {
    SaleOrder saleOrder = saleOrderLine.getSaleOrder();
    if (saleOrder == null) {
      return;
    }
    if (SupplyChainConfigRepository.SALE_ORDER_SHIPPING_DATE
        != Beans.get(SupplyChainConfigService.class)
            .getSupplyChainConfig(saleOrder.getCompany())
            .getSaleOrderReservationDateSelect()) {
      return;
    }

    Beans.get(StockMoveLineRepository.class)
        .all()
        .filter("self.saleOrderLine = :saleOrderLineId")
        .bind("saleOrderLineId", saleOrderLine.getId())
        .fetchStream()
        .filter(
            stockMoveLine ->
                stockMoveLine.getStockMove() != null
                    && stockMoveLine.getStockMove().getStatusSelect()
                        == StockMoveRepository.STATUS_PLANNED)
        .forEach(
            stockMoveLine ->
                stockMoveLine.setReservationDateTime(
                    saleOrderLine.getEstimatedShippingDate().atStartOfDay()));
  }

  @Override
  public SaleOrderLine updateProductQty(
      SaleOrderLine saleOrderLine, SaleOrder saleOrder, BigDecimal oldQty, BigDecimal newQty)
      throws AxelorException {
    BigDecimal qty = saleOrderLine.getQty();
    qty =
        qty.divide(oldQty, appBaseService.getNbDecimalDigitForQty(), RoundingMode.HALF_EVEN)
            .multiply(newQty)
            .setScale(appBaseService.getNbDecimalDigitForQty(), RoundingMode.HALF_EVEN);
    saleOrderLine.setQty(qty);

    if (appSupplychainService.isApp("supplychain")
        && saleOrder.getStatusSelect() == SaleOrderRepository.STATUS_ORDER_CONFIRMED) {
      qty = this.checkInvoicedOrDeliveredOrderQty(saleOrderLine);
      saleOrderLine.setQty(qty);
    }

    saleOrderLine = super.updateProductQty(saleOrderLine, saleOrder, oldQty, newQty);
    if (!appSupplychainService.isApp("supplychain")
        || saleOrderLine.getTypeSelect() != SaleOrderLineRepository.TYPE_NORMAL) {
      return saleOrderLine;
    }
    if (appAccountService.getAppAccount().getManageAnalyticAccounting()) {
      this.computeAnalyticDistribution(saleOrderLine);
    }
    if (appSupplychainService.getAppSupplychain().getManageStockReservation()
        && (saleOrderLine.getRequestedReservedQty().compareTo(qty) > 0
            || saleOrderLine.getIsQtyRequested())) {
      saleOrderLine.setRequestedReservedQty(BigDecimal.ZERO.max(qty));
    }
    return saleOrderLine;
  }

  @Override
  public SaleOrderLine createSaleOrderLine(
      PackLine packLine,
      SaleOrder saleOrder,
      BigDecimal packQty,
      BigDecimal conversionRate,
      Integer sequence)
      throws AxelorException {

    SaleOrderLine soLine =
        super.createSaleOrderLine(packLine, saleOrder, packQty, conversionRate, sequence);

    if (soLine != null && soLine.getProduct() != null) {
      soLine.setSaleSupplySelect(soLine.getProduct().getSaleSupplySelect());
      getAndComputeAnalyticDistribution(soLine, saleOrder);
      if (ObjectUtils.notEmpty(soLine.getAnalyticMoveLineList())) {
        soLine.getAnalyticMoveLineList().stream()
            .forEach(analyticMoveLine -> analyticMoveLine.setSaleOrderLine(soLine));
      }
      try {
        SupplyChainConfig supplyChainConfig =
            Beans.get(SupplyChainConfigService.class).getSupplyChainConfig(saleOrder.getCompany());
        if (supplyChainConfig.getAutoRequestReservedQty()) {
          Beans.get(ReservedQtyService.class).requestQty(soLine);
        }
      } catch (AxelorException e) {
        TraceBackService.trace(e);
      }
    }
    return soLine;
  }

  protected BigDecimal getInvoicedQty(SaleOrderLine saleOrderLine) {

    TypedQuery<BigDecimal> query =
        JPA.em()
            .createQuery(
                "SELECT COALESCE(SUM(CASE WHEN self.invoice.operationTypeSelect = 3 THEN self.qty WHEN self.invoice.operationTypeSelect = 4 THEN -self.qty END),0) FROM InvoiceLine self WHERE self.invoice.statusSelect = :statusSelect AND self.saleOrderLine.id = :saleOrderLineId",
                BigDecimal.class);
    query.setParameter("statusSelect", InvoiceRepository.STATUS_VENTILATED);
    query.setParameter("saleOrderLineId", saleOrderLine.getId());

    return query.getSingleResult();
  }
}
