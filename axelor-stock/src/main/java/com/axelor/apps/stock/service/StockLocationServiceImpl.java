/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2026 Axelor (<http://axelor.com>).
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
import com.axelor.rpc.filter.Filter;
import com.axelor.rpc.filter.JPQLFilter;
import com.google.common.collect.Lists;
import com.google.inject.persist.Transactional;
import com.google.inject.servlet.RequestScoped;
import jakarta.inject.Inject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

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
  protected final StockLocationFetchService stockLocationFetchService;

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
      StockLocationLineRepository stockLocationLineRepository,
      StockLocationFetchService stockLocationFetchService) {
    this.stockLocationLineService = stockLocationLineService;
    this.productRepo = productRepo;
    this.stockConfigService = stockConfigService;
    this.appBaseService = appBaseService;
    this.unitRepository = unitRepository;
    this.unitConversionService = unitConversionService;
    this.stockLocationUtilsService = stockLocationUtilsService;
    this.stockRulesRepository = stockRulesRepository;
    this.stockLocationLineRepository = stockLocationLineRepository;
    this.stockLocationFetchService = stockLocationFetchService;
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
    Set<Long> locationIdSet = new HashSet<>();
    if (stockLocation == null) {
      locationIdSet.add(0l);
      return locationIdSet;
    }

    locationIdSet.addAll(
        stockLocationFetchService.getAllContentLocationAndSubLocation(stockLocation.getId()));
    return locationIdSet;
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
}
