package com.axelor.apps.stock.web;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.stock.db.MassStockMove;
import com.axelor.apps.stock.db.PickedProduct;
import com.axelor.apps.stock.db.repo.PickedProductRepository;
import com.axelor.apps.stock.service.massstockmove.MassStockMovableProductAttrsService;
import com.axelor.apps.stock.service.massstockmove.MassStockMovableProductCancelService;
import com.axelor.apps.stock.service.massstockmove.MassStockMovableProductQuantityService;
import com.axelor.apps.stock.service.massstockmove.MassStockMovableProductRealizeService;
import com.axelor.apps.stock.service.massstockmove.PickedProductAttrsService;
import com.axelor.apps.stock.service.massstockmove.PickedProductService;
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
    var pickedProduct = request.getContext().asType(PickedProduct.class);

    var pickedProductInDB =
        Optional.of(request.getContext().asType(PickedProduct.class))
            .map(pp -> Beans.get(PickedProductRepository.class).find(pp.getId()))
            .map(
                productInDb ->
                    Beans.get(PickedProductService.class).copy(pickedProduct, productInDb))
            .orElse(pickedProduct);

    Beans.get(MassStockMovableProductRealizeService.class).realize(pickedProductInDB);
    response.setReload(true);
  }

  public void cancelPicking(ActionRequest request, ActionResponse response) throws AxelorException {
    var pickedProduct =
        Optional.of(request.getContext().asType(PickedProduct.class))
            .map(pp -> Beans.get(PickedProductRepository.class).find(pp.getId()));

    if (pickedProduct.isPresent()) {
      Beans.get(MassStockMovableProductCancelService.class).cancel(pickedProduct.get());
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

  public void setPickedProductDomain(ActionRequest request, ActionResponse response) {
    var pickedProduct = request.getContext().asType(PickedProduct.class);

    if (pickedProduct != null) {
      response.setAttr(
          "pickedProduct",
          "domain",
          Beans.get(PickedProductAttrsService.class).getPickedProductDomain(pickedProduct));
    }
  }

  public void setTrackingNumberDomain(ActionRequest request, ActionResponse response) {
    var pickedProduct = request.getContext().asType(PickedProduct.class);

    if (pickedProduct != null) {
      response.setAttr(
          "trackingNumber",
          "domain",
          Beans.get(PickedProductAttrsService.class).getTrackingNumberDomain(pickedProduct));
    }
  }
}
