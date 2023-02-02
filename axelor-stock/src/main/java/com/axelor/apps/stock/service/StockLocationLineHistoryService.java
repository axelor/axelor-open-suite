package com.axelor.apps.stock.service;

import com.axelor.apps.stock.db.StockLocationLine;
import com.axelor.apps.stock.db.StockLocationLineHistory;
import java.time.LocalDateTime;

public interface StockLocationLineHistoryService {

  /**
   * Method that create a stock location line history.
   *
   * @param stockLocationLine
   * @param date
   * @param origin
   * @return StockLocationLineHistory created.
   */
  StockLocationLineHistory saveHistory(
      StockLocationLine stockLocationLine,
      LocalDateTime dateTime,
      String origin,
      String typeSelect);
}
