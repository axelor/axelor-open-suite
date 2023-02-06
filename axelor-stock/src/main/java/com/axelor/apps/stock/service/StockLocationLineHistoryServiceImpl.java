package com.axelor.apps.stock.service;

import com.axelor.apps.stock.db.StockLocationLine;
import com.axelor.apps.stock.db.StockLocationLineHistory;
import com.axelor.apps.stock.db.repo.StockLocationLineHistoryRepository;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.time.LocalDateTime;

public class StockLocationLineHistoryServiceImpl implements StockLocationLineHistoryService {

  protected StockLocationLineHistoryRepository stockLocationLineHistoryRepo;

  @Inject
  public StockLocationLineHistoryServiceImpl(
      StockLocationLineHistoryRepository stockLocationLineHistoryRepo) {
    this.stockLocationLineHistoryRepo = stockLocationLineHistoryRepo;
  }

  @Override
  @Transactional
  public StockLocationLineHistory saveHistory(
      StockLocationLine stockLocationLine,
      LocalDateTime dateTime,
      String origin,
      String typeSelect) {

    return stockLocationLineHistoryRepo.save(
        new StockLocationLineHistory(
            stockLocationLine,
            typeSelect,
            dateTime,
            origin,
            stockLocationLine.getAvgPrice(),
            stockLocationLine.getCurrentQty(),
            stockLocationLine.getUnit()));
  }
}
