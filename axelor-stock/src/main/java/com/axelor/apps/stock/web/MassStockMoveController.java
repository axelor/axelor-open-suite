package com.axelor.apps.stock.web;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.stock.db.MassStockMove;
import com.axelor.apps.stock.service.massstockmove.MassStockMovableProductCancelService;
import com.axelor.apps.stock.service.massstockmove.MassStockMovableProductRealizeService;
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

    Beans.get(MassStockMovableProductRealizeService.class)
        .realize(massStockMove.getPickedProductList());

    response.setReload(true);
  }

  public void realizeAllStoring(ActionRequest request, ActionResponse response)
      throws AxelorException {
    var massStockMove = request.getContext().asType(MassStockMove.class);

    Beans.get(MassStockMovableProductRealizeService.class)
        .realize(massStockMove.getStoredProductList());

    response.setReload(true);
  }

  public void cancelAllPicking(ActionRequest request, ActionResponse response)
      throws AxelorException {
    var massStockMove = request.getContext().asType(MassStockMove.class);

    Beans.get(MassStockMovableProductCancelService.class)
        .cancel(massStockMove.getPickedProductList());

    response.setReload(true);
  }

  public void cancelAllStoring(ActionRequest request, ActionResponse response)
      throws AxelorException {
    var massStockMove = request.getContext().asType(MassStockMove.class);

    Beans.get(MassStockMovableProductCancelService.class)
        .cancel(massStockMove.getStoredProductList());

    response.setReload(true);
  }
}
