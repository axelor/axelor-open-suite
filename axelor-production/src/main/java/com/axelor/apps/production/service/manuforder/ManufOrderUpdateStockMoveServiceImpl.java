package com.axelor.apps.production.service.manuforder;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.production.db.ManufOrder;
import com.axelor.apps.stock.db.StockMove;
import com.axelor.apps.stock.db.StockMoveLine;
import com.axelor.apps.stock.service.StockMoveService;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.util.List;

public class ManufOrderUpdateStockMoveServiceImpl implements ManufOrderUpdateStockMoveService {

  protected ManufOrderGetStockMoveService manufOrderGetStockMoveService;
  protected ManufOrderService manufOrderService;
  protected StockMoveService stockMoveService;

  @Inject
  public ManufOrderUpdateStockMoveServiceImpl(
      ManufOrderGetStockMoveService manufOrderGetStockMoveService,
      ManufOrderService manufOrderService,
      StockMoveService stockMoveService) {
    this.manufOrderGetStockMoveService = manufOrderGetStockMoveService;
    this.manufOrderService = manufOrderService;
    this.stockMoveService = stockMoveService;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void updateConsumedStockMoveFromManufOrder(ManufOrder manufOrder) throws AxelorException {
    manufOrderService.updateDiffProdProductList(manufOrder);
    List<StockMoveLine> consumedStockMoveLineList = manufOrder.getConsumedStockMoveLineList();
    if (consumedStockMoveLineList == null) {
      return;
    }
    updateStockMoveFromManufOrder(
        consumedStockMoveLineList,
        manufOrderGetStockMoveService.getConsumedStockMoveFromManufOrder(manufOrder));
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void updateProducedStockMoveFromManufOrder(ManufOrder manufOrder) throws AxelorException {
    List<StockMoveLine> producedStockMoveLineList = manufOrder.getProducedStockMoveLineList();
    if (producedStockMoveLineList == null) {
      return;
    }
    updateStockMoveFromManufOrder(
        producedStockMoveLineList,
        manufOrderGetStockMoveService.getProducedStockMoveFromManufOrder(manufOrder));
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void updateResidualStockMoveFromManufOrder(ManufOrder manufOrder) throws AxelorException {
    List<StockMoveLine> residualStockMoveLineList = manufOrder.getResidualStockMoveLineList();
    if (residualStockMoveLineList == null) {
      return;
    }
    updateStockMoveFromManufOrder(
        residualStockMoveLineList,
        manufOrderGetStockMoveService.getResidualStockMoveFromManufOrder(manufOrder));
  }

  @Override
  public void updateStockMoveFromManufOrder(
      List<StockMoveLine> stockMoveLineList, StockMove stockMove) throws AxelorException {
    if (stockMoveLineList == null) {
      return;
    }

    // add missing lines in stock move
    stockMoveLineList.stream()
        .filter(stockMoveLine -> stockMoveLine.getStockMove() == null)
        .forEach(stockMove::addStockMoveLineListItem);

    // remove lines in stock move removed in manuf order
    if (stockMove.getStockMoveLineList() != null) {
      stockMove
          .getStockMoveLineList()
          .removeIf(stockMoveLine -> !stockMoveLineList.contains(stockMoveLine));
    }
    // update stock location by cancelling then planning stock move.
    stockMoveService.cancel(stockMove);
    stockMoveService.goBackToDraft(stockMove);
    stockMoveService.plan(stockMove);
  }
}
