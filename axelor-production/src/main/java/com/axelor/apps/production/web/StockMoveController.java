package com.axelor.apps.production.web;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.production.service.StockMoveProductionService;
import com.axelor.apps.stock.db.StockMove;
import com.axelor.apps.stock.db.repo.StockMoveRepository;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;

public class StockMoveController {

  public void cancelFromManufOrder(ActionRequest request, ActionResponse response)
      throws AxelorException {
    StockMove stockMove = request.getContext().asType(StockMove.class);
    Beans.get(StockMoveProductionService.class)
        .cancelFromManufOrder(
            Beans.get(StockMoveRepository.class).find(stockMove.getId()),
            stockMove.getCancelReason());
    response.setCanClose(true);
  }
}
