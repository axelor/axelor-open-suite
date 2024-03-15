package com.axelor.apps.production.service.manuforder;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.production.db.ManufOrder;
import com.axelor.apps.stock.db.StockMove;
import com.axelor.apps.stock.db.StockMoveLine;
import com.google.inject.Inject;

public class ManufOrderSetStockMoveLineServiceImpl implements ManufOrderSetStockMoveLineService {

  protected ManufOrderGetStockMoveService manufOrderGetStockMoveService;

  @Inject
  public ManufOrderSetStockMoveLineServiceImpl(
      ManufOrderGetStockMoveService manufOrderGetStockMoveService) {
    this.manufOrderGetStockMoveService = manufOrderGetStockMoveService;
  }

  @Override
  public void setProducedStockMoveLineStockLocation(ManufOrder manufOrder) throws AxelorException {

    if (manufOrder.getProducedStockMoveLineList() != null) {
      StockMove stockMove =
          manufOrderGetStockMoveService.getProducedStockMoveFromManufOrder(manufOrder);

      for (StockMoveLine stockMoveLine : manufOrder.getProducedStockMoveLineList()) {
        if (stockMoveLine.getFromStockLocation() == null) {
          stockMoveLine.setFromStockLocation(stockMove.getFromStockLocation());
        }
        if (stockMoveLine.getToStockLocation() == null) {
          stockMoveLine.setToStockLocation(stockMove.getToStockLocation());
        }
      }
    }
  }

  @Override
  public void setResidualStockMoveLineStockLocation(ManufOrder manufOrder) throws AxelorException {

    if (manufOrder.getResidualStockMoveLineList() != null) {
      StockMove stockMove =
          manufOrderGetStockMoveService.getResidualStockMoveFromManufOrder(manufOrder);

      for (StockMoveLine stockMoveLine : manufOrder.getResidualStockMoveLineList()) {
        if (stockMoveLine.getFromStockLocation() == null) {
          stockMoveLine.setFromStockLocation(stockMove.getFromStockLocation());
        }
        if (stockMoveLine.getToStockLocation() == null) {
          stockMoveLine.setToStockLocation(stockMove.getToStockLocation());
        }
      }
    }
  }

  @Override
  public void setConsumedStockMoveLineStockLocation(ManufOrder manufOrder) throws AxelorException {

    if (manufOrder.getConsumedStockMoveLineList() != null) {
      StockMove stockMove =
          manufOrderGetStockMoveService.getConsumedStockMoveFromManufOrder(manufOrder);

      for (StockMoveLine stockMoveLine : manufOrder.getConsumedStockMoveLineList()) {
        if (stockMoveLine.getFromStockLocation() == null) {
          stockMoveLine.setFromStockLocation(stockMove.getFromStockLocation());
        }
        if (stockMoveLine.getToStockLocation() == null) {
          stockMoveLine.setToStockLocation(stockMove.getToStockLocation());
        }
      }
    }
  }
}
