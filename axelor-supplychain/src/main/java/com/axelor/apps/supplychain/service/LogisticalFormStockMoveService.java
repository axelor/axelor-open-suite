package com.axelor.apps.supplychain.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.stock.db.LogisticalForm;
import com.axelor.apps.stock.db.StockMove;

public interface LogisticalFormStockMoveService {

  void addStockMoveToLogisticalForm(LogisticalForm logisticalForm, StockMove stockMove)
      throws AxelorException;

  void removeStockMoveFromLogisticalForm(LogisticalForm logisticalForm, StockMove stockMove)
      throws AxelorException;

  String validateAndUpdateStockMoveList(
      LogisticalForm savedLogisticalForm, LogisticalForm currentForm) throws AxelorException;
}
