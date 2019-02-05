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
package com.axelor.apps.stock.service.stockmove.print;

import com.axelor.apps.report.engine.ReportSettings;
import com.axelor.apps.stock.db.StockMove;
import com.axelor.exception.AxelorException;
import java.io.File;
import java.io.IOException;
import java.util.List;

public interface PickingStockMovePrintService {

  /**
   * Print a list of stock moves in the same output.
   *
   * @param ids ids of the stock move.
   * @param userType
   * @return the link to the generated file.
   * @throws IOException
   */
  String printStockMoves(List<Long> ids, String userType) throws IOException;

  ReportSettings prepareReportSettings(StockMove stockMove, String format) throws AxelorException;

  File print(StockMove stockMove, String format) throws AxelorException;

  String printStockMove(StockMove stockMove, String format, String userType)
      throws AxelorException, IOException;

  String getFileName(StockMove stockMove);
}
