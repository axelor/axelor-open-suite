package com.axelor.apps.stock.listeners;

import com.axelor.apps.stock.db.StockMove;
import com.axelor.apps.stock.db.repo.StockMoveRepository;
import com.axelor.apps.stock.service.StockMoveService;
import com.axelor.exception.service.TraceBackService;
import com.axelor.inject.Beans;
import javax.persistence.PostPersist;
import javax.persistence.PostUpdate;

public class StockMoveListener {

  @PostPersist
  @PostUpdate
  private void updateStocks(StockMove stockMove) {
    try {
      System.out.println("SALUT JUPDATE");
      Beans.get(StockMoveService.class)
          .updateStocks(Beans.get(StockMoveRepository.class).find(stockMove.getId()));
    } catch (Exception e) {
      TraceBackService.trace(e);
    }
  }
}
