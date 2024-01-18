/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2023 Axelor (<http://axelor.com>).
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
package com.axelor.apps.supplychain.service.batch;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.repo.BatchRepository;
import com.axelor.apps.base.service.administration.AbstractBatch;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.stock.db.StockLocation;
import com.axelor.apps.stock.db.StockLocationLine;
import com.axelor.apps.stock.db.StockRules;
import com.axelor.apps.stock.db.repo.StockRulesRepository;
import com.axelor.apps.stock.service.StockLocationService;
import com.axelor.apps.stock.service.StockRulesService;
import com.axelor.apps.stock.service.app.AppStockService;
import com.axelor.apps.supplychain.db.SupplychainBatch;
import com.axelor.apps.supplychain.service.StockRulesSupplychainService;
import com.axelor.common.ObjectUtils;
import com.axelor.db.JPA;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.persistence.TypedQuery;

public class BatchCheckStockComplianceWithStockRules extends AbstractBatch {

  protected final StockRulesService stockRulesService;
  protected final StockRulesSupplychainService stockRulesSupplychainService;
  protected final AppStockService appStockService;
  protected final StockLocationService stockLocationService;
  protected final StockRulesRepository stockRulesRepository;

  @Inject
  public BatchCheckStockComplianceWithStockRules(
      StockRulesService stockRulesService,
      StockRulesSupplychainService stockRulesSupplychainService,
      AppStockService appStockService,
      StockLocationService stockLocationService,
      StockRulesRepository stockRulesRepository) {
    this.stockRulesService = stockRulesService;
    this.stockRulesSupplychainService = stockRulesSupplychainService;
    this.appStockService = appStockService;
    this.stockLocationService = stockLocationService;
    this.stockRulesRepository = stockRulesRepository;
  }

  @Override
  protected void process() {

    int offset = 0;
    Map<StockRules, List<StockLocationLine>> stockLocationLinesByStockRules;
    while ((stockLocationLinesByStockRules =
            getNonCompliantStockLocationLinesByStockRules(AbstractBatch.FETCH_LIMIT, offset))
        != null) {
      if (ObjectUtils.isEmpty(stockLocationLinesByStockRules)) {
        offset += AbstractBatch.FETCH_LIMIT;
        JPA.clear();
        continue;
      }
      for (Map.Entry<StockRules, List<StockLocationLine>> stockLocationsByStockRule :
          stockLocationLinesByStockRules.entrySet()) {
        StockRules stockRule = stockLocationsByStockRule.getKey();
        List<StockLocationLine> stockLocationLines = stockLocationsByStockRule.getValue();
        for (StockLocationLine stockLocationLine : stockLocationLines) {
          try {
            processStockLocationLineNonCompliantToStockRules(stockRule, stockLocationLine);
            incrementDone();
          } catch (Exception e) {
            incrementAnomaly();
            TraceBackService.trace(e, null, batch.getId());
          }
        }
        offset += AbstractBatch.FETCH_LIMIT;
        JPA.clear();
      }
    }
  }

  @Transactional(rollbackOn = {Exception.class})
  public void processStockLocationLineNonCompliantToStockRules(
      StockRules stockRules, StockLocationLine stockLocationLine) throws AxelorException {
    stockRulesSupplychainService.processNonCompliantStockLocationLine(
        stockRules, stockLocationLine);
  }

  public Map<StockRules, List<StockLocationLine>> getNonCompliantStockLocationLinesByStockRules(
      int fetchLimit, int offset) {

    Map<StockRules, List<StockLocationLine>> stockLocationLineByStockRules = new HashMap<>();

    Map<StockLocation, List<StockLocationLine>> stockLocationLinesByStockLocation =
        getStockLocationLinesByStockLocationsToCheck(fetchLimit, offset);

    if (ObjectUtils.isEmpty(stockLocationLinesByStockLocation)) {
      /* This null serves as an escape for the calling while loop, since this method can return an empty list for a call and a list with elements for the next call, this null serves an indicator that all the subsequent calls will have no elements */
      return null;
    }

    for (Map.Entry<StockLocation, List<StockLocationLine>> mapEntry :
        stockLocationLinesByStockLocation.entrySet()) {
      StockLocation stockLocation = mapEntry.getKey();
      List<StockLocationLine> stockLocationLines = mapEntry.getValue();

      List<StockRules> stockRulesToBeChecked = getToBeCheckedStockRules(stockLocation);

      if (ObjectUtils.isEmpty(stockRulesToBeChecked)) {
        continue;
      }

      for (StockLocationLine stockLocationLine : stockLocationLines) {
        List<StockRules> toBeCheckedStockRules =
            stockRulesToBeChecked.stream()
                .filter(
                    stockRules -> stockLocationLine.getProduct().equals(stockRules.getProduct()))
                .collect(Collectors.toList());
        for (StockRules stockRules : toBeCheckedStockRules) {
          if (stockRulesService.useMinStockRules(
              stockLocationLine, stockRules, stockRules.getTypeSelect())) {
            List<StockLocationLine> sll = stockLocationLineByStockRules.get(stockRules);
            sll = sll != null ? sll : new ArrayList<>();
            sll.add(stockLocationLine);
            stockLocationLineByStockRules.put(stockRules, sll);
          }
        }
      }
    }
    return stockLocationLineByStockRules;
  }

  protected List<StockRules> getToBeCheckedStockRules(StockLocation stockLocation) {
    Set<StockLocation> stockLocationAndItsParents =
        stockLocationService.getListOfStockLocationAndAllItsParentsStockLocations(stockLocation);
    SupplychainBatch supplychainBatch = batch.getSupplychainBatch();
    Set<StockRules> usedStockRules = supplychainBatch.getUsedStockRulesSet();
    return ObjectUtils.notEmpty(usedStockRules)
        ? usedStockRules.stream()
            .filter(sr -> stockLocationAndItsParents.contains(sr.getStockLocation()))
            .collect(Collectors.toList())
        : getStockRulesOfStockLocationsAndUseCaseStockControl(stockLocationAndItsParents);
  }

  protected List<StockRules> getStockRulesOfStockLocationsAndUseCaseStockControl(
      Set<StockLocation> stockLocations) {
    return stockRulesRepository
        .all()
        .filter(" self.stockLocation in :stockLocations AND self.useCaseSelect = :useCase")
        .bind("stockLocations", stockLocations)
        .bind("useCase", StockRulesRepository.USE_CASE_STOCK_CONTROL)
        .fetch();
  }

  public Map<StockLocation, List<StockLocationLine>> getStockLocationLinesByStockLocationsToCheck(
      int fetchLimit, int offset) {
    SupplychainBatch supplychainBatch = batch.getSupplychainBatch();
    Set<StockRules> usedStockRules = supplychainBatch.getUsedStockRulesSet();

    StringBuilder query = new StringBuilder();
    Map<String, Object> parameterMap = new HashMap<>();
    query.append("SELECT sll FROM StockLocationLine sll WHERE sll.stockLocation is not null");

    if (!ObjectUtils.isEmpty(usedStockRules)) {
      query.append(" AND sll.product.id IN :productIds");
      parameterMap.put(
          "productIds",
          usedStockRules.stream()
              .map(StockRules::getProduct)
              .map(Product::getId)
              .distinct()
              .collect(Collectors.toList()));
    }
    query.append(" ORDER BY sll.stockLocation.id,sll.id desc");

    TypedQuery<StockLocationLine> typedQuery =
        JPA.em()
            .createQuery(query.toString(), StockLocationLine.class)
            .setMaxResults(fetchLimit)
            .setFirstResult(offset);
    parameterMap.forEach(typedQuery::setParameter);

    /* Don't change the groupingBy lambda expression to a method reference, it will throw a ClassNotFoundException */
    return typedQuery.getResultList().stream()
        .collect(Collectors.groupingBy(sll -> sll.getStockLocation()));
  }

  @Override
  protected void setBatchTypeSelect() {
    this.batch.setBatchTypeSelect(BatchRepository.BATCH_TYPE_SUPPLYCHAIN_BATCH);
  }
}
