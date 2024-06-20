package com.axelor.apps.stock.web;

import com.axelor.apps.stock.db.MassStockMove;
import com.axelor.apps.stock.db.PickedProduct;
import com.axelor.apps.stock.service.massstockmove.PickedProductAttrsService;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;

public class PickedProductController {

  public void setFromStockLocationDomain(ActionRequest request, ActionResponse response) {
    var pickedProduct = request.getContext().asType(PickedProduct.class);
    var massStockMove = request.getContext().getParent().asType(MassStockMove.class);

    if (massStockMove != null) {
      response.setAttr(
          "fromStockLocation",
          "domain",
          Beans.get(PickedProductAttrsService.class)
              .getStockLocationDomain(pickedProduct, massStockMove));
    }
  }
}
