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
package com.axelor.apps.stock.service;

import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.stock.db.StockLocationLine;
import com.axelor.apps.stock.db.StockMove;
import com.axelor.apps.stock.db.StockMoveLine;
import com.axelor.apps.stock.db.WapHistory;
import com.axelor.apps.stock.db.repo.WapHistoryRepository;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.time.LocalDate;
import java.util.Optional;

/** Service used for wap history creation in stock location line. */
public class WapHistoryServiceImpl implements WapHistoryService {

  protected WapHistoryRepository wapHistoryRepository;
  protected AppBaseService appBaseService;

  @Inject
  public WapHistoryServiceImpl(
      WapHistoryRepository wapHistoryRepository, AppBaseService appBaseService) {
    this.wapHistoryRepository = wapHistoryRepository;
    this.appBaseService = appBaseService;
  }

  @Override
  @Transactional
  public WapHistory saveWapHistory(
      StockLocationLine stockLocationLine, StockMoveLine stockMoveLine) {
    return wapHistoryRepository.save(createWapHistory(stockLocationLine, stockMoveLine));
  }

  @Override
  @Transactional
  public WapHistory saveWapHistory(
      StockLocationLine stockLocationLine, LocalDate date, String origin) {
    return wapHistoryRepository.save(createWapHistory(stockLocationLine, date, origin));
  }

  @Override
  @Transactional
  public WapHistory saveWapHistory(StockLocationLine stockLocationLine) {
    return wapHistoryRepository.save(createWapHistory(stockLocationLine));
  }

  /** Create a wap history when the wap change is manual, so no stock move line. */
  protected WapHistory createWapHistory(StockLocationLine stockLocationLine) {
    return createWapHistory(stockLocationLine, null, WapHistoryRepository.ORIGIN_MANUAL_CORRECTION);
  }

  /** Create a wap history when the wap change is from a stock move realization. */
  protected WapHistory createWapHistory(
      StockLocationLine stockLocationLine, StockMoveLine stockMoveLine) {
    String origin =
        Optional.ofNullable(stockMoveLine)
            .map(StockMoveLine::getStockMove)
            .map(StockMove::getStockMoveSeq)
            .orElse("");
    return createWapHistory(stockLocationLine, origin);
  }

  protected WapHistory createWapHistory(
      StockLocationLine stockLocationLine, LocalDate date, String origin) {
    WapHistory wapHistory = createWapHistory(stockLocationLine, origin);
    wapHistory.setDate(date);

    return wapHistory;
  }

  /** Use current value of stock location line to create a wap history. */
  protected WapHistory createWapHistory(StockLocationLine stockLocationLine, String origin) {
    LocalDate today =
        appBaseService.getTodayDate(
            stockLocationLine.getStockLocation() != null
                ? stockLocationLine.getStockLocation().getCompany()
                : Optional.ofNullable(AuthUtils.getUser())
                    .map(User::getActiveCompany)
                    .orElse(null));
    return new WapHistory(
        stockLocationLine,
        today,
        stockLocationLine.getAvgPrice(),
        stockLocationLine.getCurrentQty(),
        stockLocationLine.getUnit(),
        origin);
  }
}
