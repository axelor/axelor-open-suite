package com.axelor.apps.stock.web;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.stock.db.MassStockMove;
import com.axelor.apps.stock.db.StockLocation;
import com.axelor.apps.stock.db.StockMoveLine;
import com.axelor.apps.stock.db.repo.MassStockMoveRepository;
import com.axelor.apps.stock.db.repo.StockMoveRepository;
import com.axelor.apps.stock.service.massstockmove.MassStockMovableProductCancelService;
import com.axelor.apps.stock.service.massstockmove.MassStockMovableProductRealizeService;
import com.axelor.apps.stock.service.massstockmove.MassStockMoveNeedService;
import com.axelor.apps.stock.service.massstockmove.MassStockMoveRecordService;
import com.axelor.apps.stock.translation.ITranslation;
import com.axelor.common.ObjectUtils;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class MassStockMoveController {

  public void onNew(ActionRequest request, ActionResponse response) {

    var massStockMove = request.getContext().asType(MassStockMove.class);

    Beans.get(MassStockMoveRecordService.class).onNew(massStockMove);

    response.setValues(massStockMove);
  }

  public void realizeAllPicking(ActionRequest request, ActionResponse response)
      throws AxelorException {
    var massStockMove = request.getContext().asType(MassStockMove.class);

    Beans.get(MassStockMovableProductRealizeService.class)
        .realize(massStockMove.getPickedProductList());

    response.setReload(true);
  }

  public void realizeAllStoring(ActionRequest request, ActionResponse response)
      throws AxelorException {
    var massStockMove = request.getContext().asType(MassStockMove.class);

    Beans.get(MassStockMovableProductRealizeService.class)
        .realize(massStockMove.getStoredProductList());

    response.setReload(true);
  }

  public void cancelAllPicking(ActionRequest request, ActionResponse response)
      throws AxelorException {
    var massStockMove = request.getContext().asType(MassStockMove.class);

    Beans.get(MassStockMovableProductCancelService.class)
        .cancel(massStockMove.getPickedProductList());

    response.setReload(true);
  }

  public void cancelAllStoring(ActionRequest request, ActionResponse response)
      throws AxelorException {
    var massStockMove = request.getContext().asType(MassStockMove.class);

    Beans.get(MassStockMovableProductCancelService.class)
        .cancel(massStockMove.getStoredProductList());

    response.setReload(true);
  }

  public void fetchStockMoveLines(ActionRequest request, ActionResponse response) {

    MassStockMove massStockMove = request.getContext().asType(MassStockMove.class);
    massStockMove = Beans.get(MassStockMoveRepository.class).find(massStockMove.getId());

    response.setView(
        ActionView.define(I18n.get("Stock move lines to select"))
            .model(StockMoveLine.class.getName())
            .add("grid", "stock-move-line-for-mass-stock-move-grid")
            .param("popup", "reload")
            .param("show-toolbar", "true")
            .param("show-confirm", "false")
            .param("popup-save", "false")
            .domain(buildDomain(massStockMove))
            .context("massStockMoveId", massStockMove.getId())
            .map());
  }

  protected String buildDomain(MassStockMove massStockMove) {

    if (massStockMove.getCommonToStockLocation() == null
        && massStockMove.getCommonFromStockLocation() == null) {
      return String.format(
          "(self.stockMove.statusSelect = %d OR self.stockMove.statusSelect = %d) AND self.stockMove.company.id = %d",
          StockMoveRepository.STATUS_PLANNED,
          StockMoveRepository.STATUS_REALIZED,
          massStockMove.getCompany().getId());
    }

    String smRealizedAndFromStockLocation =
        "self.stockMove.statusSelect = "
            + StockMoveRepository.STATUS_REALIZED
            + " AND self.stockMove.toStockLocation.id = "
            + Optional.ofNullable(massStockMove.getCommonFromStockLocation())
                .map(StockLocation::getId)
                .map(Object::toString)
                .orElse("0");
    String smPlannedAndToStockLocation =
        "self.stockMove.statusSelect = "
            + StockMoveRepository.STATUS_PLANNED
            + " AND self.stockMove.fromStockLocation.id = "
            + Optional.ofNullable(massStockMove.getCommonToStockLocation())
                .map(StockLocation::getId)
                .map(Object::toString)
                .orElse("0");

    return String.format(
        "((%s) OR (%s)) AND self.stockMove.company.id = %d",
        smRealizedAndFromStockLocation,
        smPlannedAndToStockLocation,
        massStockMove.getCompany().getId());
  }

  public void fillProductNeed(ActionRequest request, ActionResponse response) {

    var context = request.getContext();
    var massStockMoveId = (int) context.get("massStockMoveId");
    var massStockMove = Beans.get(MassStockMoveRepository.class).find((long) massStockMoveId);
    if (!ObjectUtils.isEmpty(context.get("_ids"))) {
      var stockMoveLinesIdList =
          ((List) context.get("_ids"))
              .stream().map(id -> Long.valueOf((int) id)).collect(Collectors.toList());
      Beans.get(MassStockMoveNeedService.class)
          .createMassStockMoveNeedFromStockMoveLinesId(massStockMove, (List) stockMoveLinesIdList);
      response.setInfo(I18n.get(ITranslation.MASS_STOCK_MOVE_NEED_CREATED));
      response.setCanClose(true);
    }
  }
}
