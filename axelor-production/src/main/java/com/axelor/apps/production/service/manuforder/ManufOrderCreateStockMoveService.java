package com.axelor.apps.production.service.manuforder;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.production.db.ManufOrder;
import com.axelor.apps.stock.db.StockLocation;
import com.axelor.apps.stock.db.StockMove;

public interface ManufOrderCreateStockMoveService {

  StockMove _createToProduceStockMove(
      ManufOrder manufOrder,
      Company company,
      StockLocation virtualStockLocation,
      StockLocation producedProductStockLocation)
      throws AxelorException;

  StockMove _createToConsumeStockMove(
      ManufOrder manufOrder,
      Company company,
      StockLocation fromStockLocation,
      StockLocation virtualStockLocation)
      throws AxelorException;

  StockMove _createToProduceIncomingOutsourceStockMove(
      ManufOrder manufOrder,
      Company company,
      StockLocation fromStockLocation,
      StockLocation toStockLocation)
      throws AxelorException;

  StockMove _createToProduceProductionStockMove(
      ManufOrder manufOrder,
      Company company,
      StockLocation virtualStockLocation,
      StockLocation producedProductStockLocation)
      throws AxelorException;

  StockMove _createToConsumeOutgoingOutsourceStockMove(
      ManufOrder manufOrder,
      Company company,
      StockLocation fromStockLocation,
      StockLocation virtualStockLocation)
      throws AxelorException;
}
