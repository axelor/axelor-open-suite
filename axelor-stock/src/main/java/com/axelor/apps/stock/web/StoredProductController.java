package com.axelor.apps.stock.web;

import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.stock.db.StoredProduct;
import com.axelor.apps.stock.db.repo.StoredProductRepository;
import com.axelor.apps.stock.service.StoredProductService;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;

public class StoredProductController {

  public void realizeStorage(ActionRequest request, ActionResponse response) {
    try {
      StoredProduct storedProduct = request.getContext().asType(StoredProduct.class);
      storedProduct = Beans.get(StoredProductRepository.class).find(storedProduct.getId());
      Beans.get(StoredProductService.class).createStockMoveAndStockMoveLine(storedProduct);
      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void cancelStorage(ActionRequest request, ActionResponse response) {
    try {
      StoredProduct storedProduct = request.getContext().asType(StoredProduct.class);
      storedProduct = Beans.get(StoredProductRepository.class).find(storedProduct.getId());
      Beans.get(StoredProductService.class).cancelStockMoveAndStockMoveLine(storedProduct);
      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
