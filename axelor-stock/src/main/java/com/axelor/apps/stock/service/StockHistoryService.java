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
