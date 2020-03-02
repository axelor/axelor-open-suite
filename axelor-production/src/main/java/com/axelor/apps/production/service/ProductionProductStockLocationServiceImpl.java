/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2020 Axelor (<http://axelor.com>).
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
package com.axelor.apps.production.service;

import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.Unit;
import com.axelor.apps.base.db.repo.CompanyRepository;
import com.axelor.apps.base.db.repo.ProductRepository;
import com.axelor.apps.base.service.UnitConversionService;
import com.axelor.apps.production.service.app.AppProductionService;
import com.axelor.apps.production.service.manuforder.ManufOrderService;
import com.axelor.apps.stock.db.StockLocation;
import com.axelor.apps.stock.db.StockMoveLine;
import com.axelor.apps.stock.db.repo.StockLocationLineRepository;
import com.axelor.apps.stock.db.repo.StockLocationRepository;
import com.axelor.apps.stock.db.repo.StockMoveLineRepository;
import com.axelor.apps.stock.service.StockLocationLineService;
import com.axelor.apps.stock.service.StockLocationService;
import com.axelor.apps.supplychain.service.ProductStockLocationServiceImpl;
import com.axelor.apps.supplychain.service.StockLocationServiceSupplychain;
import com.axelor.apps.supplychain.service.app.AppSupplychainService;
import com.axelor.exception.AxelorException;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;

public class ProductionProductStockLocationServiceImpl extends ProductStockLocationServiceImpl {

  protected AppProductionService appProductionService;
  protected ManufOrderService manufOrderService;
  protected StockMoveLineRepository stockMoveLineRepository;

  @Inject
  public ProductionProductStockLocationServiceImpl(
      UnitConversionService unitConversionService,
      AppSupplychainService appSupplychainService,
      ProductRepository productRepository,
      CompanyRepository companyRepository,
      StockLocationRepository stockLocationRepository,
      StockLocationService stockLocationService,
      StockLocationServiceSupplychain stockLocationServiceSupplychain,
      StockLocationLineService stockLocationLineService,
      StockLocationLineRepository stockLocationLineRepository,
      AppProductionService appProductionService,
      ManufOrderService manufOrderService,
      StockMoveLineRepository stockMoveLineRepository,AppBaseService appBaseService) {
    super(
        unitConversionService,
        appSupplychainService,
        productRepository,
        companyRepository,
        stockLocationRepository,
        stockLocationService,
        stockLocationServiceSupplychain,
        stockLocationLineService,
        stockLocationLineRepository,appBaseService);
    this.appProductionService = appProductionService;
    this.manufOrderService = manufOrderService;
    this.stockMoveLineRepository = stockMoveLineRepository;
  }

  @Override
  public Map<String, Object> computeIndicators(Long productId, Long companyId, Long stockLocationId)
      throws AxelorException {
    Map<String, Object> map = super.computeIndicators(productId, companyId, stockLocationId);
    Product product = productRepository.find(productId);
    Company company = companyRepository.find(companyId);
    StockLocation stockLocation = stockLocationRepository.find(stockLocationId);
    int scale = appBaseService.getNbDecimalDigitForQty();
    BigDecimal consumeManufOrderQty =
        this.getConsumeManufOrderQty(product, company, stockLocation).setScale(scale, RoundingMode.HALF_UP);
    BigDecimal availableQty =
        (BigDecimal) map.getOrDefault("$availableQty", BigDecimal.ZERO.setScale(scale, RoundingMode.HALF_UP));
    map.put("$buildingQty", this.getBuildingQty(product, company, stockLocation).setScale(scale, RoundingMode.HALF_UP));
    map.put("$consumeManufOrderQty", consumeManufOrderQty);
    map.put(
        "$missingManufOrderQty",
        BigDecimal.ZERO.max(consumeManufOrderQty.subtract(availableQty)).setScale(scale, RoundingMode.HALF_UP));
    return map;
  }

  protected BigDecimal getBuildingQty(Product product, Company company, StockLocation stockLocation)
      throws AxelorException {
    if (product == null || product.getUnit() == null) {
      return BigDecimal.ZERO;
    }
    Long companyId = 0L;
    Long stockLocationId = 0L;
    if (company != null) {
      companyId = company.getId();
      if (stockLocation != null) {
        stockLocationId = stockLocation.getId();
      }
    }

    String query =
        manufOrderService.getBuildingQtyForAProduct(product.getId(), companyId, stockLocationId);
    List<StockMoveLine> stockMoveLineList = stockMoveLineRepository.all().filter(query).fetch();

    BigDecimal sumBuildingQty = BigDecimal.ZERO;
    if (!stockMoveLineList.isEmpty()) {

      Unit unitConversion = product.getUnit();
      for (StockMoveLine stockMoveLine : stockMoveLineList) {
        BigDecimal productBuildingQty = stockMoveLine.getRealQty();
        unitConversionService.convert(
            stockMoveLine.getUnit(),
            unitConversion,
            productBuildingQty,
            productBuildingQty.scale(),
            product);
        sumBuildingQty = sumBuildingQty.add(productBuildingQty);
      }
    }
    return sumBuildingQty;
  }

  protected BigDecimal getConsumeManufOrderQty(
      Product product, Company company, StockLocation stockLocation) throws AxelorException {
    if (product == null || product.getUnit() == null) {
      return BigDecimal.ZERO;
    }
    Long companyId = 0L;
    Long stockLocationId = 0L;
    if (company != null) {
      companyId = company.getId();
      if (stockLocation != null) {
        stockLocationId = stockLocation.getId();
      }
    }
    String query =
        manufOrderService.getConsumeAndMissingQtyForAProduct(
            product.getId(), companyId, stockLocationId);
    List<StockMoveLine> stockMoveLineList = stockMoveLineRepository.all().filter(query).fetch();

    BigDecimal sumConsumeManufOrderQty = BigDecimal.ZERO;
    if (!stockMoveLineList.isEmpty()) {
      Unit unitConversion = product.getUnit();
      for (StockMoveLine stockMoveLine : stockMoveLineList) {
        BigDecimal productConsumeManufOrderQty = stockMoveLine.getRealQty();
        unitConversionService.convert(
            stockMoveLine.getUnit(),
            unitConversion,
            productConsumeManufOrderQty,
            productConsumeManufOrderQty.scale(),
            product);
        sumConsumeManufOrderQty = sumConsumeManufOrderQty.add(productConsumeManufOrderQty);
      }
    }
    return sumConsumeManufOrderQty;
  }

  protected BigDecimal getMissingManufOrderQty(
      Product product, Company company, StockLocation stockLocation) throws AxelorException {
    if (product == null || product.getUnit() == null) {
      return BigDecimal.ZERO;
    }
    Long companyId = 0L;
    Long stockLocationId = 0L;
    if (company != null) {
      companyId = company.getId();
      if (stockLocation != null) {
        stockLocationId = stockLocation.getId();
      }
    }
    String query =
        manufOrderService.getConsumeAndMissingQtyForAProduct(
            product.getId(), companyId, stockLocationId);
    List<StockMoveLine> stockMoveLineList = stockMoveLineRepository.all().filter(query).fetch();

    BigDecimal sumMissingManufOrderQty = BigDecimal.ZERO;
    if (!stockMoveLineList.isEmpty()) {
      Unit unitConversion = product.getUnit();
      for (StockMoveLine stockMoveLine : stockMoveLineList) {
        BigDecimal productMissingManufOrderQty = getMissingQtyOfStockMoveLine(stockMoveLine);
        unitConversionService.convert(
            stockMoveLine.getUnit(),
            unitConversion,
            productMissingManufOrderQty,
            productMissingManufOrderQty.scale(),
            product);
        sumMissingManufOrderQty = sumMissingManufOrderQty.add(productMissingManufOrderQty);
      }
    }
    return sumMissingManufOrderQty;
  }

  protected BigDecimal getMissingQtyOfStockMoveLine(StockMoveLine stockMoveLine) {
    if (stockMoveLine.getProduct() != null) {
      BigDecimal availableQty = stockMoveLine.getAvailableQty();
      BigDecimal availableQtyForProduct = stockMoveLine.getAvailableQtyForProduct();
      BigDecimal realQty = stockMoveLine.getRealQty();

      if (availableQty.compareTo(realQty) >= 0 || !stockMoveLine.getProduct().getStockManaged()) {
        return BigDecimal.ZERO;
      } else if (availableQtyForProduct.compareTo(realQty) >= 0) {
        return BigDecimal.ZERO;
      } else if (availableQty.compareTo(realQty) < 0
          && availableQtyForProduct.compareTo(realQty) < 0) {
        if (stockMoveLine.getProduct().getTrackingNumberConfiguration() != null) {
          return availableQtyForProduct.subtract(realQty);
        } else {
          return availableQty.subtract(realQty);
        }
      }
    }
    return BigDecimal.ZERO;
  }
}
