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
import com.axelor.apps.stock.db.StockHistoryLine;
import com.axelor.meta.db.MetaFile;
import java.io.IOException;
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

  MetaFile exportStockHistoryLineList(List<StockHistoryLine> stockHistoryLineList, String fileName)
      throws IOException;

  public String getStockHistoryLineExportName(String productName);

  /**
   * Same as {@link StockHistoryService#computeStockHistoryLineList(Long, Long, Long, LocalDate,
   * LocalDate)} But, this method will save the computed stock history lines
   *
   * @param productId
   * @param companyId
   * @param stockLocationId
   * @param beginDate
   * @param endDate
   * @return
   * @throws AxelorException
   */
  List<StockHistoryLine> computeAndSaveStockHistoryLineList(
      Long productId, Long companyId, Long stockLocationId, LocalDate beginDate, LocalDate endDate)
      throws AxelorException;
}
