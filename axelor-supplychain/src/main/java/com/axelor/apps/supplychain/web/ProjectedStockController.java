/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2019 Axelor (<http://axelor.com>).
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
package com.axelor.apps.supplychain.web;

import com.axelor.apps.supplychain.db.MrpLine;
import com.axelor.apps.supplychain.db.repo.MrpLineRepository;
import com.axelor.apps.supplychain.db.repo.MrpRepository;
import com.axelor.apps.supplychain.service.ProjectedStockService;
import com.axelor.db.mapper.Mapper;
import com.axelor.exception.service.TraceBackService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ProjectedStockController {

  @SuppressWarnings("unchecked")
  public void showProjectedStock(ActionRequest request, ActionResponse response) {

    try {
      Context context = request.getContext();
      Long productId = 0L;
      Long companyId = 0L;
      Long stockLocationId = 0L;
      String productName;

      LinkedHashMap<String, Object> productHashMap =
          (LinkedHashMap<String, Object>) context.get("product");
      if (productHashMap != null) {
        productId = Long.valueOf(productHashMap.get("id").toString());
        productName = (String) productHashMap.get("fullName");
      } else {
        return;
      }
      LinkedHashMap<String, Object> companyHashMap =
          (LinkedHashMap<String, Object>) context.get("company");
      if (companyHashMap != null) {
        companyId = Long.valueOf(companyHashMap.get("id").toString());
      }
      LinkedHashMap<String, Object> stockLocationHashMap =
          (LinkedHashMap<String, Object>) context.get("stockLocation");
      if (stockLocationHashMap != null) {
        stockLocationId = Long.valueOf(stockLocationHashMap.get("id").toString());
      }

      List<MrpLine> mrpLineList = new ArrayList<>();
      try {
        mrpLineList =
            Beans.get(ProjectedStockService.class)
                .createProjectedStock(productId, companyId, stockLocationId);
        response.setView(
            ActionView.define(productName + I18n.get(" Projected stock"))
                .model(MrpLine.class.getName())
                .add("form", "projected-stock-form")
                .param("forceEdit", "true")
                .param("popup", "true")
                .context("_mrpLineList", mrpLineList)
                .map());
      } catch (Exception e) {
        TraceBackService.trace(response, e);
      } finally {
        if (mrpLineList != null && !mrpLineList.isEmpty()) {
          Long mrpId = mrpLineList.get(0).getId();
          Beans.get(MrpRepository.class).all().filter("self.id = ?1", mrpId).remove();
          Beans.get(MrpLineRepository.class).all().filter("self.mrp.id = ?1", mrpId).remove();
        }
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void showChartProjectedStock(ActionRequest request, ActionResponse response) {
    Context context = request.getContext();
    List<Map<String, Object>> dataList = new ArrayList<>();

    @SuppressWarnings("unchecked")
    Collection<Map<String, Object>> contextMrpLineList =
        (Collection<Map<String, Object>>) context.get("_mrpLineListToProject");

    List<MrpLine> mrpLineList =
        contextMrpLineList
            .stream()
            .map(map -> Mapper.toBean(MrpLine.class, map))
            .collect(Collectors.toList());

    if (!mrpLineList.isEmpty()) {
      List<MrpLine> mrpLineLastList = new ArrayList<>();
      MrpLine lastMrpLine = mrpLineList.get(0);

      for (int i = 1; i < mrpLineList.size(); ++i) {
        MrpLine mrpLine = mrpLineList.get(i);
        if (mrpLine.getMaturityDate().isAfter(lastMrpLine.getMaturityDate())) {
          mrpLineLastList.add(lastMrpLine);
        }
        lastMrpLine = mrpLine;
      }
      mrpLineLastList.add(lastMrpLine);
      lastMrpLine = mrpLineList.get(0);
      LocalDate mrpDate = lastMrpLine.getMaturityDate();
      for (MrpLine mrpLine : mrpLineLastList) {
        mrpDate = addInterElementForProjectedStockChart(dataList, lastMrpLine, mrpDate, mrpLine);
        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("name", mrpLine.getMaturityDate());
        dataMap.put("cumulativeQty", mrpLine.getCumulativeQty());
        dataList.add(dataMap);
        lastMrpLine = mrpLine;
      }
    }
    response.setData(dataList);
  }

  private LocalDate addInterElementForProjectedStockChart(
      List<Map<String, Object>> dataList, MrpLine lastMrpLine, LocalDate mrpDate, MrpLine mrpLine) {
    while (mrpDate.isBefore(mrpLine.getMaturityDate())) {
      mrpDate = mrpDate.plusDays(1);
      Map<String, Object> dataMapDate = new HashMap<>();
      dataMapDate.put("name", mrpDate);
      dataMapDate.put("cumulativeQty", lastMrpLine.getCumulativeQty());
      dataList.add(dataMapDate);
    }
    return mrpDate;
  }
}
