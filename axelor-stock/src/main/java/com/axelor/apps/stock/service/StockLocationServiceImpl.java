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
package com.axelor.apps.stock.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.Unit;
import com.axelor.apps.base.db.repo.ProductRepository;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.db.repo.UnitRepository;
import com.axelor.apps.base.service.UnitConversionService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.stock.db.StockConfig;
import com.axelor.apps.stock.db.StockLocation;
import com.axelor.apps.stock.db.StockLocationLine;
import com.axelor.apps.stock.db.StockRules;
import com.axelor.apps.stock.db.repo.StockLocationLineRepository;
import com.axelor.apps.stock.db.repo.StockLocationRepository;
import com.axelor.apps.stock.db.repo.StockRulesRepository;
import com.axelor.apps.stock.exception.StockExceptionMessage;
import com.axelor.apps.stock.service.config.StockConfigService;
import com.axelor.db.JPA;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.rpc.filter.Filter;
import com.axelor.rpc.filter.JPQLFilter;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import com.google.inject.servlet.RequestScoped;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.persistence.Query;
import javax.persistence.Tuple;
import javax.persistence.TypedQuery;

@RequestScoped
public class StockLocationServiceImpl implements StockLocationService {

  protected StockLocationRepository stockLocationRepo;

  protected StockLocationLineService stockLocationLineService;

  protected ProductRepository productRepo;

  protected StockConfigService stockConfigService;
  protected AppBaseService appBaseService;
  protected UnitRepository unitRepository;
  protected UnitConversionService unitConversionService;

  protected Set<Long> locationIdSet = new HashSet<>();

  @Inject
  public StockLocationServiceImpl(
      StockLocationRepository stockLocationRepo,
      StockLocationLineService stockLocationLineService,
      ProductRepository productRepo,
      StockConfigService stockConfigService,
      AppBaseService appBaseService,
      UnitRepository unitRepository,
      UnitConversionService unitConversionService) {
    this.stockLocationRepo = stockLocationRepo;
    this.stockLocationLineService = stockLocationLineService;
    this.productRepo = productRepo;
    this.stockConfigService = stockConfigService;
    this.appBaseService = appBaseService;
    this.unitRepository = unitRepository;
    this.unitConversionService = unitConversionService;
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

  protected BigDecimal getQtyOfProductInStockLocations(
      Long productId, List<Long> stockLocationIds, Long companyId, String qtyFieldName)
      throws AxelorException {
    Product product = productRepo.find(productId);
    Unit productUnit = product.getUnit();

    StringBuilder query = new StringBuilder();
    Map<String, Object> parameterMap = new HashMap<>();
    query.append("SELECT self.unit.id, sum(self.%s)");
    query.append(" FROM StockLocationLine self");
    query.append(" WHERE self.stockLocation.typeSelect != :stockLocationTypeSelectVirtual");
    query.append(" AND self.product.id = :productId AND self.product.stockManaged is TRUE");

    parameterMap.put("stockLocationTypeSelectVirtual", StockLocationRepository.TYPE_VIRTUAL);
    parameterMap.put("productId", product.getId());

    if (companyId != null && companyId > 0L) {
      query.append(" AND self.stockLocation.company.id = :companyId");
      parameterMap.put("companyId", companyId);
    }

    if (stockLocationIds != null && !stockLocationIds.isEmpty()) {
      query.append(" AND self.stockLocation.id IN (:stockLocationIds)");
      parameterMap.put("stockLocationIds", stockLocationIds);
    }

    query.append(" GROUP BY self.unit.id");

    TypedQuery<Tuple> sumOfQtyPerUnitQuery =
        JPA.em().createQuery(String.format(query.toString(), qtyFieldName), Tuple.class);

    parameterMap.forEach(sumOfQtyPerUnitQuery::setParameter);

    BigDecimal sumOfQty = BigDecimal.ZERO;
    for (Tuple qtyPerUnit : sumOfQtyPerUnitQuery.getResultList()) {
      Long stockLocationLineUnitId = (Long) qtyPerUnit.get(0);
      BigDecimal sumOfQtyOfStockLocationLineUnit = (BigDecimal) qtyPerUnit.get(1);
      if (stockLocationLineUnitId == null) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_NO_VALUE,
            I18n.get(StockExceptionMessage.STOCK_LOCATION_UNIT_NULL));
      }
      if (productUnit != null && !productUnit.getId().equals(stockLocationLineUnitId)) {
        Unit stockLocationLineUnit = unitRepository.find(stockLocationLineUnitId);

        sumOfQty =
            sumOfQty.add(
                unitConversionService.convert(
                    stockLocationLineUnit,
                    productUnit,
                    sumOfQtyOfStockLocationLineUnit,
                    sumOfQtyOfStockLocationLineUnit.scale(),
                    product));
      } else {
        sumOfQty = sumOfQty.add(sumOfQtyOfStockLocationLineUnit);
      }
    }
    return sumOfQty.setScale(appBaseService.getNbDecimalDigitForQty(), RoundingMode.HALF_UP);
  }

  @Override
  public BigDecimal getRealQtyOfProductInStockLocations(
      Long productId, List<Long> stockLocationIds, Long companyId) throws AxelorException {

    return getQtyOfProductInStockLocations(productId, stockLocationIds, companyId, "currentQty");
  }

  @Override
  public BigDecimal getFutureQtyOfProductInStockLocations(
      Long productId, List<Long> stockLocationIds, Long companyId) throws AxelorException {

    return getQtyOfProductInStockLocations(productId, stockLocationIds, companyId, "futureQty");
  }

  @Override
  public Map<String, Object> getStockIndicators(Long productId, Long companyId, Long locationId)
      throws AxelorException {
    Map<String, Object> map = new HashMap<>();

    List<Long> stockLocationIds = Collections.singletonList(locationId);
    map.put(
        "$realQty", getRealQtyOfProductInStockLocations(productId, stockLocationIds, companyId));
    map.put(
        "$futureQty",
        getFutureQtyOfProductInStockLocations(productId, stockLocationIds, companyId));
    return map;
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
      List<StockLocation> stockLocations = getAllLocationAndSubLocation(stockLocation, false);
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
                "SELECT SUM( self.currentQty * "
                    + "CASE WHEN (location.company.stockConfig.stockValuationTypeSelect = 1) THEN (self.product.avgPrice) "
                    + "WHEN (location.company.stockConfig.stockValuationTypeSelect = 2) THEN "
                    + "CASE WHEN (self.product.costTypeSelect = 3) THEN (self.avgPrice) ELSE (self.product.costPrice) END "
                    + "WHEN (location.company.stockConfig.stockValuationTypeSelect = 3) THEN (self.product.salePrice) "
                    + "WHEN (location.company.stockConfig.stockValuationTypeSelect = 4) THEN (self.product.purchasePrice) "
                    + "WHEN (location.company.stockConfig.stockValuationTypeSelect = 5) THEN (self.avgPrice) "
                    + "ELSE (self.avgPrice) END ) AS value "
                    + "FROM StockLocationLine AS self "
                    + "LEFT JOIN StockLocation AS location "
                    + "ON location.id= self.stockLocation "
                    + "WHERE self.stockLocation.id =:id");
    query.setParameter("id", stockLocation.getId());

    List<?> result = query.getResultList();
    return (result.get(0) == null || ((BigDecimal) result.get(0)).signum() == 0)
        ? BigDecimal.ZERO
        : ((BigDecimal) result.get(0)).setScale(2, BigDecimal.ROUND_HALF_UP);
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
                    && !stockConfig.getIsDisplaySaleValueInPrinting())
                && !stockConfig.getIsDisplayPurchaseValueInPrinting());
  }

  @Override
  @Transactional
  public void changeProductLocker(StockLocation stockLocation, Product product, String newLocker) {
    List<StockLocationLine> stockLocationLineList = stockLocation.getStockLocationLineList();
    for (StockLocationLine stockLocationLine : stockLocationLineList) {
      if (stockLocationLine.getProduct() == product) {
        stockLocationLine.setRack(newLocker);
      }
    }
    stockLocationRepo.save(stockLocation);
  }
}
