package com.axelor.apps.supplychain.service;

import com.axelor.apps.stock.db.StockMove;
import com.axelor.apps.stock.db.StockMoveLine;
import com.axelor.exception.AxelorException;
import com.google.inject.Inject;
import java.util.List;
import java.util.stream.Collectors;

public class StockMoveReservedQtyServiceImpl implements StockMoveReservedQtyService {

  protected ReservedQtyService reservedQtyService;

  @Inject
  public StockMoveReservedQtyServiceImpl(ReservedQtyService reservedQtyService) {
    this.reservedQtyService = reservedQtyService;
  }

  @Override
  public void allocateAll(StockMove stockMove) throws AxelorException {
    if (stockMove.getStockMoveLineList() == null) {
      return;
    }
    List<StockMoveLine> stockMoveLineToAllocateList =
        stockMove
            .getStockMoveLineList()
            .stream()
            .filter(stockMoveLine -> stockMoveLine.getRealQty().signum() != 0)
            .collect(Collectors.toList());
    for (StockMoveLine stockMoveLine : stockMoveLineToAllocateList) {
      reservedQtyService.allocateAll(stockMoveLine);
    }
  }
}
