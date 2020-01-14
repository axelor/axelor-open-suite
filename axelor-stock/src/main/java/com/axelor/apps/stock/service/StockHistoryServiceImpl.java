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
package com.axelor.apps.stock.service;

import com.axelor.apps.base.service.UnitConversionService;
import com.axelor.apps.stock.db.StockHistoryLine;
import com.axelor.apps.stock.db.StockMoveLine;
import com.axelor.apps.stock.db.repo.StockMoveLineRepository;
import com.axelor.apps.stock.db.repo.StockMoveRepository;
import com.axelor.db.JPA;
import com.axelor.exception.AxelorException;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class StockHistoryServiceImpl implements StockHistoryService {

  protected StockMoveLineRepository stockMoveLineRepository;
  protected UnitConversionService unitConversionService;

  @Inject
  public StockHistoryServiceImpl(
      StockMoveLineRepository stockMoveLineRepository,
      UnitConversionService unitConversionService) {
    this.stockMoveLineRepository = stockMoveLineRepository;
    this.unitConversionService = unitConversionService;
  }

  public List<StockHistoryLine> computeStockHistoryLineList(
      Long productId, Long companyId, Long stockLocationId, LocalDate beginDate, LocalDate endDate)
      throws AxelorException {
    List<StockHistoryLine> stockHistoryLineList = new ArrayList<>();

    // one line per month
    for (LocalDate periodBeginDate = beginDate.withDayOfMonth(1);
        periodBeginDate.isBefore(endDate);
        periodBeginDate = periodBeginDate.plusMonths(1)) {
      LocalDate periodEndDate = periodBeginDate.plusMonths(1);
      StockHistoryLine stockHistoryLine = new StockHistoryLine();
      stockHistoryLine.setLabel(periodBeginDate.toString());
      fetchAndFillResultForStockHistoryQuery(
          stockHistoryLine,
          productId,
          companyId,
          stockLocationId,
          periodBeginDate,
          periodEndDate,
          true);
      fetchAndFillResultForStockHistoryQuery(
          stockHistoryLine,
          productId,
          companyId,
          stockLocationId,
          periodBeginDate,
          periodEndDate,
          false);
      stockHistoryLineList.add(stockHistoryLine);
    }
    StockHistoryLine totalStockHistoryLine = createStockHistoryTotalLine(stockHistoryLineList);
    StockHistoryLine avgStockHistoryLine =
        createStockHistoryAvgLine(stockHistoryLineList, totalStockHistoryLine);
    stockHistoryLineList.add(totalStockHistoryLine);
    stockHistoryLineList.add(avgStockHistoryLine);

    // result lines
    return stockHistoryLineList;
  }

  protected void fetchAndFillResultForStockHistoryQuery(
      StockHistoryLine stockHistoryLine,
      Long productId,
      Long companyId,
      Long stockLocationId,
      LocalDate periodBeginDate,
      LocalDate periodEndDate,
      boolean incoming)
      throws AxelorException {
    String filter =
        "self.product.id = :productId "
            + "AND self.stockMove.statusSelect = :realized "
            + "AND self.stockMove.company.id = :companyId "
            + "AND self.stockMove.realDate >= :beginDate "
            + "AND self.stockMove.realDate < :endDate ";

    if (incoming) {
      filter += "AND self.stockMove.toStockLocation.id = :stockLocationId ";
    } else {
      filter += "AND self.stockMove.fromStockLocation.id = :stockLocationId ";
    }

    List<StockMoveLine> stockMoveLineList =
        stockMoveLineRepository
            .all()
            .filter(filter)
            .bind("productId", productId)
            .bind("companyId", companyId)
            .bind("stockLocationId", stockLocationId)
            .bind("realized", StockMoveRepository.STATUS_REALIZED)
            .bind("beginDate", periodBeginDate)
            .bind("endDate", periodEndDate)
            .fetch();
    if (incoming) {
      fillIncomingStockHistoryLineFields(stockHistoryLine, stockMoveLineList);
    } else {
      fillOutgoingStockHistoryLineFields(stockHistoryLine, stockMoveLineList);
    }
    JPA.clear();
  }

  protected void fillIncomingStockHistoryLineFields(
      StockHistoryLine stockHistoryLine, List<StockMoveLine> stockMoveLineList)
      throws AxelorException {

    stockHistoryLine.setCountIncMvtStockPeriod(
        Math.toIntExact(
            stockMoveLineList.stream().map(StockMoveLine::getStockMove).distinct().count()));
    BigDecimal sumIncQtyPeriod = BigDecimal.ZERO;
    for (StockMoveLine stockMoveLine : stockMoveLineList) {
      // quantity in product unit
      BigDecimal qtyConverted =
          unitConversionService.convert(
              stockMoveLine.getUnit(),
              stockMoveLine.getProduct().getUnit(),
              stockMoveLine.getRealQty(),
              stockMoveLine.getRealQty().scale(),
              stockMoveLine.getProduct());
      sumIncQtyPeriod = sumIncQtyPeriod.add(qtyConverted);
    }
    stockHistoryLine.setSumIncQtyPeriod(sumIncQtyPeriod);
    stockHistoryLine.setPriceIncStockMovePeriod(
        stockMoveLineList
            .stream()
            .map(StockMoveLine::getValuatedUnitPrice)
            .reduce(BigDecimal::add)
            .orElse(BigDecimal.ZERO));
  }

  protected void fillOutgoingStockHistoryLineFields(
      StockHistoryLine stockHistoryLine, List<StockMoveLine> stockMoveLineList)
      throws AxelorException {

    stockHistoryLine.setCountOutMvtStockPeriod(
        Math.toIntExact(
            stockMoveLineList.stream().map(StockMoveLine::getStockMove).distinct().count()));
    BigDecimal sumOutQtyPeriod = BigDecimal.ZERO;
    for (StockMoveLine stockMoveLine : stockMoveLineList) {
      // quantity in product unit
      BigDecimal qtyConverted =
          unitConversionService.convert(
              stockMoveLine.getUnit(),
              stockMoveLine.getProduct().getUnit(),
              stockMoveLine.getRealQty(),
              stockMoveLine.getRealQty().scale(),
              stockMoveLine.getProduct());
      sumOutQtyPeriod = sumOutQtyPeriod.add(qtyConverted);
    }
    stockHistoryLine.setSumOutQtyPeriod(sumOutQtyPeriod);
    stockHistoryLine.setPriceOutStockMovePeriod(
        stockMoveLineList
            .stream()
            .map(StockMoveLine::getValuatedUnitPrice)
            .reduce(BigDecimal::add)
            .orElse(BigDecimal.ZERO));
  }

  /**
   * Create a line labelled "Total", summing each field in the table.
   *
   * @param stockHistoryLineList
   * @return the created line.
   */
  protected StockHistoryLine createStockHistoryTotalLine(
      List<StockHistoryLine> stockHistoryLineList) {
    StockHistoryLine stockHistoryLine = new StockHistoryLine();
    stockHistoryLine.setLabel(I18n.get("Total"));

    Integer countIncMvtStock =
        stockHistoryLineList
            .stream()
            .map(StockHistoryLine::getCountIncMvtStockPeriod)
            .filter(Objects::nonNull)
            .mapToInt(Integer::intValue)
            .sum();
    stockHistoryLine.setCountIncMvtStockPeriod(countIncMvtStock);

    BigDecimal sumIncQtyPeriod =
        stockHistoryLineList
            .stream()
            .map(StockHistoryLine::getSumIncQtyPeriod)
            .filter(Objects::nonNull)
            .reduce(BigDecimal::add)
            .orElse(BigDecimal.ZERO);
    stockHistoryLine.setSumIncQtyPeriod(sumIncQtyPeriod);

    BigDecimal priceIncStockMove =
        stockHistoryLineList
            .stream()
            .map(StockHistoryLine::getPriceIncStockMovePeriod)
            .filter(Objects::nonNull)
            .reduce(BigDecimal::add)
            .orElse(BigDecimal.ZERO);
    stockHistoryLine.setPriceIncStockMovePeriod(priceIncStockMove);

    Integer countOutMvtStock =
        stockHistoryLineList
            .stream()
            .map(StockHistoryLine::getCountOutMvtStockPeriod)
            .filter(Objects::nonNull)
            .mapToInt(Integer::intValue)
            .sum();
    stockHistoryLine.setCountOutMvtStockPeriod(countOutMvtStock);

    BigDecimal sumOutQtyPeriod =
        stockHistoryLineList
            .stream()
            .map(StockHistoryLine::getSumOutQtyPeriod)
            .filter(Objects::nonNull)
            .reduce(BigDecimal::add)
            .orElse(BigDecimal.ZERO);
    stockHistoryLine.setSumOutQtyPeriod(sumOutQtyPeriod);

    BigDecimal priceOutStockMove =
        stockHistoryLineList
            .stream()
            .map(StockHistoryLine::getPriceOutStockMovePeriod)
            .filter(Objects::nonNull)
            .reduce(BigDecimal::add)
            .orElse(BigDecimal.ZERO);
    stockHistoryLine.setPriceOutStockMovePeriod(priceOutStockMove);

    return stockHistoryLine;
  }

  /**
   * Create a line labelled "Average", using a list of stockHistory and a line containing totals.
   *
   * @param stockHistoryLineList
   * @param totalStockHistoryLine
   * @return the created line.
   */
  protected StockHistoryLine createStockHistoryAvgLine(
      List<StockHistoryLine> stockHistoryLineList, StockHistoryLine totalStockHistoryLine) {
    StockHistoryLine stockHistoryLine = new StockHistoryLine();
    stockHistoryLine.setLabel(I18n.get("Average"));

    int sizeOfList = stockHistoryLineList.size();
    if (sizeOfList == 0) {
      return stockHistoryLine;
    }

    stockHistoryLine.setCountIncMvtStockPeriod(
        totalStockHistoryLine.getCountIncMvtStockPeriod() / sizeOfList);

    stockHistoryLine.setSumIncQtyPeriod(
        totalStockHistoryLine
            .getSumIncQtyPeriod()
            .divide(BigDecimal.valueOf(sizeOfList), 2, RoundingMode.HALF_EVEN));

    stockHistoryLine.setPriceIncStockMovePeriod(
        totalStockHistoryLine
            .getPriceIncStockMovePeriod()
            .divide(BigDecimal.valueOf(sizeOfList), 2, RoundingMode.HALF_EVEN));

    stockHistoryLine.setCountOutMvtStockPeriod(
        totalStockHistoryLine.getCountOutMvtStockPeriod() / sizeOfList);

    stockHistoryLine.setSumOutQtyPeriod(
        totalStockHistoryLine
            .getSumOutQtyPeriod()
            .divide(BigDecimal.valueOf(sizeOfList), 2, RoundingMode.HALF_EVEN));

    stockHistoryLine.setPriceOutStockMovePeriod(
        totalStockHistoryLine
            .getPriceOutStockMovePeriod()
            .divide(BigDecimal.valueOf(sizeOfList), 2, RoundingMode.HALF_EVEN));

    return stockHistoryLine;
  }
}
