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
package com.axelor.apps.bpm.web;

import com.axelor.apps.bpm.db.WkfModel;
import com.axelor.apps.bpm.db.repo.WkfModelRepository;
import com.axelor.apps.bpm.service.dashboard.BpmManagerDashboardService;
import com.axelor.apps.bpm.service.dashboard.BpmManagerDashboardServiceImpl;
import com.axelor.apps.bpm.service.dashboard.BpmManagerDashboardTaskService;
import com.axelor.apps.bpm.service.dashboard.BpmManagerDashboardUserService;
import com.axelor.apps.bpm.service.dashboard.WkfDashboardCommonService;
import com.axelor.inject.Beans;
import com.axelor.meta.schema.actions.ActionView.ActionViewBuilder;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;

public class BpmManagerDashboardController {

  public void showBpmManagerProcess(ActionRequest request, ActionResponse response) {
    this.showProcess(0, response);
  }

  private void showProcess(int offset, ActionResponse response) {
    response.setValues(Beans.get(BpmManagerDashboardService.class).showProcess(offset));
  }

  public void showPreviousProcess(ActionRequest request, ActionResponse response) {
    this.showProcess(this.getOffset(request, false), response);
  }

  public void showNextProcess(ActionRequest request, ActionResponse response) {
    this.showProcess(this.getOffset(request, true), response);
  }

  private int getOffset(ActionRequest request, boolean isNext) {
    if (isNext) {
      return (int) request.getContext().get("offset") + BpmManagerDashboardServiceImpl.FETCH_LIMIT;
    } else {
      return (int) request.getContext().get("offset") - BpmManagerDashboardServiceImpl.FETCH_LIMIT;
    }
  }

  public void getAssignedToMeTask(ActionRequest request, ActionResponse response) {
    this.getChartData(request, response, WkfDashboardCommonService.ASSIGNED_ME);
  }

  public void getAssignedToOtherTask(ActionRequest request, ActionResponse response) {
    this.getChartData(request, response, WkfDashboardCommonService.ASSIGNED_OTHER);
  }

  public void getTaskByProcess(ActionRequest request, ActionResponse response) {
    this.getChartData(request, response, WkfDashboardCommonService.TASK_BY_PROCESS);
  }

  private void getChartData(ActionRequest request, ActionResponse response, String type) {
    Context context = request.getContext();
    WkfModel wkfModel = this.getWkfModel(context);

    String taskByProcessType = "";
    if (context.get("typeSelect") != null) {
      taskByProcessType = context.get("typeSelect").toString();
    }

    List<Map<String, Object>> dataMapList =
        Beans.get(BpmManagerDashboardService.class).getChartData(wkfModel, type, taskByProcessType);

    response.setData(dataMapList);
  }

  @SuppressWarnings("rawtypes")
  private WkfModel getWkfModel(Map<String, Object> context) {
    Long wkfModelId = Long.valueOf(((Map) context.get("wkfModel")).get("id").toString());
    return Beans.get(WkfModelRepository.class).find(wkfModelId);
  }

  public void getAvgTimePerUser(ActionRequest request, ActionResponse response) {
    Context context = request.getContext();
    WkfModel wkfModel = this.getWkfModel(context);

    String unit = (String) context.get("unit");

    List<Map<String, Object>> dataMapList =
        Beans.get(BpmManagerDashboardUserService.class).getAvgTimePerUserData(wkfModel, unit);

    response.setData(dataMapList);
  }

  public void getTaskDoneTodayPerUser(ActionRequest request, ActionResponse response) {
    Context context = request.getContext();
    WkfModel wkfModel = this.getWkfModel(context);

    List<Map<String, Object>> dataMapList =
        Beans.get(BpmManagerDashboardUserService.class).getTaskDoneTodayPerUser(wkfModel);

    response.setData(dataMapList);
  }

  public void getTaskToDoPerUser(ActionRequest request, ActionResponse response) {
    Context context = request.getContext();
    WkfModel wkfModel = this.getWkfModel(context);

    List<Map<String, Object>> dataMapList =
        Beans.get(BpmManagerDashboardUserService.class).getTaskToDoPerUser(wkfModel);

    response.setData(dataMapList);
  }

  public void getTaskCompletionByDays(ActionRequest request, ActionResponse response) {
    LocalDate fromDate = LocalDate.parse(request.getContext().get("fromDate").toString());
    LocalDate toDate = LocalDate.parse(request.getContext().get("toDate").toString());

    List<Map<String, Object>> dataMapList =
        Beans.get(BpmManagerDashboardTaskService.class).getTaskCompletionByDays(fromDate, toDate);

    response.setData(dataMapList);
  }

  public void showAssignedToMeTask(ActionRequest request, ActionResponse response) {
    this.showAssignedRecords(request, response, WkfDashboardCommonService.ASSIGNED_ME);
  }

  public void showAssignedToOtherTask(ActionRequest request, ActionResponse response) {
    this.showAssignedRecords(request, response, WkfDashboardCommonService.ASSIGNED_OTHER);
  }

  @SuppressWarnings({"unchecked"})
  private void showAssignedRecords(ActionRequest request, ActionResponse response, String type) {
    Map<String, Object> context = request.getRawContext();
    WkfModel wkfModel = this.getWkfModel(context);

    String status = "";
    if (context.get("status") != null) {
      status = context.get("status").toString();
    }
    status = StringUtils.substringBefore(status, "(").trim();

    Map<String, Object> dataMap =
        Beans.get(BpmManagerDashboardUserService.class).getStatusRecords(wkfModel, status, type);

    if (dataMap.isEmpty()) {
      return;
    }

    String modelName = dataMap.get("modelName").toString();
    boolean isMetaModel = (boolean) dataMap.get("isMetaModel");
    List<Long> recordIds = (List<Long>) dataMap.get("recordIds");

    ActionViewBuilder actionViewBuilder =
        Beans.get(WkfDashboardCommonService.class)
            .computeActionView(status, modelName, isMetaModel);

    response.setView(actionViewBuilder.context("ids", !recordIds.isEmpty() ? recordIds : 0).map());
  }

  @SuppressWarnings({"unchecked"})
  public void showTaskByProcess(ActionRequest request, ActionResponse response) {
    Map<String, Object> context = request.getRawContext();
    WkfModel wkfModel = this.getWkfModel(context);

    String process = "";
    if (context.get("process") != null) {
      process = context.get("process").toString();
    }
    String model = "";
    if (context.get("model") != null) {
      model = context.get("model").toString();
    }
    String typeSelect = context.get("typeSelect").toString();

    Map<String, Object> dataMap =
        Beans.get(BpmManagerDashboardTaskService.class)
            .getTaskByProcessRecords(wkfModel, process, model, typeSelect);

    if (dataMap.isEmpty()) {
      return;
    }

    String modelName = dataMap.get("modelName").toString();
    boolean isMetaModel = (boolean) dataMap.get("isMetaModel");
    List<Long> recordIds = (List<Long>) dataMap.get("recordIds");

    ActionViewBuilder actionViewBuilder =
        Beans.get(WkfDashboardCommonService.class).computeActionView(null, modelName, isMetaModel);

    response.setView(actionViewBuilder.context("ids", !recordIds.isEmpty() ? recordIds : 0).map());
  }
}
