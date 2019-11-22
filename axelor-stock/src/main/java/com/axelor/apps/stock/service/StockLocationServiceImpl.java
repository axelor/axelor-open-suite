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
package com.axelor.apps.stock.service;

import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.Unit;
import com.axelor.apps.base.db.repo.ProductRepository;
import com.axelor.apps.base.service.UnitConversionService;
import com.axelor.apps.stock.db.StockConfig;
import com.axelor.apps.stock.db.StockLocation;
import com.axelor.apps.stock.db.StockLocationLine;
import com.axelor.apps.stock.db.StockRules;
import com.axelor.apps.stock.db.repo.StockLocationLineRepository;
import com.axelor.apps.stock.db.repo.StockLocationRepository;
import com.axelor.apps.stock.db.repo.StockRulesRepository;
import com.axelor.apps.stock.service.config.StockConfigService;
import com.axelor.db.JPA;
import com.axelor.exception.AxelorException;
import com.axelor.inject.Beans;
import com.axelor.rpc.filter.Filter;
import com.axelor.rpc.filter.JPQLFilter;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.servlet.RequestScoped;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.persistence.Query;

@RequestScoped
public class StockLocationServiceImpl implements StockLocationService {

  protected StockLocationRepository stockLocationRepo;

  protected StockLocationLineService stockLocationLineService;

  protected ProductRepository productRepo;

  protected Set<Long> locationIdSet = new HashSet<>();

  @Inject
  public StockLocationServiceImpl(
      StockLocationRepository stockLocationRepo,
      StockLocationLineService stockLocationLineService,
      ProductRepository productRepo) {
    this.stockLocationRepo = stockLocationRepo;
    this.stockLocationLineService = stockLocationLineService;
    this.productRepo = productRepo;
  }

  @Override
  public StockLocation getDefaultReceiptStockLocation(Company company) {
    try {
      StockConfigService stockConfigService = Beans.get(StockConfigService.class);
      StockConfig stockConfig = stockConfigService.getStockConfig(company);
      return stockConfigService.getReceiptDefaultStockLocation(stockConfig);
    } catch (Exception e) {
      return null;
    }
  }

  @Override
  public StockLocation getPickupDefaultStockLocation(Company company) {
    try {
      StockConfigService stockConfigService = Beans.get(StockConfigService.class);
      StockConfig stockConfig = stockConfigService.getStockConfig(company);
      return stockConfigService.getPickupDefaultStockLocation(stockConfig);
    } catch (Exception e) {
      return null;
    }
  }

  protected List<StockLocation> getNonVirtualStockLocations(Long companyId) {
    List<Filter> queryFilter =
        Lists.newArrayList(new JPQLFilter("self.typeSelect != :stockLocationTypSelect"));
    if (companyId != null && companyId != 0L) {
      queryFilter.add(new JPQLFilter("self.company.id = :companyId "));
    }
    return Filter.and(queryFilter)
        .build(StockLocation.class)
        .bind("stockLocationTypSelect", StockLocationRepository.TYPE_VIRTUAL)
        .bind("companyId", companyId)
        .fetch();
  }

  @Override
  public BigDecimal getQty(Long productId, Long locationId, Long companyId, String qtyType)
      throws AxelorException {
    if (productId != null) {
      Product product = productRepo.find(productId);
      Unit productUnit = product.getUnit();
      UnitConversionService unitConversionService = Beans.get(UnitConversionService.class);

      if (locationId == null || locationId == 0L) {
        List<StockLocation> stockLocations = getNonVirtualStockLocations(companyId);
        if (!stockLocations.isEmpty()) {
          BigDecimal qty = BigDecimal.ZERO;
          for (StockLocation stockLocation : stockLocations) {
            StockLocationLine stockLocationLine =
                stockLocationLineService.getOrCreateStockLocationLine(
                    stockLocationRepo.find(stockLocation.getId()), productRepo.find(productId));

            if (stockLocationLine != null) {
              Unit stockLocationLineUnit = stockLocationLine.getUnit();
              qty =
                  qty.add(
                      qtyType.equals("real")
                          ? stockLocationLine.getCurrentQty()
                          : stockLocationLine.getFutureQty());

              if (productUnit != null && !productUnit.equals(stockLocationLineUnit)) {
                qty =
                    unitConversionService.convert(
                        stockLocationLineUnit, productUnit, qty, qty.scale(), product);
              }
            }
          }
          return qty;
        }
      } else {
        StockLocationLine stockLocationLine =
            stockLocationLineService.getOrCreateStockLocationLine(
                stockLocationRepo.find(locationId), productRepo.find(productId));

        if (stockLocationLine != null) {
          Unit stockLocationLineUnit = stockLocationLine.getUnit();
          BigDecimal qty = BigDecimal.ZERO;

          qty =
              qtyType.equals("real")
                  ? stockLocationLine.getCurrentQty()
                  : stockLocationLine.getFutureQty();

          if (productUnit != null && !productUnit.equals(stockLocationLineUnit)) {
            qty =
                unitConversionService.convert(
                    stockLocationLineUnit, productUnit, qty, qty.scale(), product);
          }
          return qty;
        }
      }
    }

    return BigDecimal.ZERO;
  }

  @Override
  public BigDecimal getRealQty(Long productId, Long locationId, Long companyId)
      throws AxelorException {
    return getQty(productId, locationId, companyId, "real");
  }

  @Override
  public BigDecimal getFutureQty(Long productId, Long locationId, Long companyId)
      throws AxelorException {
    return getQty(productId, locationId, companyId, "future");
  }

  public List<Long> getBadStockLocationLineId() {

    List<StockLocationLine> stockLocationLineList =
        Beans.get(StockLocationLineRepository.class)
            .all()
            .filter("self.stockLocation.typeSelect = 1 OR self.stockLocation.typeSelect = 2")
            .fetch();

    List<Long> idList = new ArrayList<>();

    StockRulesRepository stockRulesRepository = Beans.get(StockRulesRepository.class);

    for (StockLocationLine stockLocationLine : stockLocationLineList) {
      StockRules stockRules =
          stockRulesRepository
              .all()
              .filter(
                  "self.stockLocation = ?1 AND self.product = ?2",
                  stockLocationLine.getStockLocation(),
                  stockLocationLine.getProduct())
              .fetchOne();
      if (stockRules != null
          && stockLocationLine.getFutureQty().compareTo(stockRules.getMinQty()) < 0) {
        idList.add(stockLocationLine.getId());
      }
    }

    if (idList.isEmpty()) {
      idList.add(0L);
    }

    return idList;
  }

  @Override
  public Set<Long> getContentStockLocationIds(StockLocation stockLocation) {
    locationIdSet = new HashSet<>();
    if (stockLocation != null) {
      List<StockLocation> stockLocations = getAllLocationAndSubLocation(stockLocation, true);
      for (StockLocation item : stockLocations) {
        locationIdSet.add(item.getId());
      }
    } else {
      locationIdSet.add(0L);
    }

    return locationIdSet;
  }

  public List<StockLocation> getAllLocationAndSubLocation(
      StockLocation stockLocation, boolean isVirtualInclude) {

    List<StockLocation> resultList = new ArrayList<>();
    if (stockLocation == null) {
      return resultList;
    }
    if (isVirtualInclude) {
      for (StockLocation subLocation :
          stockLocationRepo
              .all()
              .filter("self.parentStockLocation.id = :stockLocationId")
              .bind("stockLocationId", stockLocation.getId())
              .fetch()) {

        resultList.addAll(this.getAllLocationAndSubLocation(subLocation, isVirtualInclude));
      }
    } else {
      for (StockLocation subLocation :
          stockLocationRepo
              .all()
              .filter(
                  "self.parentStockLocation.id = :stockLocationId AND self.typeSelect != :virtual")
              .bind("stockLocationId", stockLocation.getId())
              .bind("virtual", StockLocationRepository.TYPE_VIRTUAL)
              .fetch()) {

        resultList.addAll(this.getAllLocationAndSubLocation(subLocation, isVirtualInclude));
      }
    }
    resultList.add(stockLocation);

    return resultList;
  }

  @Override
  public BigDecimal getStockLocationValue(StockLocation stockLocation) {

    Query query =
        JPA.em()
            .createQuery(
                "SELECT SUM( self.currentQty * CASE WHEN (location.company.stockConfig.stockLocationValue = 1) THEN "
                    + "(self.avgPrice)  WHEN (location.company.stockConfig.stockLocationValue = 2) THEN "
                    + "CASE WHEN (self.product.costTypeSelect = 3) THEN (self.avgPrice) ELSE (self.product.costPrice) END "
                    + "WHEN (location.company.stockConfig.stockLocationValue = 3) THEN "
                    + "(self.product.salePrice) ELSE (self.avgPrice) END ) AS value "
                    + "FROM StockLocationLine AS self "
                    + "LEFT JOIN StockLocation AS location "
                    + "ON location.id= self.stockLocation "
                    + "WHERE self.stockLocation.id =:id");
    query.setParameter("id", stockLocation.getId());

    List<?> result = query.getResultList();
    return result.get(0) == null
        ? BigDecimal.ZERO
        : ((BigDecimal) result.get(0)).setScale(2, BigDecimal.ROUND_HALF_EVEN);
  }

  @Override
  public List<Long> getAllLocationAndSubLocationId(
      StockLocation stockLocation, boolean isVirtualInclude) {
    List<StockLocation> stockLocationList =
        getAllLocationAndSubLocation(stockLocation, isVirtualInclude);
    List<Long> stockLocationListId = null;
    if (stockLocationList != null) {
      stockLocationListId =
          stockLocationList.stream().map(StockLocation::getId).collect(Collectors.toList());
    }
    return stockLocationListId;
  }

  @Override
  public boolean isConfigMissing(StockLocation stockLocation, int printType) {

    StockConfig stockConfig = stockLocation.getCompany().getStockConfig();
    return printType == StockLocationRepository.PRINT_TYPE_LOCATION_FINANCIAL_DATA
        && (stockConfig == null
            || (!stockConfig.getIsDisplayAccountingValueInPrinting()
                && !stockConfig.getIsDisplayAgPriceInPrinting()
                && !stockConfig.getIsDisplaySaleValueInPrinting()));
  }
}
