package com.axelor.apps.production.web;

import com.axelor.apps.production.db.ConfiguratorProdProcessLine;
import com.axelor.apps.production.db.WorkCenter;
import com.axelor.apps.production.db.WorkCenterGroup;
import com.axelor.apps.production.db.repo.ConfiguratorProdProcessLineRepository;
import com.axelor.apps.production.db.repo.WorkCenterGroupRepository;
import com.axelor.apps.production.service.WorkCenterService;
import com.axelor.apps.production.service.configurator.ConfiguratorProdProcessLineService;
import com.axelor.exception.service.TraceBackService;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import java.util.LinkedHashMap;
import java.util.Map;

public class ConfiguratorProdProcessLineController {

  public void updateDuration(ActionRequest request, ActionResponse response) {
    try {
      ConfiguratorProdProcessLine confProdProcessLine =
          request.getContext().asType(ConfiguratorProdProcessLine.class);
      WorkCenter workCenter = confProdProcessLine.getWorkCenter();
      if (workCenter != null) {
        response.setValue(
            "durationPerCycle",
            Beans.get(WorkCenterService.class).getDurationFromWorkCenter(workCenter));
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void updateCapacitySettings(ActionRequest request, ActionResponse response) {
    try {
      ConfiguratorProdProcessLine confProdProcessLine =
          request.getContext().asType(ConfiguratorProdProcessLine.class);
      WorkCenter workCenter = confProdProcessLine.getWorkCenter();
      if (workCenter != null) {
        response.setValue(
            "minCapacityPerCycle",
            Beans.get(WorkCenterService.class).getMinCapacityPerCycleFromWorkCenter(workCenter));
        response.setValue(
            "maxCapacityPerCycle",
            Beans.get(WorkCenterService.class).getMaxCapacityPerCycleFromWorkCenter(workCenter));
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void fillWorkCenter(ActionRequest request, ActionResponse response) {
    try {
      ConfiguratorProdProcessLine confProdProcessLine =
          request.getContext().asType(ConfiguratorProdProcessLine.class);
      response.setValue(
          "workCenter",
          Beans.get(WorkCenterService.class)
              .getMainWorkCenterFromGroup(confProdProcessLine.getWorkCenterGroup()));
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  @SuppressWarnings("unchecked")
  public void setWorkCenterGroup(ActionRequest request, ActionResponse response) {
    try {
      Long configuratorProdProcessId =
          request.getContext().asType(ConfiguratorProdProcessLine.class).getId();
      ConfiguratorProdProcessLine confProdProcessLine =
          Beans.get(ConfiguratorProdProcessLineRepository.class).find(configuratorProdProcessId);
      Map<String, Object> workCenterGroupMap =
          ((LinkedHashMap<String, Object>) request.getContext().get("workCenterGroupWizard"));
      if (workCenterGroupMap != null && workCenterGroupMap.containsKey("id")) {
        WorkCenterGroup workCenterGroup =
            Beans.get(WorkCenterGroupRepository.class)
                .find(Long.valueOf(workCenterGroupMap.get("id").toString()));
        Beans.get(ConfiguratorProdProcessLineService.class)
            .setWorkCenterGroup(confProdProcessLine, workCenterGroup);
      }
      response.setCanClose(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
