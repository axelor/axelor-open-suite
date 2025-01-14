package com.axelor.apps.businessproduction.web;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.businessproduction.service.BusinessProjectProdOrderService;
import com.axelor.apps.production.db.ProductionOrder;
import com.axelor.apps.production.exceptions.ProductionExceptionMessage;
import com.axelor.apps.project.db.Project;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.common.base.Joiner;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;

public class ProjectController {

  public void generateProdOrders(ActionRequest request, ActionResponse response)
      throws AxelorException {
    Project project = request.getContext().asType(Project.class);
    List<Long> prodOrderIds =
        Beans.get(BusinessProjectProdOrderService.class).generateProductionOrders(project).stream()
            .map(ProductionOrder::getId)
            .collect(Collectors.toList());

    if (CollectionUtils.isEmpty(prodOrderIds)) {
      response.setInfo(I18n.get(ProductionExceptionMessage.PRODUCTION_ORDER_NO_GENERATION));
      return;
    }

    if (prodOrderIds.size() == 1) {
      response.setView(
          ActionView.define(I18n.get("Production order"))
              .model(ProductionOrder.class.getName())
              .add("form", "production-order-form")
              .add("grid", "production-order-grid")
              .param("search-filters", "production-order-filters")
              .param("forceEdit", "true")
              .context("_showRecord", String.valueOf(prodOrderIds.get(0)))
              .map());
    } else {
      response.setView(
          ActionView.define(I18n.get("Production order"))
              .model(ProductionOrder.class.getName())
              .add("grid", "production-order-grid")
              .add("form", "production-order-form")
              .param("search-filters", "production-order-filters")
              .domain("self.id in (" + Joiner.on(",").join(prodOrderIds) + ")")
              .map());
    }
  }
}
