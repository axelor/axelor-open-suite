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
import com.axelor.apps.sale.db.SaleOrderLine;
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

public class StockMoveProductServiceImpl implements StockMoveProductService {

  protected UnitConversionService unitConversionService;
  protected AppSupplychainService appSupplychainService;
  protected ProductRepository productRepository;
  protected CompanyRepository companyRepository;
  protected StockLocationRepository stockLocationRepository;
  protected StockLocationService stockLocationService;
  protected StockLocationServiceSupplychain stockLocationServiceSupplychain;

  @Inject
  public StockMoveProductServiceImpl(
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

    map.put("$realQty", stockLocationService.getRealQty(productId, stockLocationId));
    map.put("$futureQty", stockLocationService.getFutureQty(productId, stockLocationId));
    map.put(
        "$reservedQty", stockLocationServiceSupplychain.getReservedQty(productId, stockLocationId));
    map.put("$requestedReservedQty", this.getRequestedReservedQty(product, company, stockLocation));
    map.put("$saleOrderQty", this.getSaleOrderQty(product, company, stockLocation));
    map.put("$purchaseOrderQty", this.getPurchaseOrderQty(product, company, stockLocation));
    map.put("$availableQty", this.getAvailableQty(product, company, stockLocation));
    return map;
  }

  protected BigDecimal getRequestedReservedQty(
      Product product, Company company, StockLocation stockLocation) throws AxelorException {
    if (product == null || product.getUnit() == null) {
      return BigDecimal.ZERO.setScale(2);
    }
    // JPQL request
    List<Filter> queryFilter = Lists.newArrayList(new JPQLFilter("" + "self.product = :product"));
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
            .bind("stockLocation", stockLocation)
            .fetch();

    // Compute
    BigDecimal sumRequestedReservedQty = BigDecimal.ZERO.setScale(2);
    if (!stockLocationLineList.isEmpty()) {
      BigDecimal requestedReservedQty = BigDecimal.ZERO.setScale(2);
      Unit unitConversion = product.getUnit();

      for (StockLocationLine stockLocationLine : stockLocationLineList) {
        requestedReservedQty = stockLocationLine.getRequestedReservedQty();
        if (stockLocationLine.getUnit() != unitConversion) {
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
      return BigDecimal.ZERO.setScale(2);
    }
    List<Integer> statusList = new ArrayList<>();
    statusList.add(SaleOrderRepository.STATUS_ORDER_CONFIRMED);
    String status = appSupplychainService.getAppSupplychain().getSaleOrderFilterStatusSelect();
    if (!StringUtils.isBlank(status)) {
      statusList = StringTool.getIntegerList(status);
    }
    // JPQL request
    List<Filter> queryFilter =
        Lists.newArrayList(
            new JPQLFilter(
                ""
                    + "self.product = :product"
                    + " AND self.saleOrder.statusSelect IN (:statusList) "));
    if (company != null) {
      queryFilter.add(new JPQLFilter("self.saleOrder.company = :company "));
    }
    if (stockLocation != null) {
      queryFilter.add(new JPQLFilter("self.saleOrder.stockLocation = :stockLocation"));
    }

    List<SaleOrderLine> saleOrderLineList =
        Filter.and(queryFilter)
            .build(SaleOrderLine.class)
            .bind("product", product)
            .bind("statusList", statusList)
            .bind("company", company)
            .bind("stockLocation", stockLocation)
            .fetch();

    // Compute
    BigDecimal sumSaleOrderQty = BigDecimal.ZERO.setScale(2);
    if (!saleOrderLineList.isEmpty()) {
      BigDecimal productSaleOrderQty = BigDecimal.ZERO.setScale(2);
      Unit unitConversion = product.getUnit();

      for (SaleOrderLine saleOrderLine : saleOrderLineList) {
        productSaleOrderQty = saleOrderLine.getQty();
        if (saleOrderLine.getUnit() != unitConversion) {
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
      return BigDecimal.ZERO.setScale(2);
    }
    List<Integer> statusList = new ArrayList<>();
    statusList.add(IPurchaseOrder.STATUS_VALIDATED);
    String status = appSupplychainService.getAppSupplychain().getPurchaseOrderFilterStatusSelect();
    if (!StringUtils.isBlank(status)) {
      statusList = StringTool.getIntegerList(status);
    }
    // JPQL request
    List<Filter> queryFilter =
        Lists.newArrayList(
            new JPQLFilter(
                ""
                    + "self.product = :product"
                    + " AND self.purchaseOrder.statusSelect IN (:statusList) "));
    if (company != null) {
      queryFilter.add(new JPQLFilter("self.purchaseOrder.company = :company "));
    }
    if (stockLocation != null) {
      queryFilter.add(new JPQLFilter("self.purchaseOrder.stockLocation = :stockLocation"));
    }

    List<PurchaseOrderLine> purchaseOrderLineList =
        Filter.and(queryFilter)
            .build(PurchaseOrderLine.class)
            .bind("product", product)
            .bind("statusList", statusList)
            .bind("company", company)
            .bind("stockLocation", stockLocation)
            .fetch();

    // Compute
    BigDecimal sumPurchaseOrderQty = BigDecimal.ZERO.setScale(2);
    if (!purchaseOrderLineList.isEmpty()) {
      BigDecimal productPurchaseOrderQty = BigDecimal.ZERO.setScale(2);
      Unit unitConversion = product.getUnit();

      for (PurchaseOrderLine purchaseOrderLine : purchaseOrderLineList) {
        productPurchaseOrderQty = purchaseOrderLine.getQty();
        if (purchaseOrderLine.getUnit() != unitConversion) {
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
      return BigDecimal.ZERO.setScale(2);
    }
    List<Filter> queryFilter =
        Lists.newArrayList(
            new JPQLFilter(
                "self.product = :product " + "AND self.stockLocation.typeSelect != :typeSelect "));
    if (company != null) {
      queryFilter.add(new JPQLFilter("self.stockLocation.company = :company "));
    }
    if (stockLocation != null) {
      queryFilter.add(
          new JPQLFilter(
              "self.stockLocation = :stockLocation "
                  + "AND self.stockLocation.isInCalculStock = true"));
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
    BigDecimal sumAvailableQty = BigDecimal.ZERO.setScale(2);
    if (!stockLocationLineList.isEmpty()) {

      BigDecimal productAvailableQty = BigDecimal.ZERO.setScale(2);
      Unit unitConversion = product.getUnit();
      for (StockLocationLine stockLocationLine : stockLocationLineList) {
        productAvailableQty = stockLocationLine.getCurrentQty();
        if (stockLocationLine.getUnit() != unitConversion) {
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
