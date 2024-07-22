package com.axelor.apps.stock.web;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.stock.db.MassStockMove;
import com.axelor.apps.stock.db.PickedProduct;
import com.axelor.apps.stock.db.repo.PickedProductRepository;
import com.axelor.apps.stock.service.massstockmove.MassStockMovableProductAttrsService;
import com.axelor.apps.stock.service.massstockmove.MassStockMovableProductQuantityService;
import com.axelor.apps.stock.service.massstockmove.MassStockMovableProductService;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import java.util.Optional;

public class PickedProductController {

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

  public void realizePicking(ActionRequest request, ActionResponse response)
      throws AxelorException {
    var pickedProduct =
        Optional.of(request.getContext().asType(PickedProduct.class))
            .map(pp -> Beans.get(PickedProductRepository.class).find(pp.getId()));

    if (pickedProduct.isPresent()) {
      Beans.get(MassStockMovableProductService.class).realize(pickedProduct.get());
    }

    response.setReload(true);
  }

  public void cancelPicking(ActionRequest request, ActionResponse response) throws AxelorException {
    var pickedProduct =
        Optional.of(request.getContext().asType(PickedProduct.class))
            .map(pp -> Beans.get(PickedProductRepository.class).find(pp.getId()));

    if (pickedProduct.isPresent()) {
      Beans.get(MassStockMovableProductService.class).cancel(pickedProduct.get());
    }

    response.setReload(true);
  }

  public void setCurrentQty(ActionRequest request, ActionResponse response) throws AxelorException {
    var pickedProduct = request.getContext().asType(PickedProduct.class);

    response.setValue(
        "currentQty",
        Beans.get(MassStockMovableProductQuantityService.class)
            .getCurrentAvailableQty(pickedProduct, pickedProduct.getFromStockLocation()));
  }
}
