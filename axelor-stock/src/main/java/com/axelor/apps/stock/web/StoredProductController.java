package com.axelor.apps.stock.web;

import com.axelor.apps.stock.db.MassStockMove;
import com.axelor.apps.stock.db.StoredProduct;
import com.axelor.apps.stock.service.massstockmove.MassStockMovableProductAttrsService;
import com.axelor.apps.stock.service.massstockmove.StoredProductAttrsService;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;

public class StoredProductController {

  public void setFromStockLocationDomain(ActionRequest request, ActionResponse response) {
    var massStockMove = request.getContext().getParent().asType(MassStockMove.class);

    if (massStockMove != null) {
      response.setAttr(
          "fromStockLocation",
          "domain",
          Beans.get(MassStockMovableProductAttrsService.class)
              .getStockLocationDomain(massStockMove));
    }
  }

  public void setStoredProductDomain(ActionRequest request, ActionResponse response) {
    var massStockMove = request.getContext().getParent().asType(MassStockMove.class);

    if (massStockMove != null) {
      response.setAttr(
          "storedProduct",
          "domain",
          Beans.get(StoredProductAttrsService.class).getStoredProductDomain(massStockMove));
    }
  }

  public void setTrackingNumberDomain(ActionRequest request, ActionResponse response) {
    var storedProduct = request.getContext().asType(StoredProduct.class);
    var massStockMove = request.getContext().getParent().asType(MassStockMove.class);

    if (massStockMove != null) {
      response.setAttr(
          "trackingNumber",
          "domain",
          Beans.get(StoredProductAttrsService.class)
              .getTrackingNumberDomain(storedProduct, massStockMove));
    }
  }
}
