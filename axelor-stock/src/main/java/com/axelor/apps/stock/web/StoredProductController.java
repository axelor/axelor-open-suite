package com.axelor.apps.stock.web;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.stock.db.MassStockMove;
import com.axelor.apps.stock.db.StoredProduct;
import com.axelor.apps.stock.db.repo.StoredProductRepository;
import com.axelor.apps.stock.service.massstockmove.MassStockMovableProductAttrsService;
import com.axelor.apps.stock.service.massstockmove.MassStockMovableProductCancelService;
import com.axelor.apps.stock.service.massstockmove.MassStockMovableProductQuantityService;
import com.axelor.apps.stock.service.massstockmove.MassStockMovableProductRealizeService;
import com.axelor.apps.stock.service.massstockmove.StoredProductAttrsService;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import java.util.Optional;

public class StoredProductController {

  public void setToStockLocationDomain(ActionRequest request, ActionResponse response) {
    var massStockMove = request.getContext().getParent().asType(MassStockMove.class);

    if (massStockMove != null) {
      response.setAttr(
          "toStockLocation",
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

  public void realizeStoring(ActionRequest request, ActionResponse response)
      throws AxelorException {
    var storedProduct =
        Optional.of(request.getContext().asType(StoredProduct.class))
            .map(sp -> Beans.get(StoredProductRepository.class).find(sp.getId()));

    if (storedProduct.isPresent()) {
      Beans.get(MassStockMovableProductRealizeService.class).realize(storedProduct.get());
    }

    response.setReload(true);
  }

  public void cancelStoring(ActionRequest request, ActionResponse response) throws AxelorException {
    var storedProduct =
        Optional.of(request.getContext().asType(StoredProduct.class))
            .map(sp -> Beans.get(StoredProductRepository.class).find(sp.getId()));

    if (storedProduct.isPresent()) {
      Beans.get(MassStockMovableProductCancelService.class).cancel(storedProduct.get());
    }

    response.setReload(true);
  }

  public void setCurrentQty(ActionRequest request, ActionResponse response) throws AxelorException {
    var storedProduct = request.getContext().asType(StoredProduct.class);
    var massStockMove = request.getContext().getParent().asType(MassStockMove.class);

    response.setValue(
        "currentQty",
        Beans.get(MassStockMovableProductQuantityService.class)
            .getCurrentAvailableQty(storedProduct, massStockMove.getCartStockLocation()));
  }
}
