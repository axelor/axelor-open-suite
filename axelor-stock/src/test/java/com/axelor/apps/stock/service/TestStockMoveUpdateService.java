package com.axelor.apps.stock.service;

import com.axelor.apps.stock.db.StockMove;
import com.axelor.exception.AxelorException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TestStockMoveUpdateService {

  private StockMoveUpdateService stockMoveUpdateService;

  @Before
  public void prepare() {
    stockMoveUpdateService = new StockMoveUpdateServiceImpl(null, null, null, null, null, null);
  }

  @Test
  public void testUpdateUnknownStatus() {
    StockMove stockMove = new StockMove();
    stockMove.setStatusSelect(5);
    int status = 6;
    String errorMessage = null;
    try {
      stockMoveUpdateService.updateStatus(stockMove, status);
    } catch (AxelorException e) {
      errorMessage = e.getMessage();
    }
    Assert.assertEquals(
        "Workflow to update status to value 6 is not supported for stock move.", errorMessage);
  }
}
