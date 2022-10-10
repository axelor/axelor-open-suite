/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2022 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or  modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.axelor.apps.purchase.web;

import com.axelor.apps.base.service.BaseChartService;
import com.axelor.apps.purchase.service.PurchaseChartService;
import com.axelor.exception.AxelorException;
import com.axelor.inject.Beans;
import com.axelor.meta.loader.XMLViews;
import com.axelor.meta.schema.actions.ActionView.ActionViewBuilder;
import com.axelor.meta.schema.views.ChartView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import java.util.List;
import java.util.Map;

public class PurchaseChartController {
  @SuppressWarnings("unchecked")
  public void chartOnClick(ActionRequest request, ActionResponse response) throws AxelorException {

    Map<String, Object> data = request.getData();
    Map<String, Object> context = (Map<String, Object>) data.get("context");
    if (!context.containsKey("_chart")) {
      return;
    }
    String chartName = context.get("_chart").toString();
    ChartView chartView = (ChartView) XMLViews.findView(chartName, "chart");
    if (chartView == null) {
      return;
    }

    List<Long> ids = Beans.get(PurchaseChartService.class).getIdList(context, chartView);
    ActionViewBuilder actionViewBuilder =
        Beans.get(BaseChartService.class).getActionView(chartView);
    actionViewBuilder.context("ids", ids);
    response.setView(actionViewBuilder.map());
  }
}
