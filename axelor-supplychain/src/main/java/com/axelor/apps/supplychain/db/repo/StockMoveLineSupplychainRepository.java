package com.axelor.apps.supplychain.db.repo;

import com.axelor.apps.stock.db.StockMove;
import com.axelor.apps.stock.db.StockMoveLine;
import com.axelor.apps.stock.db.repo.StockMoveLineStockRepository;
import com.axelor.apps.stock.db.repo.StockMoveRepository;
import com.axelor.apps.supplychain.service.StockMoveLineServiceSupplychain;
import com.axelor.inject.Beans;
import java.util.Map;

public class StockMoveLineSupplychainRepository extends StockMoveLineStockRepository {

  @Override
  public Map<String, Object> populate(Map<String, Object> json, Map<String, Object> context) {
    Long stockMoveLineId = (Long) json.get("id");
    StockMoveLine stockMoveLine = find(stockMoveLineId);
    StockMove stockMove = stockMoveLine.getStockMove();

    Map<String, Object> stockMoveLineMap = super.populate(json, context);
    if (stockMove.getStatusSelect() == StockMoveRepository.STATUS_REALIZED) {
      Beans.get(StockMoveLineServiceSupplychain.class).setInvoiceStatus(stockMoveLine);
      json.put(
          "availableStatus",
          stockMoveLine.getProduct() != null && stockMoveLine.getProduct().getStockManaged()
              ? stockMoveLine.getAvailableStatus()
              : null);
      json.put("availableStatusSelect", stockMoveLine.getAvailableStatusSelect());
    }
    return stockMoveLineMap;
  }
}
