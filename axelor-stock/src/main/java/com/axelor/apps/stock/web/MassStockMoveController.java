package com.axelor.apps.stock.web;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.stock.db.MassStockMove;
import com.axelor.apps.stock.service.massstockmove.MassStockMovableProductService;
import com.axelor.apps.stock.service.massstockmove.MassStockMoveRecordService;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;

public class MassStockMoveController {

  public void onNew(ActionRequest request, ActionResponse response) {

    var massStockMove = request.getContext().asType(MassStockMove.class);

    Beans.get(MassStockMoveRecordService.class).onNew(massStockMove);

    response.setValues(massStockMove);
  }

  public void realizeAllPicking(ActionRequest request, ActionResponse response)
      throws AxelorException {
    var massStockMove = request.getContext().asType(MassStockMove.class);

    Beans.get(MassStockMovableProductService.class).realize(massStockMove.getPickedProductList());

    response.setReload(true);
  }
}
