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
   * @return the link to the generated file.
   * @throws IOException
   */
  String printStockMoves(List<Long> ids) throws IOException;

  ReportSettings prepareReportSettings(StockMove stockMove, String format) throws AxelorException;

  File print(StockMove stockMove, String format) throws AxelorException;

  String printStockMove(StockMove stockMove, String format) throws AxelorException, IOException;

  String getFileName(StockMove stockMove);
}
