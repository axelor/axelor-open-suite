/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2025 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
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
