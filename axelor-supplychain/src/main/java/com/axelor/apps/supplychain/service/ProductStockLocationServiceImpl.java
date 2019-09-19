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
import com.axelor.apps.purchase.db.PurchaseOrderLine;
import com.axelor.apps.purchase.db.repo.PurchaseOrderLineRepository;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.db.repo.SaleOrderLineRepository;
import com.axelor.apps.stock.db.StockLocation;
import com.axelor.apps.stock.db.StockLocationLine;
import com.axelor.apps.stock.db.repo.StockLocationLineRepository;
import com.axelor.apps.stock.db.repo.StockLocationRepository;
import com.axelor.apps.stock.service.StockLocationLineService;
import com.axelor.apps.stock.service.StockLocationService;
import com.axelor.apps.supplychain.service.app.AppSupplychainService;
import com.axelor.exception.AxelorException;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import java.math.BigDecimal;
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
  protected StockLocationLineService stockLocationLineService;
  protected StockLocationLineRepository stockLocationLineRepository;
  protected StockLocationServiceSupplychain stockLocationServiceSupplychain;

  @Inject
  public ProductStockLocationServiceImpl(
      UnitConversionService unitConversionService,
      AppSupplychainService appSupplychainService,
      ProductRepository productRepository,
      CompanyRepository companyRepository,
      StockLocationRepository stockLocationRepository,
      StockLocationService stockLocationService,
      StockLocationServiceSupplychain stockLocationServiceSupplychain,
      StockLocationLineService stockLocationLineService,
      StockLocationLineRepository stockLocationLineRepository) {
    super();
    this.unitConversionService = unitConversionService;
    this.appSupplychainService = appSupplychainService;
    this.productRepository = productRepository;
    this.companyRepository = companyRepository;
    this.stockLocationRepository = stockLocationRepository;
    this.stockLocationService = stockLocationService;
    this.stockLocationServiceSupplychain = stockLocationServiceSupplychain;
    this.stockLocationLineService = stockLocationLineService;
    this.stockLocationLineRepository = stockLocationLineRepository;
  }

  @Override
  public Map<String, Object> computeIndicators(Long productId, Long companyId, Long stockLocationId)
      throws AxelorException {
    Map<String, Object> map = new HashMap<>();
    Product product = productRepository.find(productId);
    Company company = companyRepository.find(companyId);
    StockLocation stockLocation = stockLocationRepository.find(stockLocationId);
    if (stockLocationId != 0L && companyId != 0L) {
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

        saleOrderQty = this.getSaleOrderQty(product, company, stockLocation);
        purchaseOrderQty = this.getPurchaseOrderQty(product, company, stockLocation);
        availableQty = this.getAvailableQty(product, company, stockLocation);
        requestedReservedQty = this.getRequestedReservedQty(product, company, stockLocation);

        for (StockLocation sl : stockLocationList) {
          realQty = realQty.add(stockLocationService.getRealQty(productId, sl.getId(), companyId));
          futureQty =
              futureQty.add(stockLocationService.getFutureQty(productId, sl.getId(), companyId));
          reservedQty =
              reservedQty.add(
                  stockLocationServiceSupplychain.getReservedQty(productId, sl.getId(), companyId));
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
        "$requestedReservedQty", this.getRequestedReservedQty(product, company, null).setScale(2));
    map.put("$saleOrderQty", this.getSaleOrderQty(product, company, null).setScale(2));
    map.put("$purchaseOrderQty", this.getPurchaseOrderQty(product, company, null).setScale(2));
    map.put(
        "$availableQty",
        this.getAvailableQty(product, company, null).subtract(reservedQty).setScale(2));
    return map;
  }

  protected BigDecimal getRequestedReservedQty(
      Product product, Company company, StockLocation stockLocation) throws AxelorException {
    if (product == null || product.getUnit() == null) {
      return BigDecimal.ZERO;
    }
    Long companyId = 0L;
    Long stockLocationId = 0L;
    if (company != null) {
      companyId = company.getId();
    }
    if (stockLocation != null) {
      stockLocationId = stockLocation.getId();
    }
    String query =
        stockLocationLineService.getStockLocationLineListForAProduct(
            product.getId(), companyId, stockLocationId);

    List<StockLocationLine> stockLocationLineList =
        stockLocationLineRepository.all().filter(query).fetch();

    // Compute
    BigDecimal sumRequestedReservedQty = BigDecimal.ZERO;
    if (!stockLocationLineList.isEmpty()) {
      Unit unitConversion = product.getUnit();

      for (StockLocationLine stockLocationLine : stockLocationLineList) {
        BigDecimal requestedReservedQty = stockLocationLine.getRequestedReservedQty();
        requestedReservedQty =
            unitConversionService.convert(
                stockLocationLine.getUnit(),
                unitConversion,
                requestedReservedQty,
                requestedReservedQty.scale(),
                product);
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
    Long companyId = 0L;
    Long stockLocationId = 0L;
    if (company != null) {
      companyId = company.getId();
    }
    if (stockLocation != null) {
      stockLocationId = stockLocation.getId();
    }
    String query =
        Beans.get(SaleOrderLineServiceSupplyChain.class)
            .getSaleOrderLineListForAProduct(product.getId(), companyId, stockLocationId);
    List<SaleOrderLine> saleOrderLineList =
        Beans.get(SaleOrderLineRepository.class).all().filter(query).fetch();

    // Compute
    BigDecimal sumSaleOrderQty = BigDecimal.ZERO;
    if (!saleOrderLineList.isEmpty()) {
      Unit unitConversion = product.getUnit();

      for (SaleOrderLine saleOrderLine : saleOrderLineList) {
        BigDecimal productSaleOrderQty = saleOrderLine.getQty();
        if (saleOrderLine.getDeliveryState()
            == SaleOrderLineRepository.DELIVERY_STATE_PARTIALLY_DELIVERED) {
          productSaleOrderQty = productSaleOrderQty.subtract(saleOrderLine.getDeliveredQty());
        }
        productSaleOrderQty =
            unitConversionService.convert(
                saleOrderLine.getUnit(),
                unitConversion,
                productSaleOrderQty,
                productSaleOrderQty.scale(),
                product);
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

    Long companyId = 0L;
    Long stockLocationId = 0L;
    if (company != null) {
      companyId = company.getId();
    }
    if (stockLocation != null) {
      stockLocationId = stockLocation.getId();
    }
    String query =
        Beans.get(PurchaseOrderStockService.class)
            .getPurchaseOrderLineListForAProduct(product.getId(), companyId, stockLocationId);

    List<PurchaseOrderLine> purchaseOrderLineList =
        Beans.get(PurchaseOrderLineRepository.class).all().filter(query).fetch();

    // Compute
    BigDecimal sumPurchaseOrderQty = BigDecimal.ZERO;
    if (!purchaseOrderLineList.isEmpty()) {
      Unit unitConversion = product.getUnit();

      for (PurchaseOrderLine purchaseOrderLine : purchaseOrderLineList) {
        BigDecimal productPurchaseOrderQty = purchaseOrderLine.getQty();
        if (purchaseOrderLine.getReceiptState()
            == PurchaseOrderLineRepository.RECEIPT_STATE_PARTIALLY_RECEIVED) {
          productPurchaseOrderQty =
              productPurchaseOrderQty.subtract(purchaseOrderLine.getReceivedQty());
        }
        productPurchaseOrderQty =
            unitConversionService.convert(
                purchaseOrderLine.getUnit(),
                unitConversion,
                productPurchaseOrderQty,
                productPurchaseOrderQty.scale(),
                product);
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
    Long companyId = 0L;
    Long stockLocationId = 0L;
    if (company != null) {
      companyId = company.getId();
    }
    if (stockLocation != null) {
      stockLocationId = stockLocation.getId();
    }
    String query =
        stockLocationLineService.getAvailableStockForAProduct(
            product.getId(), companyId, stockLocationId);
    List<StockLocationLine> stockLocationLineList =
        stockLocationLineRepository.all().filter(query).fetch();

    // Compute
    BigDecimal sumAvailableQty = BigDecimal.ZERO;
    if (!stockLocationLineList.isEmpty()) {

      Unit unitConversion = product.getUnit();
      for (StockLocationLine stockLocationLine : stockLocationLineList) {
        BigDecimal productAvailableQty = stockLocationLine.getCurrentQty();
        unitConversionService.convert(
            stockLocationLine.getUnit(),
            unitConversion,
            productAvailableQty,
            productAvailableQty.scale(),
            product);
        sumAvailableQty = sumAvailableQty.add(productAvailableQty);
      }
    }
    return sumAvailableQty;
  }
}
