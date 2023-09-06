package com.axelor.apps.stock.web;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.stock.db.PickedProducts;
import com.axelor.apps.stock.db.repo.PickedProductsRepository;
import com.axelor.apps.stock.service.PickedProductsService;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;

public class PickedProductsController {

  public void realizePicking(ActionRequest request, ActionResponse response)
      throws AxelorException {
    PickedProducts pickedProducts = request.getContext().asType(PickedProducts.class);
    pickedProducts = Beans.get(PickedProductsRepository.class).find(pickedProducts.getId());
    try {
      Beans.get(PickedProductsService.class)
          .createStockMoveAndStockMoveLine(pickedProducts.getMassStockMove(), pickedProducts);
    } catch (IllegalStateException e) {
      response.setAlert(e.getMessage());
    }
    response.setReload(true);
  }

  public void cancelPicking(ActionRequest request, ActionResponse response) {
    PickedProducts pickedProducts = request.getContext().asType(PickedProducts.class);
    pickedProducts = Beans.get(PickedProductsRepository.class).find(pickedProducts.getId());
    Beans.get(PickedProductsService.class)
        .cancelStockMoveAndStockMoveLine(pickedProducts.getMassStockMove(), pickedProducts);
    response.setReload(true);
  }
}
