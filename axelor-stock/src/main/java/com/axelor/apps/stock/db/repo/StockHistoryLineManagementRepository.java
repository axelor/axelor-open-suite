package com.axelor.apps.stock.db.repo;

import com.axelor.apps.stock.db.StockHistoryLine;
import com.google.inject.persist.Transactional;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class StockHistoryLineManagementRepository extends StockHistoryLineRepository {

  @Transactional
  public List<StockHistoryLine> save(List<StockHistoryLine> stockHistoryLineList) {
    Objects.requireNonNull(stockHistoryLineList);

    return stockHistoryLineList.stream().map(this::save).collect(Collectors.toList());
  }
}
