package com.axelor.apps.stock.web;

import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.stock.db.PickedProduct;
import com.axelor.apps.stock.db.repo.PickedProductRepository;
import com.axelor.apps.stock.service.PickedProductService;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;

public class PickedProductController {

  public void realizePicking(ActionRequest request, ActionResponse response) {
    try {
      PickedProduct pickedProduct = request.getContext().asType(PickedProduct.class);
      pickedProduct = Beans.get(PickedProductRepository.class).find(pickedProduct.getId());
      Beans.get(PickedProductService.class)
          .createStockMoveAndStockMoveLine(pickedProduct.getMassStockMove(), pickedProduct);
      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void cancelPicking(ActionRequest request, ActionResponse response) {
    try {
      PickedProduct pickedProduct = request.getContext().asType(PickedProduct.class);
      pickedProduct = Beans.get(PickedProductRepository.class).find(pickedProduct.getId());
      Beans.get(PickedProductService.class)
          .cancelStockMoveAndStockMoveLine(pickedProduct.getMassStockMove(), pickedProduct);
      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
