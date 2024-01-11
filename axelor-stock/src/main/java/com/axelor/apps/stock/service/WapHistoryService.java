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

import com.axelor.apps.stock.db.StockLocationLine;
import com.axelor.apps.stock.db.StockLocationLineHistory;
import com.axelor.apps.stock.db.StockMoveLine;
import com.axelor.apps.stock.db.WapHistory;
import java.time.LocalDate;

public interface WapHistoryService {

  /**
   * Create a new wap history line in given stock location line. The WAP ({@link
   * StockLocationLine#avgPrice} must already be updated in the stock location line.
   *
   * <p>The origin will be {@link
   * com.axelor.apps.stock.db.repo.WapHistoryRepository#ORIGIN_MANUAL_CORRECTION}
   *
   * @param stockLocationLine a stock location line with updated WAP
   * @deprecated This method must no longer be used, as WapHistory is deprecated, please use {@link
   *     StockLocationLineHistory}
   * @return the saved wap history
   */
  @Deprecated
  WapHistory saveWapHistory(StockLocationLine stockLocationLine);

  /**
   * Create a new wap history line in given stock location line. The WAP ({@link
   * StockLocationLine#avgPrice} must already be updated in the stock location line.
   *
   * <p>Set the origin using given stock move line.
   *
   * @param stockLocationLine a stock location line with updated WAP
   * @param stockMoveLine the stock move line that caused the WAP change
   * @deprecated This method must no longer be used, as WapHistory is deprecated, please use {@link
   *     StockLocationLineHistory}
   * @return the saved wap history
   */
  @Deprecated
  WapHistory saveWapHistory(StockLocationLine stockLocationLine, StockMoveLine stockMoveLine);

  /**
   * Same as {@link #saveWapHistory(StockLocationLine, StockMoveLine)}, but with a personalized date
   * instead of today date and origin
   *
   * @param stockLocationLine
   * @param stockMoveLine
   * @param date
   * @deprecated This method must no longer be used, as WapHistory is deprecated, please use {@link
   *     StockLocationLineHistory}
   * @return
   */
  @Deprecated
  WapHistory saveWapHistory(StockLocationLine stockLocationLine, LocalDate date, String origin);
}
