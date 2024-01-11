package com.axelor.apps.stock.web;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.stock.db.MassStockMove;
import com.axelor.apps.stock.db.MassStockMoveNeed;
import com.axelor.apps.stock.db.StockMoveLine;
import com.axelor.apps.stock.db.repo.MassStockMoveRepository;
import com.axelor.apps.stock.db.repo.StockMoveRepository;
import com.axelor.apps.stock.exception.StockExceptionMessage;
import com.axelor.apps.stock.service.MassStockMoveService;
import com.axelor.apps.stock.service.PickedProductService;
import com.axelor.common.ObjectUtils;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

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
      int errors = Beans.get(MassStockMoveService.class).cancelPicking(massStockMove);
      if (errors > 0) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
            I18n.get(StockExceptionMessage.MASS_STOCK_MOVE_LINE_ALREADY_STORED));
      }
      massStockMove = Beans.get(MassStockMoveRepository.class).find(massStockMove.getId());
      Beans.get(MassStockMoveService.class).setStatusSelectToDraft(massStockMove);
      response.setReload(true);
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

  public void fetchStockMoveLines(ActionRequest request, ActionResponse response) {
    String domain = "";
    MassStockMove massStockMove = request.getContext().asType(MassStockMove.class);
    massStockMove = Beans.get(MassStockMoveRepository.class).find(massStockMove.getId());
    if (massStockMove.getCommonFromStockLocation() != null
        && massStockMove.getCommonToStockLocation() != null) {
      domain =
          "(self.stockMove.statusSelect = "
              + StockMoveRepository.STATUS_REALIZED
              + " AND self.stockMove.toStockLocation.id = "
              + massStockMove.getCommonFromStockLocation().getId().toString()
              + " ) OR ( self.stockMove.statusSelect = "
              + StockMoveRepository.STATUS_PLANNED
              + " AND self.stockMove.fromStockLocation.id = "
              + massStockMove.getCommonToStockLocation().getId().toString()
              + ")";
    } else if (massStockMove.getCommonFromStockLocation() != null) {
      domain =
          "self.stockMove.statusSelect = "
              + StockMoveRepository.STATUS_REALIZED
              + " AND self.stockMove.toStockLocation.id = "
              + massStockMove.getCommonFromStockLocation().getId().toString();
    } else if (massStockMove.getCommonToStockLocation() != null) {
      domain =
          "self.stockMove.statusSelect = "
              + StockMoveRepository.STATUS_PLANNED
              + " AND self.stockMove.fromStockLocation.id = "
              + massStockMove.getCommonToStockLocation().getId().toString();
    } else {
      response.setAlert(I18n.get(StockExceptionMessage.LOCATIONS_ARE_EMPTY));
    }
    response.setView(
        ActionView.define(I18n.get("Stock move lines to fetch"))
            .model(StockMoveLine.class.getName())
            .add("grid", "stock-move-line-for-mass-stock-move-grid")
            .param("popup", "reload")
            .param("show-toolbar", "true")
            .param("show-confirm", "false")
            .param("popup-save", "false")
            .domain(domain)
            .context("massStockMoveId", massStockMove.getId())
            .map());
  }

  public void fillProductToMove(ActionRequest request, ActionResponse response) {
    int massStockMoveId = (int) request.getContext().get("massStockMoveId");
    MassStockMove massStockMove =
        Beans.get(MassStockMoveRepository.class).find((long) massStockMoveId);
    Context context = request.getContext();
    if (!ObjectUtils.isEmpty(context.get("_ids"))) {
      List<Long> stockMoveLinesIdList =
          (List)
              (((List) context.get("_ids"))
                  .stream()
                      .filter(ObjectUtils::notEmpty)
                      .map(input -> Long.parseLong(input.toString()))
                      .collect(Collectors.toList()));
      Beans.get(MassStockMoveService.class)
          .useStockMoveLinesIdsToCreateMassStockMoveNeeds(massStockMove, stockMoveLinesIdList);
      response.setCanClose(true);
    }
  }

  public void generatePickedLines(ActionRequest request, ActionResponse response) {
    MassStockMove massStockMove = request.getContext().asType(MassStockMove.class);
    massStockMove = Beans.get(MassStockMoveRepository.class).find(massStockMove.getId());
    for (MassStockMoveNeed massStockMoveNeed : massStockMove.getProductToMoveList()) {
      Beans.get(PickedProductService.class)
          .createPickedProductFromMassStockMoveNeed(massStockMoveNeed);
    }
    Beans.get(MassStockMoveService.class).clearProductToMoveList(massStockMove);
    response.setReload(true);
  }
}
