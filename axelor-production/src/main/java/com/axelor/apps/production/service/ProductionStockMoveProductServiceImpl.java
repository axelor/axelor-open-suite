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
package com.axelor.apps.production.service;

import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.Unit;
import com.axelor.apps.base.db.repo.CompanyRepository;
import com.axelor.apps.base.db.repo.ProductRepository;
import com.axelor.apps.base.service.UnitConversionService;
import com.axelor.apps.production.db.repo.ManufOrderRepository;
import com.axelor.apps.production.service.app.AppProductionService;
import com.axelor.apps.stock.db.StockLocation;
import com.axelor.apps.stock.db.StockMoveLine;
import com.axelor.apps.stock.db.repo.StockLocationRepository;
import com.axelor.apps.stock.service.StockLocationService;
import com.axelor.apps.supplychain.service.StockLocationServiceSupplychain;
import com.axelor.apps.supplychain.service.StockMoveProductServiceImpl;
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
import java.util.List;
import java.util.Map;

public class ProductionStockMoveProductServiceImpl extends StockMoveProductServiceImpl {

  protected AppProductionService appProductionService;

  @Inject
  public ProductionStockMoveProductServiceImpl(
      UnitConversionService unitConversionService,
      AppSupplychainService appSupplychainService,
      ProductRepository productRepository,
      CompanyRepository companyRepository,
      StockLocationRepository stockLocationRepository,
      StockLocationService stockLocationService,
      StockLocationServiceSupplychain stockLocationServiceSupplychain,
      AppProductionService appProductionService) {
    super(
        unitConversionService,
        appSupplychainService,
        productRepository,
        companyRepository,
        stockLocationRepository,
        stockLocationService,
        stockLocationServiceSupplychain);
    this.appProductionService = appProductionService;
  }

  @Override
  public Map<String, Object> computeIndicators(Long productId, Long companyId, Long stockLocationId)
      throws AxelorException {
    Map<String, Object> map = super.computeIndicators(productId, companyId, stockLocationId);
    Product product = productRepository.find(productId);
    Company company = companyRepository.find(companyId);
    StockLocation stockLocation = stockLocationRepository.find(stockLocationId);

    BigDecimal consumeManufOrderQty = this.getConsumeManufOrderQty(product, company, stockLocation);
    BigDecimal availableQty =
        (BigDecimal) map.getOrDefault("$availableQty", BigDecimal.ZERO.setScale(2));
    map.put("$buildingQty", this.getBuildingQty(product, company, stockLocation));
    map.put("$consumeManufOrderQty", consumeManufOrderQty);
    map.put(
        "$missingManufOrderQty",
        BigDecimal.ZERO.max(consumeManufOrderQty.subtract(availableQty)).setScale(2));
    return map;
  }

  protected BigDecimal getBuildingQty(Product product, Company company, StockLocation stockLocation)
      throws AxelorException {
    if (product == null || product.getUnit() == null) {
      return BigDecimal.ZERO.setScale(2);
    }
    List<Integer> statusList = new ArrayList<>();
    statusList.add(ManufOrderRepository.STATUS_PLANNED);
    String status = appProductionService.getAppProduction().getManufOrderFilterStatusSelect();
    if (!StringUtils.isBlank(status)) {
      statusList = StringTool.getIntegerList(status);
    }

    List<Filter> queryFilter =
        Lists.newArrayList(
            new JPQLFilter(
                ""
                    + "self.product = :product "
                    + " AND self.stockMove.statusSelect IN (:statusList) "
                    + " AND self.producedManufOrder IS NOT NULL "));
    if (company != null) {
      queryFilter.add(new JPQLFilter("self.stockMove.company = :company "));
    }
    if (stockLocation != null) {
      queryFilter.add(
          new JPQLFilter(
              "self.stockMove.toStockLocation = :stockLocation "
                  + " AND self.stockMove.toStockLocation.typeSelect != :typeSelect "));
    }

    List<StockMoveLine> stockMoveLineList =
        Filter.and(queryFilter)
            .build(StockMoveLine.class)
            .bind("product", product)
            .bind("company", company)
            .bind("statusList", statusList)
            .bind("stockLocation", stockLocation)
            .bind("typeSelect", StockLocationRepository.TYPE_VIRTUAL)
            .fetch();

    BigDecimal sumBuildingQty = BigDecimal.ZERO.setScale(2);
    if (!stockMoveLineList.isEmpty()) {

      BigDecimal productBuildingQty = BigDecimal.ZERO.setScale(2);
      Unit unitConversion = product.getUnit();
      for (StockMoveLine stockMoveLine : stockMoveLineList) {
        productBuildingQty = stockMoveLine.getQty();
        if (stockMoveLine.getUnit() != unitConversion) {
          unitConversionService.convert(
              stockMoveLine.getUnit(),
              unitConversion,
              productBuildingQty,
              productBuildingQty.scale(),
              product);
        }
      }
      sumBuildingQty = sumBuildingQty.add(productBuildingQty);
    }
    return sumBuildingQty;
  }

  protected BigDecimal getConsumeManufOrderQty(
      Product product, Company company, StockLocation stockLocation) throws AxelorException {
    if (product == null || product.getUnit() == null) {
      return BigDecimal.ZERO.setScale(2);
    }
    List<Integer> statusList = new ArrayList<>();
    statusList.add(ManufOrderRepository.STATUS_PLANNED);
    String status = appProductionService.getAppProduction().getManufOrderFilterStatusSelect();
    if (!StringUtils.isBlank(status)) {
      statusList = StringTool.getIntegerList(status);
    }
    List<Filter> queryFilter =
        Lists.newArrayList(
            new JPQLFilter(
                ""
                    + "self.product = :product "
                    + " AND self.stockMove.statusSelect IN (:statusList) "
                    + " AND (self.consumedManufOrder IS NOT NULL OR self.consumedOperationOrder IS NOT NULL) "));
    if (company != null) {
      queryFilter.add(new JPQLFilter("self.stockMove.company = :company "));
    }
    if (stockLocation != null) {
      queryFilter.add(
          new JPQLFilter(
              "self.stockMove.fromStockLocation = :stockLocation "
                  + "AND self.stockMove.fromStockLocation.typeSelect != :typeSelect "));
    }

    List<StockMoveLine> stockMoveLineList =
        Filter.and(queryFilter)
            .build(StockMoveLine.class)
            .bind("product", product)
            .bind("company", company)
            .bind("statusList", statusList)
            .bind("stockLocation", stockLocation)
            .bind("typeSelect", StockLocationRepository.TYPE_VIRTUAL)
            .fetch();
    BigDecimal sumConsumeManufOrderQty = BigDecimal.ZERO.setScale(2);
    if (!stockMoveLineList.isEmpty()) {
      BigDecimal productConsumeManufOrderQty = BigDecimal.ZERO.setScale(2);
      Unit unitConversion = product.getUnit();
      for (StockMoveLine stockMoveLine : stockMoveLineList) {
        productConsumeManufOrderQty = stockMoveLine.getQty();
        if (stockMoveLine.getUnit() != unitConversion) {
          unitConversionService.convert(
              stockMoveLine.getUnit(),
              unitConversion,
              productConsumeManufOrderQty,
              productConsumeManufOrderQty.scale(),
              product);
        }
      }
      sumConsumeManufOrderQty = sumConsumeManufOrderQty.add(productConsumeManufOrderQty);
    }
    return sumConsumeManufOrderQty;
  }
}
