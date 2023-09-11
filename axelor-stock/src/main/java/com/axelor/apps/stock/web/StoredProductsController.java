package com.axelor.apps.stock.web;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.stock.db.StoredProducts;
import com.axelor.apps.stock.db.repo.StoredProductsRepository;
import com.axelor.apps.stock.service.StoredProductsService;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;

public class StoredProductsController {

  public void realizeStorage(ActionRequest request, ActionResponse response) {
    try {
      StoredProducts storedProducts = request.getContext().asType(StoredProducts.class);
      storedProducts = Beans.get(StoredProductsRepository.class).find(storedProducts.getId());
      Beans.get(StoredProductsService.class).createStockMoveAndStockMoveLine(storedProducts);
      response.setReload(true);
    } catch (AxelorException e) {
      TraceBackService.trace(response, e);
    }
  }

  public void cancelStorage(ActionRequest request, ActionResponse response) throws AxelorException {
    StoredProducts storedProducts = request.getContext().asType(StoredProducts.class);
    storedProducts = Beans.get(StoredProductsRepository.class).find(storedProducts.getId());
    Beans.get(StoredProductsService.class).cancelStockMoveAndStockMoveLine(storedProducts);
    response.setReload(true);
  }
}
