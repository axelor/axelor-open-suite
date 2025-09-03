/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2025 Axelor (<http://axelor.com>).
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
import com.axelor.apps.base.db.repo.ProductRepository;
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
import com.axelor.apps.stock.service.config.StockConfigService;
import com.axelor.apps.stock.utils.StockLocationUtilsService;
import com.axelor.db.JPA;
import com.axelor.rpc.filter.Filter;
import com.axelor.rpc.filter.JPQLFilter;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import com.google.inject.servlet.RequestScoped;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@RequestScoped
public class StockLocationServiceImpl implements StockLocationService {

  protected StockLocationRepository stockLocationRepo;

  protected StockLocationLineService stockLocationLineService;

  protected ProductRepository productRepo;

  protected StockConfigService stockConfigService;
  protected AppBaseService appBaseService;
  protected UnitRepository unitRepository;
  protected UnitConversionService unitConversionService;
  protected StockLocationUtilsService stockLocationUtilsService;

  protected final StockRulesRepository stockRulesRepository;
  protected final StockLocationLineRepository stockLocationLineRepository;

  protected Set<Long> locationIdSet = new HashSet<>();

  @Inject
  public StockLocationServiceImpl(
      StockLocationRepository stockLocationRepo,
      StockLocationLineService stockLocationLineService,
      ProductRepository productRepo,
      StockConfigService stockConfigService,
      AppBaseService appBaseService,
      UnitRepository unitRepository,
      UnitConversionService unitConversionService,
      StockLocationUtilsService stockLocationUtilsService,
      StockRulesRepository stockRulesRepository,
      StockLocationLineRepository stockLocationLineRepository) {
    this.stockLocationRepo = stockLocationRepo;
    this.stockLocationLineService = stockLocationLineService;
    this.productRepo = productRepo;
    this.stockConfigService = stockConfigService;
    this.appBaseService = appBaseService;
    this.unitRepository = unitRepository;
    this.unitConversionService = unitConversionService;
    this.stockLocationUtilsService = stockLocationUtilsService;
    this.stockRulesRepository = stockRulesRepository;
    this.stockLocationLineRepository = stockLocationLineRepository;
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
  public Map<String, Object> getStockIndicators(Long productId, Long companyId, Long locationId)
      throws AxelorException {
    Map<String, Object> map = new HashMap<>();

    List<Long> stockLocationIds = Collections.singletonList(locationId);
    map.put(
        "$realQty",
        stockLocationUtilsService.getRealQtyOfProductInStockLocations(
            productId, stockLocationIds, companyId));
    map.put(
        "$futureQty",
        stockLocationUtilsService.getFutureQtyOfProductInStockLocations(
            productId, stockLocationIds, companyId));
    return map;
  }

  public List<Long> getBadStockLocationLineId() {

    List<StockLocationLine> stockLocationLineList =
        stockLocationLineRepository
            .all()
            .filter("self.stockLocation.typeSelect = 1 OR self.stockLocation.typeSelect = 2")
            .fetch();

    List<Long> idList = new ArrayList<>();

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

  @Override
  public Set<StockLocation> getListOfStockLocationAndAllItsParentsStockLocations(
      StockLocation stockLocation) {
    Set<StockLocation> allStockLocations = new HashSet<>();
    allStockLocations.add(stockLocation);
    if (stockLocation == null) {
      return allStockLocations;
    }
    StockLocation parentStockLocation = stockLocation.getParentStockLocation();
    if (parentStockLocation == null) {
      return allStockLocations;
    }
    while (allStockLocations.add(parentStockLocation)) {
      parentStockLocation = parentStockLocation.getParentStockLocation();
      if (parentStockLocation == null) {
        break;
      }
    }
    return allStockLocations;
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

  public List<Long> getAllLocationAndSubLocation(Long stockLocationId, boolean isVirtualInclude) {

    List<Long> resultList = new ArrayList<>();
    if (stockLocationId == null) {
      return resultList;
    }
    for (Long subLocationId :
        JPA.em()
            .createQuery(
                "SELECT sl.id FROM StockLocation sl WHERE sl.parentStockLocation.id = :stockLocationId AND (:isVirtual is true OR sl.typeSelect != :isVirtual)",
                Long.class)
            .setParameter("stockLocationId", stockLocationId)
            .setParameter("isVirtual", isVirtualInclude)
            .getResultList()) {
      resultList.addAll(this.getAllLocationAndSubLocation(subLocationId, isVirtualInclude));
    }
    resultList.add(stockLocationId);

    return resultList;
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
    Optional.ofNullable(
            stockLocationLineRepository
                .all()
                .filter("self.product = :product AND self.stockLocation = :stockLocation")
                .bind("product", product)
                .bind("stockLocation", stockLocation)
                .fetchOne())
        .ifPresent(sml -> sml.setRack(newLocker));
  }

  @Override
  public String computeStockLocationChildren(StockLocation stockLocation) {
    if (stockLocation == null) {
      return "self.id in (0)";
    }
    return String.format(
        "self.id in (%s)",
        getAllLocationAndSubLocation(stockLocation, false).stream()
            .map(location -> location.getId().toString())
            .collect(Collectors.joining(",")));
  }

  @Override
  public Set<Long> getLocationAndAllParentLocationsIdsOrderedFromTheClosestToTheFurthest(
      StockLocation stockLocation) {
    Set<Long> resultSet = new LinkedHashSet<>();
    if (stockLocation == null) {
      return resultSet;
    }
    resultSet.add(stockLocation.getId());
    StockLocation parentStockLocation = stockLocation.getParentStockLocation();
    /* Adding to the set returns false if the value already exists, in our case this could be a good
    way to prevent an infinite loop */
    while (parentStockLocation != null && resultSet.add(parentStockLocation.getId())) {
      parentStockLocation = parentStockLocation.getParentStockLocation();
    }
    return resultSet;
  }
}
