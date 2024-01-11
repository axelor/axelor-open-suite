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
package com.axelor.apps.stock.service.batch;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.BatchRepository;
import com.axelor.apps.base.db.repo.ExceptionOriginRepository;
import com.axelor.apps.base.service.administration.AbstractBatch;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.stock.db.StockMove;
import com.axelor.apps.stock.db.repo.StockLocationRepository;
import com.axelor.apps.stock.db.repo.StockMoveLineRepository;
import com.axelor.apps.stock.db.repo.StockMoveRepository;
import com.axelor.apps.stock.service.StockMoveLineService;
import com.axelor.apps.stock.service.StockMoveService;
import com.axelor.apps.stock.service.batch.model.StockMoveGroup;
import com.axelor.apps.stock.service.batch.model.StockMoveLineOrigin;
import com.axelor.apps.stock.service.batch.model.TrackProduct;
import com.axelor.db.JPA;
import com.axelor.db.Query;
import com.google.inject.Inject;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;

public class BatchRecomputeStockLocationLines extends AbstractBatch {

  protected StockMoveRepository stockMoveRepository;
  protected StockMoveService stockMoveService;
  protected StockMoveLineService stockMoveLineService;
  protected StockMoveLineRepository stockMoveLineRepository;
  protected StockLocationRepository stockLocationRepository;

  @Inject
  public BatchRecomputeStockLocationLines(
      StockMoveRepository stockMoveRepository,
      StockMoveService stockMoveService,
      StockMoveLineService stockMoveLineService,
      StockMoveLineRepository stockMoveLineRepository,
      StockLocationRepository stockLocationRepository) {

    this.stockMoveLineRepository = stockMoveLineRepository;
    this.stockMoveRepository = stockMoveRepository;
    this.stockMoveService = stockMoveService;
    this.stockMoveLineService = stockMoveLineService;
    this.stockLocationRepository = stockLocationRepository;
  }

  @Override
  protected void process() {
    clearWapHistoryLines();
    resetStockLocations();
    List<StockMoveGroup> groups = fetchStockMoveGroup();

    // Recomputing stockLocationLine with realized stock move
    groups.stream()
        .filter(
            stockMoveGroup ->
                stockMoveGroup.getStatusSelect() == StockMoveRepository.STATUS_REALIZED)
        .forEachOrdered(
            stockMoveGroup -> {
              try {
                recomputeStockMoves(stockMoveGroup);
                incrementDone();
              } catch (Exception e) {
                incrementAnomaly();
                TraceBackService.trace(
                    e, ExceptionOriginRepository.RECOMPUTE_STOCK_MOVE_LINES, batch.getId());
              } finally {
                JPA.clear();
              }
            });

    // Updating planned quantities
    groups.stream()
        .filter(
            stockMoveGroup ->
                stockMoveGroup.getStatusSelect() == StockMoveRepository.STATUS_PLANNED)
        .forEachOrdered(
            stockMoveGroup -> {
              try {
                updatePlannedQty(stockMoveGroup);
                incrementDone();
              } catch (Exception e) {
                incrementAnomaly();
                TraceBackService.trace(
                    e, ExceptionOriginRepository.RECOMPUTE_STOCK_MOVE_LINES, batch.getId());
              }
            });
  }

  protected void resetStockLocations() {

    javax.persistence.Query clearWapHistoryLinesQuery =
        JPA.em()
            .createNativeQuery(
                "UPDATE stock_stock_location_line SET "
                    + " avg_price = 0, "
                    + " current_qty = 0, "
                    + " future_qty = 0 ");

    JPA.runInTransaction(clearWapHistoryLinesQuery::executeUpdate);
  }

  protected void updatePlannedQty(StockMoveGroup stockMoveGroup) throws AxelorException {

    List<StockMove> stockMoves;
    Query<StockMove> query = buildQueryFetchStockMoveFromGroup(stockMoveGroup).order("id");
    int offSet = 0;
    while (!(stockMoves = query.fetch(FETCH_LIMIT, offSet)).isEmpty()) {

      for (StockMove stockMove : stockMoves) {
        stockMoveService.updateLocations(
            stockMove,
            stockMove.getFromStockLocation(),
            stockMove.getToStockLocation(),
            StockMoveRepository.STATUS_DRAFT);
      }

      offSet += FETCH_LIMIT;
      JPA.clear();
    }
  }

  protected String getOrigin(StockMove stockMove) {
    return Optional.ofNullable(stockMove).map(StockMove::getStockMoveSeq).orElse("");
  }

  protected void recomputeStockMoves(StockMoveGroup group) throws AxelorException {
    List<StockMove> stockMoves;
    Query<StockMove> query = buildQueryFetchStockMoveFromGroup(group).order("id");
    HashMap<TrackProduct, StockMoveLineOrigin> stockMoveLinesMap = new HashMap<>();
    int offSet = 0;

    while (!(stockMoves = query.fetch(FETCH_LIMIT, offSet)).isEmpty()) {

      stockMoves.stream()
          .forEach(
              stockMove -> {
                stockMove.getStockMoveLineList().stream()
                    .forEach(
                        stockMoveLine -> {
                          TrackProduct trackProduct =
                              new TrackProduct(
                                  stockMoveLine.getProduct(), stockMoveLine.getTrackingNumber());
                          if (!stockMoveLinesMap.containsKey(trackProduct)) {
                            stockMoveLinesMap.put(
                                trackProduct,
                                new StockMoveLineOrigin(
                                    stockMoveLineRepository.copy(stockMoveLine, false),
                                    getOrigin(stockMove)));
                          } else {
                            stockMoveLinesMap.merge(
                                trackProduct,
                                new StockMoveLineOrigin(stockMoveLine, getOrigin(stockMove)),
                                this::merge);
                          }
                        });
              });

      offSet += FETCH_LIMIT;
    }

    for (Entry<TrackProduct, StockMoveLineOrigin> entry : stockMoveLinesMap.entrySet()) {
      stockMoveLineService.updateLocations(
          stockLocationRepository.find(group.getFromStockLocation()),
          stockLocationRepository.find(group.getToStockLocation()),
          StockMoveRepository.STATUS_PLANNED,
          StockMoveRepository.STATUS_REALIZED,
          Collections.singletonList(entry.getValue().getStockMoveLine()),
          null,
          false,
          group.getRealDate(),
          entry.getValue().getOrigin(),
          true);
    }
  }

  protected StockMoveLineOrigin merge(StockMoveLineOrigin sml1, StockMoveLineOrigin sml2) {

    return sml1.merge(sml2);
  }

  protected void clearWapHistoryLines() {

    javax.persistence.Query clearWapHistoryLinesQuery =
        JPA.em().createNativeQuery("Delete FROM stock_wap_history");

    javax.persistence.Query clearStockLocationsHistoryLinesQuery =
        JPA.em().createNativeQuery("Delete FROM stock_stock_location_line_history");

    JPA.runInTransaction(clearWapHistoryLinesQuery::executeUpdate);
    JPA.runInTransaction(clearStockLocationsHistoryLinesQuery::executeUpdate);
  }

  protected Query<StockMove> buildQueryFetchStockMoveFromGroup(StockMoveGroup stockMoveGroup) {

    StringBuilder query =
        new StringBuilder(
            "self.fromStockLocation.id = :fromStockLocation"
                + " AND self.toStockLocation.id = :toStockLocation"
                + " AND self.statusSelect = :status");

    if (stockMoveGroup.getRealDate() == null) {
      query.append(" AND self.realDate is NULL");
    } else {
      query.append(" AND self.realDate = :realDate");
    }

    return stockMoveRepository
        .all()
        .filter(query.toString())
        .bind("realDate", stockMoveGroup.getRealDate())
        .bind("fromStockLocation", stockMoveGroup.getFromStockLocation())
        .bind("toStockLocation", stockMoveGroup.getToStockLocation())
        .bind("status", stockMoveGroup.getStatusSelect());
  }

  protected List<StockMoveGroup> fetchStockMoveGroup() {
    List<StockMoveGroup> stockMoveGroups = new ArrayList<>();
    javax.persistence.Query query =
        JPA.em()
            .createQuery(
                "SELECT "
                    + "self.realDate,"
                    + " self.fromStockLocation.id,"
                    + " self.toStockLocation.id,"
                    + " self.statusSelect,"
                    + " self.toStockLocation.typeSelect"
                    + " FROM StockMove self "
                    + " GROUP BY"
                    + " self.realDate, self.fromStockLocation.id, self.toStockLocation.id, self.statusSelect, self.toStockLocation.typeSelect"
                    + " ORDER BY self.realDate, self.toStockLocation.typeSelect");

    List<Object[]> resultList = query.getResultList();
    for (Object[] result : resultList) {
      stockMoveGroups.add(createStockMoveGroup(result));
    }

    return stockMoveGroups;
  }

  protected StockMoveGroup createStockMoveGroup(Object[] result) {
    LocalDate realDate = null;
    Long fromStockLocation = null;
    Long toStockLocation = null;
    Integer status = null;

    if (result[0] != null) {
      realDate = (LocalDate) result[0];
    }

    if (result[1] != null) {
      fromStockLocation = (Long) result[1];
    }

    if (result[2] != null) {
      toStockLocation = (Long) result[2];
    }

    if (result[3] != null) {
      status = (Integer) result[3];
    }

    return new StockMoveGroup(realDate, fromStockLocation, toStockLocation, status);
  }

  @Override
  protected void setBatchTypeSelect() {
    this.batch.setBatchTypeSelect(BatchRepository.BATCH_TYPE_STOCK_BATCH);
  }
}
