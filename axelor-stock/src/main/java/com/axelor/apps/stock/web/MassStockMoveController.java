package com.axelor.apps.stock.web;

import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.stock.db.MassStockMove;
import com.axelor.apps.stock.db.repo.MassStockMoveRepository;
import com.axelor.apps.stock.exception.StockExceptionMessage;
import com.axelor.apps.stock.service.MassStockMoveService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import java.math.BigDecimal;

public class MassStockMoveController {

  public void importProductFromStockLocation(ActionRequest request, ActionResponse response) {
    try {
      MassStockMove massStockMove = request.getContext().asType(MassStockMove.class);
      massStockMove = Beans.get(MassStockMoveRepository.class).find(massStockMove.getId());
      Beans.get(MassStockMoveService.class).importProductFromStockLocation(massStockMove);
      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void realizePicking(ActionRequest request, ActionResponse response) {
    try {
      MassStockMove massStockMove = request.getContext().asType(MassStockMove.class);
      massStockMove = Beans.get(MassStockMoveRepository.class).find(massStockMove.getId());

      if (massStockMove.getPickedProductList().stream()
          .anyMatch(it -> BigDecimal.ZERO.compareTo(it.getPickedQty()) == 0)) {
        response.setAlert(I18n.get(StockExceptionMessage.AT_LEAST_ONE_PICKED_QUANTITY_IS_ZERO));
      }
      if (massStockMove.getPickedProductList().stream()
          .anyMatch(it -> it.getPickedQty().compareTo(it.getCurrentQty()) == 1)) {
        response.setAlert(
            I18n.get(StockExceptionMessage.AT_LEAST_ONE_PICKED_QUANTITY_GREATER_THAN_CURRENT_QTY));
      }
      Beans.get(MassStockMoveService.class).realizePicking(massStockMove);

      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void cancelPicking(ActionRequest request, ActionResponse response) {
    try {
      MassStockMove massStockMove = request.getContext().asType(MassStockMove.class);
      massStockMove = Beans.get(MassStockMoveRepository.class).find(massStockMove.getId());
      Beans.get(MassStockMoveService.class).cancelPicking(massStockMove);
      Beans.get(MassStockMoveService.class).setStatusSelectToDraft(massStockMove);
      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void getSequence(ActionRequest request, ActionResponse response) {
    try {
      MassStockMove massStockMove = request.getContext().asType(MassStockMove.class);
      massStockMove = Beans.get(MassStockMoveRepository.class).find(massStockMove.getId());
      String sequence =
          Beans.get(MassStockMoveService.class)
              .getAndSetSequence(massStockMove.getCompany(), massStockMove);
      response.setValue("sequence", sequence);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void realizeStorage(ActionRequest request, ActionResponse response) {
    try {
      MassStockMove massStockMove = request.getContext().asType(MassStockMove.class);
      massStockMove = Beans.get(MassStockMoveRepository.class).find(massStockMove.getId());

      if (massStockMove.getStoredProductList().stream()
          .anyMatch(it -> BigDecimal.ZERO.compareTo(it.getStoredQty()) == 0)) {
        response.setAlert(I18n.get(StockExceptionMessage.AT_LEAST_ONE_STORED_QUANTITY_IS_ZERO));
      }
      if (massStockMove.getStoredProductList().stream()
          .anyMatch(it -> it.getStoredQty().compareTo(it.getCurrentQty()) == 1)) {
        response.setAlert(
            I18n.get(StockExceptionMessage.AT_LEAST_ONE_STORED_QUANTITY_GREATER_THAN_CURRENT_QTY));
      }

      Beans.get(MassStockMoveService.class).realizeStorage(massStockMove);

      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void cancelStorage(ActionRequest request, ActionResponse response) {
    try {
      MassStockMove massStockMove = request.getContext().asType(MassStockMove.class);
      massStockMove = Beans.get(MassStockMoveRepository.class).find(massStockMove.getId());
      Beans.get(MassStockMoveService.class).cancelStorage(massStockMove);
      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
