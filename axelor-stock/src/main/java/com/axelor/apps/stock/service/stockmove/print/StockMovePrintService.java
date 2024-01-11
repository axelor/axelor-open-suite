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
package com.axelor.apps.stock.service.stockmove.print;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.report.engine.ReportSettings;
import com.axelor.apps.stock.db.StockMove;
import java.io.File;
import java.io.IOException;
import java.util.List;

public interface StockMovePrintService {

  /**
   * Print a list of stock moves in the same output.
   *
   * @param ids ids of the stock move.
   * @return the link to the generated file.
   * @throws IOException
   */
  String printStockMoves(List<Long> ids) throws IOException;

  ReportSettings prepareReportSettings(StockMove stockMove, String format) throws AxelorException;

  File print(StockMove stockMove, String format) throws AxelorException;

  String printStockMove(StockMove stockMove, String format) throws AxelorException, IOException;

  String getFileName(StockMove stockMove);
}
