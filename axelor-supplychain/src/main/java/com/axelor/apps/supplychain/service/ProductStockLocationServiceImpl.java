/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2019 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or  modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.axelor.apps.supplychain.service;

import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.Unit;
import com.axelor.apps.base.db.repo.CompanyRepository;
import com.axelor.apps.base.db.repo.ProductRepository;
import com.axelor.apps.base.service.UnitConversionService;
import com.axelor.apps.purchase.db.IPurchaseOrder;
import com.axelor.apps.purchase.db.PurchaseOrderLine;
import com.axelor.apps.purchase.db.repo.PurchaseOrderLineRepository;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.db.repo.SaleOrderLineRepository;
import com.axelor.apps.sale.db.repo.SaleOrderRepository;
import com.axelor.apps.stock.db.StockLocation;
import com.axelor.apps.stock.db.StockLocationLine;
import com.axelor.apps.stock.db.repo.StockLocationRepository;
import com.axelor.apps.stock.service.StockLocationService;
import com.axelor.apps.supplychain.service.app.AppSupplychainService;
import com.axelor.apps.tool.StringTool;
import com.axelor.common.StringUtils;
import com.axelor.exception.AxelorException;
import com.axelor.rpc.filter.Filter;
import com.axelor.rpc.filter.JPQLFilter;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProductStockLocationServiceImpl implements ProductStockLocationService {

  protected UnitConversionService unitConversionService;
  protected AppSupplychainService appSupplychainService;
  protected ProductRepository productRepository;
  protected CompanyRepository companyRepository;
  protected StockLocationRepository stockLocationRepository;
  protected StockLocationService stockLocationService;
  protected StockLocationServiceSupplychain stockLocationServiceSupplychain;

  @Inject
  public ProductStockLocationServiceImpl(
      UnitConversionService unitConversionService,
      AppSupplychainService appSupplychainService,
      ProductRepository productRepository,
      CompanyRepository companyRepository,
      StockLocationRepository stockLocationRepository,
      StockLocationService stockLocationService,
      StockLocationServiceSupplychain stockLocationServiceSupplychain) {
    super();
    this.unitConversionService = unitConversionService;
    this.appSupplychainService = appSupplychainService;
    this.productRepository = productRepository;
    this.companyRepository = companyRepository;
    this.stockLocationRepository = stockLocationRepository;
    this.stockLocationService = stockLocationService;
    this.stockLocationServiceSupplychain = stockLocationServiceSupplychain;
  }

  @Override
  public Map<String, Object> computeIndicators(Long productId, Long companyId, Long stockLocationId)
      throws AxelorException {
    Map<String, Object> map = new HashMap<>();
    Product product = productRepository.find(productId);
    Company company = companyRepository.find(companyId);
    StockLocation stockLocation = stockLocationRepository.find(stockLocationId);
    if (stockLocationId != 0L) {
      List<StockLocation> stockLocationList =
          stockLocationService.getAllLocationAndSubLocation(stockLocation, false);
      if (!stockLocationList.isEmpty()) {
        BigDecimal realQty = BigDecimal.ZERO;
        BigDecimal futureQty = BigDecimal.ZERO;
        BigDecimal reservedQty = BigDecimal.ZERO;
        BigDecimal requestedReservedQty = BigDecimal.ZERO;
        BigDecimal saleOrderQty = BigDecimal.ZERO;
        BigDecimal purchaseOrderQty = BigDecimal.ZERO;
        BigDecimal availableQty = BigDecimal.ZERO;

        for (StockLocation sl : stockLocationList) {
          realQty = realQty.add(stockLocationService.getRealQty(productId, sl.getId(), companyId));
          futureQty =
              futureQty.add(stockLocationService.getFutureQty(productId, sl.getId(), companyId));
          reservedQty =
              reservedQty.add(
                  stockLocationServiceSupplychain.getReservedQty(productId, sl.getId(), companyId));
          requestedReservedQty =
              requestedReservedQty.add(this.getRequestedReservedQty(product, company, sl));
          saleOrderQty = saleOrderQty.add(this.getSaleOrderQty(product, company, sl));
          purchaseOrderQty = purchaseOrderQty.add(this.getPurchaseOrderQty(product, company, sl));
          availableQty = availableQty.add(this.getAvailableQty(product, company, sl));
        }
        map.put("$realQty", realQty.setScale(2));
        map.put("$futureQty", futureQty.setScale(2));
        map.put("$reservedQty", reservedQty.setScale(2));
        map.put("$requestedReservedQty", requestedReservedQty.setScale(2));
        map.put("$saleOrderQty", saleOrderQty.setScale(2));
        map.put("$purchaseOrderQty", purchaseOrderQty.setScale(2));
        map.put("$availableQty", availableQty.subtract(reservedQty).setScale(2));

        return map;
      }
    }
    BigDecimal reservedQty =
        stockLocationServiceSupplychain
            .getReservedQty(productId, stockLocationId, companyId)
            .setScale(2);
    map.put(
        "$realQty",
        stockLocationService.getRealQty(productId, stockLocationId, companyId).setScale(2));
    map.put(
        "$futureQty",
        stockLocationService.getFutureQty(productId, stockLocationId, companyId).setScale(2));
    map.put("$reservedQty", reservedQty);
    map.put(
        "$requestedReservedQty",
        this.getRequestedReservedQty(product, company, stockLocation).setScale(2));
    map.put("$saleOrderQty", this.getSaleOrderQty(product, company, stockLocation).setScale(2));
    map.put(
        "$purchaseOrderQty", this.getPurchaseOrderQty(product, company, stockLocation).setScale(2));
    map.put(
        "$availableQty",
        this.getAvailableQty(product, company, stockLocation).subtract(reservedQty).setScale(2));
    return map;
  }

  protected BigDecimal getRequestedReservedQty(
      Product product, Company company, StockLocation stockLocation) throws AxelorException {
    if (product == null || product.getUnit() == null) {
      return BigDecimal.ZERO;
    }
    // JPQL request
    List<Filter> queryFilter =
        Lists.newArrayList(
            new JPQLFilter(
                "self.product = :product AND self.stockLocation.typeSelect != :typeSelect "));
    if (company != null) {
      queryFilter.add(new JPQLFilter("self.stockLocation.company = :company "));
    }
    if (stockLocation != null) {
      queryFilter.add(new JPQLFilter("self.stockLocation = :stockLocation"));
    }

    List<StockLocationLine> stockLocationLineList =
        Filter.and(queryFilter)
            .build(StockLocationLine.class)
            .bind("product", product)
            .bind("company", company)
            .bind("typeSelect", StockLocationRepository.TYPE_VIRTUAL)
            .bind("stockLocation", stockLocation)
            .fetch();

    // Compute
    BigDecimal sumRequestedReservedQty = BigDecimal.ZERO;
    if (!stockLocationLineList.isEmpty()) {
      BigDecimal requestedReservedQty = BigDecimal.ZERO;
      Unit unitConversion = product.getUnit();

      for (StockLocationLine stockLocationLine : stockLocationLineList) {
        requestedReservedQty = stockLocationLine.getRequestedReservedQty();
        if (!stockLocationLine.getUnit().equals(unitConversion)) {
          requestedReservedQty =
              unitConversionService.convert(
                  stockLocationLine.getUnit(),
                  unitConversion,
                  requestedReservedQty,
                  requestedReservedQty.scale(),
                  product);
        }
        sumRequestedReservedQty = sumRequestedReservedQty.add(requestedReservedQty);
      }
    }

    return sumRequestedReservedQty;
  }

  protected BigDecimal getSaleOrderQty(
      Product product, Company company, StockLocation stockLocation) throws AxelorException {
    if (product == null || product.getUnit() == null) {
      return BigDecimal.ZERO;
    }
    List<Integer> statusList = new ArrayList<>();
    statusList.add(SaleOrderRepository.STATUS_ORDER_CONFIRMED);
    String status =
        appSupplychainService.getAppSupplychain().getsOFilterOnStockDetailStatusSelect();
    if (!StringUtils.isBlank(status)) {
      statusList = StringTool.getIntegerList(status);
    }
    // JPQL request
    List<Filter> queryFilter =
        Lists.newArrayList(
            new JPQLFilter(
                "self.product = :product"
                    + " AND self.saleOrder.statusSelect IN (:statusList) "
                    + " AND self.deliveryState != :deliveryStateSaleOrder"));
    if (company != null) {
      queryFilter.add(new JPQLFilter("self.saleOrder.company = :company "));
    }
    if (stockLocation != null) {
      queryFilter.add(new JPQLFilter("self.saleOrder.stockLocation = :stockLocation"));
    }

    List<SaleOrderLine> saleOrderLineList =
        Filter.and(queryFilter)
            .build(SaleOrderLine.class)
            .bind("deliveryStateSaleOrder", SaleOrderLineRepository.DELIVERY_STATE_DELIVERED)
            .bind("product", product)
            .bind("statusList", statusList)
            .bind("company", company)
            .bind("stockLocation", stockLocation)
            .fetch();

    // Compute
    BigDecimal sumSaleOrderQty = BigDecimal.ZERO;
    if (!saleOrderLineList.isEmpty()) {
      BigDecimal productSaleOrderQty = BigDecimal.ZERO;
      Unit unitConversion = product.getUnit();

      for (SaleOrderLine saleOrderLine : saleOrderLineList) {
        productSaleOrderQty = saleOrderLine.getQty();
        if (saleOrderLine.getDeliveryState()
            == SaleOrderLineRepository.DELIVERY_STATE_PARTIALLY_DELIVERED) {
          productSaleOrderQty = productSaleOrderQty.subtract(saleOrderLine.getDeliveredQty());
        }
        if (!saleOrderLine.getUnit().equals(unitConversion)) {
          productSaleOrderQty =
              unitConversionService.convert(
                  saleOrderLine.getUnit(),
                  unitConversion,
                  productSaleOrderQty,
                  productSaleOrderQty.scale(),
                  product);
        }
        sumSaleOrderQty = sumSaleOrderQty.add(productSaleOrderQty);
      }
    }

    return sumSaleOrderQty;
  }

  protected BigDecimal getPurchaseOrderQty(
      Product product, Company company, StockLocation stockLocation) throws AxelorException {
    if (product == null || product.getUnit() == null) {
      return BigDecimal.ZERO;
    }
    List<Integer> statusList = new ArrayList<>();
    statusList.add(IPurchaseOrder.STATUS_VALIDATED);
    String status =
        appSupplychainService.getAppSupplychain().getpOFilterOnStockDetailStatusSelect();
    if (!StringUtils.isBlank(status)) {
      statusList = StringTool.getIntegerList(status);
    }
    // JPQL request
    List<Filter> queryFilter =
        Lists.newArrayList(
            new JPQLFilter(
                "self.product = :product"
                    + " AND self.purchaseOrder.statusSelect IN (:statusList) "
                    + " AND self.purchaseOrder.orderDate IS NOT NULL "
                    + " AND self.receiptState != :receiptStatePurchaseOrder "));
    if (company != null) {
      queryFilter.add(new JPQLFilter("self.purchaseOrder.company = :company"));
    }
    if (stockLocation != null) {
      queryFilter.add(new JPQLFilter("self.purchaseOrder.stockLocation = :stockLocation"));
    }

    List<PurchaseOrderLine> purchaseOrderLineList =
        Filter.and(queryFilter)
            .build(PurchaseOrderLine.class)
            .bind("receiptStatePurchaseOrder", PurchaseOrderLineRepository.RECEIPT_STATE_RECEIVED)
            .bind("product", product)
            .bind("statusList", statusList)
            .bind("company", company)
            .bind("stockLocation", stockLocation)
            .fetch();

    // Compute
    BigDecimal sumPurchaseOrderQty = BigDecimal.ZERO;
    if (!purchaseOrderLineList.isEmpty()) {
      BigDecimal productPurchaseOrderQty = BigDecimal.ZERO;
      Unit unitConversion = product.getUnit();

      for (PurchaseOrderLine purchaseOrderLine : purchaseOrderLineList) {
        productPurchaseOrderQty = purchaseOrderLine.getQty();
        if (purchaseOrderLine.getReceiptState()
            == PurchaseOrderLineRepository.RECEIPT_STATE_PARTIALLY_RECEIVED) {
          productPurchaseOrderQty =
              productPurchaseOrderQty.subtract(purchaseOrderLine.getReceivedQty());
        }
        if (!purchaseOrderLine.getUnit().equals(unitConversion)) {
          productPurchaseOrderQty =
              unitConversionService.convert(
                  purchaseOrderLine.getUnit(),
                  unitConversion,
                  productPurchaseOrderQty,
                  productPurchaseOrderQty.scale(),
                  product);
        }
        sumPurchaseOrderQty = sumPurchaseOrderQty.add(productPurchaseOrderQty);
      }
    }
    return sumPurchaseOrderQty;
  }

  protected BigDecimal getAvailableQty(
      Product product, Company company, StockLocation stockLocation) throws AxelorException {
    if (product == null || product.getUnit() == null) {
      return BigDecimal.ZERO;
    }
    List<Filter> queryFilter =
        Lists.newArrayList(
            new JPQLFilter(
                "self.product = :product AND self.stockLocation.typeSelect != :typeSelect "
                    + " AND (self.stockLocation.isNotInCalculStock = false OR self.stockLocation.isNotInCalculStock IS NULL)"));
    if (company != null) {
      queryFilter.add(new JPQLFilter("self.stockLocation.company = :company "));
    }
    if (stockLocation != null) {
      queryFilter.add(new JPQLFilter("self.stockLocation = :stockLocation "));
    }
    List<StockLocationLine> stockLocationLineList =
        Filter.and(queryFilter)
            .build(StockLocationLine.class)
            .bind("product", product)
            .bind("typeSelect", StockLocationRepository.TYPE_VIRTUAL)
            .bind("company", company)
            .bind("stockLocation", stockLocation)
            .fetch();

    // Compute
    BigDecimal sumAvailableQty = BigDecimal.ZERO;
    if (!stockLocationLineList.isEmpty()) {

      BigDecimal productAvailableQty = BigDecimal.ZERO;
      Unit unitConversion = product.getUnit();
      for (StockLocationLine stockLocationLine : stockLocationLineList) {
        productAvailableQty = stockLocationLine.getCurrentQty();
        if (!stockLocationLine.getUnit().equals(unitConversion)) {
          unitConversionService.convert(
              stockLocationLine.getUnit(),
              unitConversion,
              productAvailableQty,
              productAvailableQty.scale(),
              product);
        }
        sumAvailableQty = sumAvailableQty.add(productAvailableQty);
      }
    }
    return sumAvailableQty;
  }
}
