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

import com.axelor.apps.stock.db.StockHistoryLine;
import com.axelor.exception.AxelorException;
import java.time.LocalDate;
import java.util.List;

public interface StockHistoryService {

  /**
   * Compute lines for stock history. Compute one line per month between beginDate and endDate and
   * add two lines for average and total.
   *
   * @param productId id of the queried product, cannot be null.
   * @param companyId id of the company used as filter, cannot be null.
   * @param stockLocationId id of the stock location used as filter, cannot be null.
   * @param beginDate mandatory date used for the generation.
   * @param endDate mandatory date used for the generation.
   * @return the computed lines.
   */
  List<StockHistoryLine> computeStockHistoryLineList(
      Long productId, Long companyId, Long stockLocationId, LocalDate beginDate, LocalDate endDate)
      throws AxelorException;
}
