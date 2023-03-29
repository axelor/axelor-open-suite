/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2023 Axelor (<http://axelor.com>).
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
package com.axelor.apps.bpm.web;

import com.axelor.apps.base.db.Wizard;
import com.axelor.apps.bpm.db.WkfModel;
import com.axelor.apps.bpm.db.WkfProcess;
import com.axelor.apps.bpm.db.WkfProcessConfig;
import com.axelor.apps.bpm.db.repo.WkfModelRepository;
import com.axelor.apps.bpm.service.dashboard.WkfDashboardCommonService;
import com.axelor.apps.bpm.service.dashboard.WkfDashboardService;
import com.axelor.exception.service.TraceBackService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.db.MetaJsonModel;
import com.axelor.meta.db.MetaJsonRecord;
import com.axelor.meta.db.MetaModel;
import com.axelor.meta.db.repo.MetaJsonModelRepository;
import com.axelor.meta.db.repo.MetaModelRepository;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.meta.schema.actions.ActionView.ActionViewBuilder;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.persistence.Table;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

public class WkfDashboardController {

  private static final String CHART_MONTH_STATUS = "chart.wkf.model.status.per.month";
  private static final String CHART_DAY_STATUS = "chart.wkf.model.status.per.day";
  private static final String CHART_TIMESPENT_STATUS = "chart.wkf.model.time.spent.per.status";

  public void showDashboard(ActionRequest request, ActionResponse response) {
    try {
      WkfModel wkfModel = request.getContext().asType(WkfModel.class);
      wkfModel = Beans.get(WkfModelRepository.class).find(wkfModel.getId());
      if (CollectionUtils.isEmpty(wkfModel.getWkfProcessList())) {
        return;
      }

      if (wkfModel.getWkfProcessList().size() == 1) {
        response.setView(
            ActionView.define(I18n.get("Workflow dashboard"))
                .add("dashboard", "dasbhoard-wkf-model")
                .context("_wkfId", wkfModel.getId())
                .context("_process", wkfModel.getWkfProcessList().get(0).getName())
                .map());
      } else {
        response.setView(
            ActionView.define(I18n.get("Select process"))
                .model(Wizard.class.getName())
                .add("form", "wfk-model-select-process-wizard-form")
                .param("popup", "true")
                .param("popup-save", "false")
                .param("show-confirm", "false")
                .param("show-toolbar", "false")
                .context("_wkf", wkfModel)
                .map());
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void setModelsDomain(ActionRequest request, ActionResponse response) {
    try {
      WkfModel wkfModel = null;
      List<Long> jsonModelIds = new ArrayList<>();
      List<Long> metaModelIds = new ArrayList<>();

      if (request.getContext().get("wkfId") != null) {
        wkfModel =
            Beans.get(WkfModelRepository.class)
                .find(Long.parseLong(request.getContext().get("wkfId").toString()));
      }

      String process = (String) request.getContext().get("_process");

      if (wkfModel != null) {
        for (WkfProcess wkfProcess : wkfModel.getWkfProcessList()) {
          if (CollectionUtils.isEmpty(wkfProcess.getWkfProcessConfigList())
              || !wkfProcess.getName().equals(process)) {
            continue;
          }
          for (WkfProcessConfig processConfig : wkfProcess.getWkfProcessConfigList()) {
            if (processConfig.getMetaModel() != null) {
              metaModelIds.add(processConfig.getMetaModel().getId());
            }

            if (processConfig.getMetaJsonModel() != null) {
              jsonModelIds.add(processConfig.getMetaJsonModel().getId());
            }
          }
        }
      }

      response.setAttr(
          "metaJsonModel",
          "domain",
          !jsonModelIds.isEmpty()
              ? "self.id IN (" + StringUtils.join(jsonModelIds, ',') + ")"
              : "self.id IN (0)");

      response.setAttr(
          "metaModel",
          "domain",
          !metaModelIds.isEmpty()
              ? "self.id IN (" + StringUtils.join(metaModelIds, ',') + ")"
              : "self.id IN (0)");

    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  @SuppressWarnings("unchecked")
  public void showRecord(ActionRequest request, ActionResponse response) {
    try {
      Context context = request.getContext();
      String status = context.get("status").toString();
      String tableName = null;
      String jsonModel = null;

      ActionViewBuilder actionViewBuilder = null;
      if (context.get("metaModel") != null) {
        Long id =
            Long.parseLong(((Map<String, Object>) context.get("metaModel")).get("id").toString());
        MetaModel metaModel = Beans.get(MetaModelRepository.class).find(id);
        tableName = metaModel.getTableName();
        actionViewBuilder =
            Beans.get(WkfDashboardCommonService.class).createActionBuilder(status, metaModel);

      } else if (context.get("metaJsonModel") != null) {
        Long id =
            Long.parseLong(
                ((Map<String, Object>) context.get("metaJsonModel")).get("id").toString());
        MetaJsonModel metaJsonModel = Beans.get(MetaJsonModelRepository.class).find(id);
        jsonModel = metaJsonModel.getName();
        tableName = MetaJsonRecord.class.getAnnotation(Table.class).name();
        actionViewBuilder =
            Beans.get(WkfDashboardCommonService.class).createActionBuilder(status, metaJsonModel);
      }

      List<Long> idList = getRecordIds(context, tableName, jsonModel);

      response.setView(actionViewBuilder.context("ids", !idList.isEmpty() ? idList : 0).map());

      response.setCanClose(true);

    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  private List<Long> getRecordIds(Context context, String tableName, String jsonModel) {
    String month = (String) context.get("month");
    String day = (String) context.get("day");
    LocalDate fromDate = LocalDate.parse(context.get("fromDate").toString());
    LocalDate toDate = LocalDate.parse(context.get("toDate").toString());
    String _chart = context.get("_chart").toString();
    String status = context.get("status").toString();

    List<Long> idList = new ArrayList<Long>();
    if (tableName != null) {
      switch (_chart) {
        case CHART_MONTH_STATUS:
          idList =
              Beans.get(WkfDashboardService.class)
                  .getStatusPerMonthRecord(tableName, status, month, jsonModel);
          break;
        case CHART_DAY_STATUS:
          idList =
              Beans.get(WkfDashboardService.class)
                  .getStatusPerDayRecord(tableName, status, day, jsonModel);
          break;
        case CHART_TIMESPENT_STATUS:
          idList =
              Beans.get(WkfDashboardService.class)
                  .getTimespentPerStatusRecord(tableName, status, fromDate, toDate, jsonModel);
          break;
        default:
          break;
      }
    }
    return idList;
  }
}
