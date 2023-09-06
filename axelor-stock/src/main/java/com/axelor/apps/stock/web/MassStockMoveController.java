package com.axelor.apps.stock.web;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.stock.db.MassStockMove;
import com.axelor.apps.stock.db.PickedProducts;
import com.axelor.apps.stock.db.StoredProducts;
import com.axelor.apps.stock.db.repo.MassStockMoveRepository;
import com.axelor.apps.stock.exception.StockExceptionMessage;
import com.axelor.apps.stock.service.MassStockMoveService;
import com.axelor.apps.stock.service.PickedProductsService;
import com.axelor.apps.stock.service.StoredProductsService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;

public class MassStockMoveController {

  public void importProductFromStockLocation(ActionRequest request, ActionResponse response)
      throws AxelorException {
    MassStockMove massStockMove = request.getContext().asType(MassStockMove.class);
    massStockMove = Beans.get(MassStockMoveRepository.class).find(massStockMove.getId());
    Beans.get(MassStockMoveService.class).importProductFromStockLocation(massStockMove);
    response.setReload(true);
  }

  @Transactional
  public void realizePicking(ActionRequest request, ActionResponse response) {
    MassStockMove massStockMove = request.getContext().asType(MassStockMove.class);
    massStockMove = Beans.get(MassStockMoveRepository.class).find(massStockMove.getId());
    boolean isQtyEqualZero = false;
    boolean isGreaterThan = false;
    for (PickedProducts pickedProducts : massStockMove.getPickedProductsList()) {
      if (pickedProducts.getPickedQty().compareTo(BigDecimal.ZERO) == 0) {
        isQtyEqualZero = true;
      }
      if (pickedProducts.getPickedQty().compareTo(pickedProducts.getCurrentQty()) == 1) {
        isGreaterThan = true;
      }
      try {
        Beans.get(PickedProductsService.class)
            .createStockMoveAndStockMoveLine(massStockMove, pickedProducts);
      } catch (AxelorException e) {
        TraceBackService.trace(e);
      }
    }
    if (isQtyEqualZero && isGreaterThan) {
      response.setAlert(
          I18n.get(StockExceptionMessage.AT_LEAST_ONE_PICKED_QUANTITY_IS_ZERO)
              + ". "
              + I18n.get(
                  StockExceptionMessage.AT_LEAST_ONE_PICKED_QUANTITY_GREATER_THAN_CURRENT_QTY));
    } else if (isQtyEqualZero) {
      response.setAlert(I18n.get(StockExceptionMessage.AT_LEAST_ONE_PICKED_QUANTITY_IS_ZERO));
    } else if (isGreaterThan) {
      response.setAlert(
          I18n.get(StockExceptionMessage.AT_LEAST_ONE_PICKED_QUANTITY_GREATER_THAN_CURRENT_QTY));
    }
    response.setReload(true);
  }

  public void cancelPicking(ActionRequest request, ActionResponse response) {
    MassStockMove massStockMove = request.getContext().asType(MassStockMove.class);
    massStockMove = Beans.get(MassStockMoveRepository.class).find(massStockMove.getId());
    for (PickedProducts pickedProducts : massStockMove.getPickedProductsList()) {
      Beans.get(PickedProductsService.class)
          .cancelStockMoveAndStockMoveLine(massStockMove, pickedProducts);
    }
    Beans.get(MassStockMoveService.class).setStatusSelectToDraft(massStockMove);
    response.setReload(true);
  }

  public void getSequence(ActionRequest request, ActionResponse response) throws AxelorException {
    MassStockMove massStockMove = request.getContext().asType(MassStockMove.class);
    massStockMove = Beans.get(MassStockMoveRepository.class).find(massStockMove.getId());
    String sequence =
        Beans.get(MassStockMoveService.class)
            .getAndSetSequence(massStockMove.getCompany(), massStockMove);
    response.setValue("sequence", sequence);
  }

  public void realizeStorage(ActionRequest request, ActionResponse response)
      throws AxelorException {
    MassStockMove massStockMove = request.getContext().asType(MassStockMove.class);
    massStockMove = Beans.get(MassStockMoveRepository.class).find(massStockMove.getId());
    boolean isGreaterThan = false;
    boolean isQtyEqualZero = false;
    for (StoredProducts storedProducts : massStockMove.getStoredProductsList()) {
      if (storedProducts.getStoredQty().compareTo(storedProducts.getCurrentQty()) == 1) {
        isGreaterThan = true;
      }
      if (storedProducts.getStoredQty().compareTo(BigDecimal.ZERO) == 0) {
        isQtyEqualZero = true;
      }
      try {
        Beans.get(StoredProductsService.class).createStockMoveAndStockMoveLine(storedProducts);
      } catch (AxelorException e) {
        TraceBackService.trace(e);
      }
    }
    if (isQtyEqualZero && isGreaterThan) {
      response.setAlert(
          I18n.get(StockExceptionMessage.AT_LEAST_ONE_STORED_QUANTITY_IS_ZERO)
              + ". "
              + I18n.get(
                  StockExceptionMessage.AT_LEAST_ONE_STORED_QUANTITY_GREATER_THAN_CURRENT_QTY));
    } else if (isQtyEqualZero) {
      response.setAlert(I18n.get(StockExceptionMessage.AT_LEAST_ONE_STORED_QUANTITY_IS_ZERO));
    } else if (isGreaterThan) {
      response.setAlert(
          I18n.get(StockExceptionMessage.AT_LEAST_ONE_STORED_QUANTITY_GREATER_THAN_CURRENT_QTY));
    }
    response.setReload(true);
  }

  public void cancelStorage(ActionRequest request, ActionResponse response) throws AxelorException {
    MassStockMove massStockMove = request.getContext().asType(MassStockMove.class);
    massStockMove = Beans.get(MassStockMoveRepository.class).find(massStockMove.getId());
    for (StoredProducts storedProducts : massStockMove.getStoredProductsList()) {
      Beans.get(StoredProductsService.class).cancelStockMoveAndStockMoveLine(storedProducts);
    }
    response.setReload(true);
  }
}
