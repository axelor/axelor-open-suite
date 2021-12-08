/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2021 Axelor (<http://axelor.com>).
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

import com.axelor.apps.bpm.db.WkfInstance;
import com.axelor.apps.bpm.db.WkfModel;
import com.axelor.apps.bpm.db.repo.WkfModelRepository;
import com.axelor.apps.bpm.service.BpmDashboardService;
import com.axelor.apps.bpm.service.BpmDashboardServiceImpl;
import com.axelor.apps.bpm.service.WkfModelServiceImpl;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.meta.schema.actions.ActionView.ActionViewBuilder;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;

public class BpmDashboardController {

  public void getBpmManagerData(ActionRequest request, ActionResponse response) {
    this.getData(0, response);
  }

  public void getPreManagerData(ActionRequest request, ActionResponse response) {
    this.getData(this.getOffset(request, false), response);
  }

  public void getNxtManagerData(ActionRequest request, ActionResponse response) {
    this.getData(this.getOffset(request, true), response);
  }

  private int getOffset(ActionRequest request, boolean isNext) {
    if (isNext) {
      return (int) request.getContext().get("offset") + BpmDashboardServiceImpl.FETCH_LIMIT;
    } else {
      return (int) request.getContext().get("offset") - BpmDashboardServiceImpl.FETCH_LIMIT;
    }
  }

  private void getData(int offset, ActionResponse response) {
    response.setValues(Beans.get(BpmDashboardService.class).getData(offset));
  }

  @SuppressWarnings("rawtypes")
  private void getChartData(ActionRequest request, ActionResponse response, String type) {
    Long wkfModelId =
        Long.valueOf(((Map) request.getContext().get("wkfModel")).get("id").toString());
    WkfModel wkfModel = Beans.get(WkfModelRepository.class).find(wkfModelId);

    List<Map<String, Object>> dataMapList =
        Beans.get(BpmDashboardService.class).getChartData(wkfModel, type);

    response.setData(dataMapList);
  }

  public void getAssignedToMeTask(ActionRequest request, ActionResponse response) {
    this.getChartData(request, response, BpmDashboardServiceImpl.ASSIGNED_ME);
  }

  public void getAssignedToOtherTask(ActionRequest request, ActionResponse response) {
    this.getChartData(request, response, BpmDashboardServiceImpl.ASSIGNED_OTHER);
  }

  public void getLateTask(ActionRequest request, ActionResponse response) {
    this.getChartData(request, response, WkfModelServiceImpl.LATE_TASK);
  }

  public void getActiveStatus(ActionRequest request, ActionResponse response) {
    this.getChartData(request, response, BpmDashboardServiceImpl.ACTIVE_STATUS);
  }

  public void getActiveInstances(ActionRequest request, ActionResponse response) {
    List<Map<String, Object>> dataMapList = Beans.get(BpmDashboardService.class).getInstances();
    response.setData(dataMapList);
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  private void ShowRecords(ActionRequest request, ActionResponse response, String type) {
    Long wkfModelId =
        Long.valueOf(((Map) request.getContext().get("wkfModel")).get("id").toString());
    WkfModel wkfModel = Beans.get(WkfModelRepository.class).find(wkfModelId);

    String status = "";
    if (request.getContext().get("status") != null) {
      status = request.getContext().get("status").toString();
    }
    status = StringUtils.substringBefore(status, "(").trim();

    Map<String, Object> dataMap =
        Beans.get(BpmDashboardService.class).getStatusRecords(wkfModel, status, type);

    if (dataMap.isEmpty()) {
      return;
    }

    String modelName = dataMap.get("modelName").toString();
    boolean isMetaModel = (boolean) dataMap.get("isMetaModel");
    List<Long> recordIds = (List<Long>) dataMap.get("recordIds");

    ActionViewBuilder actionViewBuilder =
        Beans.get(WkfModelController.class).computeActionView(status, modelName, isMetaModel);

    response.setView(actionViewBuilder.context("ids", !recordIds.isEmpty() ? recordIds : 0).map());
  }

  public void showAssignedToMeTask(ActionRequest request, ActionResponse response) {
    this.ShowRecords(request, response, BpmDashboardServiceImpl.ASSIGNED_ME);
  }

  public void showAssignedToOtherTask(ActionRequest request, ActionResponse response) {
    this.ShowRecords(request, response, BpmDashboardServiceImpl.ASSIGNED_OTHER);
  }

  public void showLateTask(ActionRequest request, ActionResponse response) {
    this.ShowRecords(request, response, WkfModelServiceImpl.LATE_TASK);
  }

  public void showActiveStatus(ActionRequest request, ActionResponse response) {
    this.ShowRecords(request, response, BpmDashboardServiceImpl.ACTIVE_STATUS);
  }

  public void showActiveInstance(ActionRequest request, ActionResponse response) {
    String status = request.getContext().get("status").toString();

    List<Long> recordIds = Beans.get(BpmDashboardService.class).getInstanceRecords(status);

    response.setView(
        ActionView.define(I18n.get(status + " instances"))
            .model(WkfInstance.class.getName())
            .add("grid", "wkf-instance-grid")
            .add("form", "wkf-instance-form")
            .domain("self.id IN (:ids)")
            .context("ids", !recordIds.isEmpty() ? recordIds : 0)
            .map());
  }
}
