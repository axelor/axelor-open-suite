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
import com.axelor.apps.stock.db.repo.StockMoveRepository;
import com.axelor.apps.stock.service.StockLocationService;
import com.axelor.apps.supplychain.service.ProductStockLocationServiceImpl;
import com.axelor.apps.supplychain.service.StockLocationServiceSupplychain;
import com.axelor.apps.supplychain.service.app.AppSupplychainService;
import com.axelor.apps.tool.StringTool;
import com.axelor.common.StringUtils;
import com.axelor.exception.AxelorException;
import com.axelor.rpc.filter.Filter;
import com.axelor.rpc.filter.JPQLFilter;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ProductionProductStockLocationServiceImpl extends ProductStockLocationServiceImpl {

  protected AppProductionService appProductionService;

  @Inject
  public ProductionProductStockLocationServiceImpl(
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

    if(stockLocationId != 0L) {
	    List<StockLocation> stockLocationList = stockLocationService.getAllLocationAndSubLocation(stockLocation, false);
	    if(!stockLocationList.isEmpty()) {
	    	BigDecimal consumeManufOrderQty = BigDecimal.ZERO;
	    	BigDecimal buildingQty = BigDecimal.ZERO;
	    	 BigDecimal availableQty =
	    		        (BigDecimal) map.getOrDefault("$availableQty", BigDecimal.ZERO.setScale(2));
	    	
	    	 for(StockLocation sl : stockLocationList) {
	    		 consumeManufOrderQty = consumeManufOrderQty.add( this.getConsumeManufOrderQty(product, company, sl));
	    		 buildingQty = buildingQty.add(this.getBuildingQty(product, company, sl));
	    	 }
	    	 map.put("$consumeManufOrderQty", consumeManufOrderQty.setScale(2));
	    	 map.put("$buildingQty", buildingQty.setScale(2));
	    	 map.put("$missingManufOrderQty",
	    		        BigDecimal.ZERO.max(consumeManufOrderQty.subtract(availableQty)).setScale(2));
	    	return map;
	    }
    }

    BigDecimal consumeManufOrderQty =
        this.getConsumeManufOrderQty(product, company, stockLocation).setScale(2);
    BigDecimal availableQty =
        (BigDecimal) map.getOrDefault("$availableQty", BigDecimal.ZERO.setScale(2));
    map.put("$buildingQty", this.getBuildingQty(product, company, stockLocation).setScale(2));
    map.put("$consumeManufOrderQty", consumeManufOrderQty);
    map.put(
        "$missingManufOrderQty",
        BigDecimal.ZERO.max(consumeManufOrderQty.subtract(availableQty)).setScale(2));
    return map;
  }

  protected BigDecimal getBuildingQty(Product product, Company company, StockLocation stockLocation)
      throws AxelorException {
    if (product == null || product.getUnit() == null) {
      return BigDecimal.ZERO;
    }
    List<Integer> statusList = new ArrayList<>();
    statusList.add(ManufOrderRepository.STATUS_PLANNED);
    String status = appProductionService.getAppProduction().getmOFilterOnStockDetailStatusSelect();
    if (!StringUtils.isBlank(status)) {
      statusList = StringTool.getIntegerList(status);
    }

    List<Filter> queryFilter =
        Lists.newArrayList(
            new JPQLFilter(
                "self.product = :product "
                    + " AND self.stockMove.statusSelect = :stockMoveStatus "
                    + " AND self.stockMove.estimatedDate IS NOT NULL "
                    + " AND self.stockMove.estimatedDate >= :localDate "
                    + " AND self.stockMove.toStockLocation.typeSelect != :typeSelect "
                    + " AND self.producedManufOrder IS NOT NULL "
                    + " AND self.producedManufOrder.statusSelect IN (:statusListManufOrder)"));
    if (company != null) {
      queryFilter.add(new JPQLFilter("self.stockMove.company = :company "));
    }
    if (stockLocation != null) {
      queryFilter.add(new JPQLFilter("self.stockMove.toStockLocation = :stockLocation "));
    }

    List<StockMoveLine> stockMoveLineList =
        Filter.and(queryFilter)
            .build(StockMoveLine.class)
            .bind("product", product)
            .bind("company", company)
            .bind("statusListManufOrder", statusList)
            .bind("stockLocation", stockLocation)
            .bind("localDate", LocalDate.now())
            .bind("stockMoveStatus", StockMoveRepository.STATUS_PLANNED)
            .bind("typeSelect", StockLocationRepository.TYPE_VIRTUAL)
            .fetch();

    BigDecimal sumBuildingQty = BigDecimal.ZERO;
    if (!stockMoveLineList.isEmpty()) {

      BigDecimal productBuildingQty = BigDecimal.ZERO;
      Unit unitConversion = product.getUnit();
      for (StockMoveLine stockMoveLine : stockMoveLineList) {
        productBuildingQty = stockMoveLine.getQty();
        if (!stockMoveLine.getUnit().equals(unitConversion)) {
          unitConversionService.convert(
              stockMoveLine.getUnit(),
              unitConversion,
              productBuildingQty,
              productBuildingQty.scale(),
              product);
        }
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
    List<Integer> statusList = new ArrayList<>();
    statusList.add(ManufOrderRepository.STATUS_PLANNED);
    String status = appProductionService.getAppProduction().getmOFilterOnStockDetailStatusSelect();
    if (!StringUtils.isBlank(status)) {
      statusList = StringTool.getIntegerList(status);
    }
    List<Filter> queryFilter =
        Lists.newArrayList(
            new JPQLFilter(
                "self.product = :product "
                    + " AND self.stockMove.statusSelect = :stockMoveStatus "
                    + " AND self.stockMove.fromStockLocation.typeSelect != :typeSelect "
                    + " AND self.stockMove.estimatedDate IS NOT NULL "
                    + " AND self.stockMove.estimatedDate >= :localDate "
                    + " AND ( (self.consumedManufOrder IS NOT NULL AND self.consumedManufOrder.statusSelect IN (:statusListManufOrder))"
                    + " OR (self.consumedOperationOrder IS NOT NULL AND self.consumedOperationOrder.statusSelect IN (:statusListManufOrder) ) ) "));
    if (company != null) {
      queryFilter.add(new JPQLFilter("self.stockMove.company = :company "));
    }
    if (stockLocation != null) {
      queryFilter.add(new JPQLFilter("self.stockMove.fromStockLocation = :stockLocation "));
    }

    List<StockMoveLine> stockMoveLineList =
        Filter.and(queryFilter)
            .build(StockMoveLine.class)
            .bind("product", product)
            .bind("company", company)
            .bind("stockMoveStatus", StockMoveRepository.STATUS_PLANNED)
            .bind("statusListManufOrder", statusList)
            .bind("stockLocation", stockLocation)
            .bind("localDate", LocalDate.now())
            .bind("typeSelect", StockLocationRepository.TYPE_VIRTUAL)
            .fetch();
    BigDecimal sumConsumeManufOrderQty = BigDecimal.ZERO;
    if (!stockMoveLineList.isEmpty()) {
      BigDecimal productConsumeManufOrderQty = BigDecimal.ZERO;
      Unit unitConversion = product.getUnit();
      for (StockMoveLine stockMoveLine : stockMoveLineList) {
        productConsumeManufOrderQty = stockMoveLine.getQty();
        if (!stockMoveLine.getUnit().equals(unitConversion)) {
          unitConversionService.convert(
              stockMoveLine.getUnit(),
              unitConversion,
              productConsumeManufOrderQty,
              productConsumeManufOrderQty.scale(),
              product);
        }
        sumConsumeManufOrderQty = sumConsumeManufOrderQty.add(productConsumeManufOrderQty);
      }
    }
    return sumConsumeManufOrderQty;
  }
}
