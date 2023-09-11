package com.axelor.apps.stock.web;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.stock.db.PickedProducts;
import com.axelor.apps.stock.db.repo.PickedProductsRepository;
import com.axelor.apps.stock.service.PickedProductsService;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;

public class PickedProductsController {

  public void realizePicking(ActionRequest request, ActionResponse response) {
    try {
      PickedProducts pickedProducts = request.getContext().asType(PickedProducts.class);
      pickedProducts = Beans.get(PickedProductsRepository.class).find(pickedProducts.getId());
      Beans.get(PickedProductsService.class)
          .createStockMoveAndStockMoveLine(pickedProducts.getMassStockMove(), pickedProducts);
      response.setReload(true);
    } catch (AxelorException e) {
      TraceBackService.trace(response, e);
    }
  }

  public void cancelPicking(ActionRequest request, ActionResponse response) {
    try {
      PickedProducts pickedProducts = request.getContext().asType(PickedProducts.class);
      pickedProducts = Beans.get(PickedProductsRepository.class).find(pickedProducts.getId());
      Beans.get(PickedProductsService.class)
          .cancelStockMoveAndStockMoveLine(pickedProducts.getMassStockMove(), pickedProducts);
      response.setReload(true);
    } catch (AxelorException e) {
      TraceBackService.trace(response, e);
    }
  }
}
