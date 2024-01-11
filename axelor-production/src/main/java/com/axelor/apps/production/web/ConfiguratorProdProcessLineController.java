/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2024 Axelor (<http://axelor.com>).
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
package com.axelor.apps.production.web;

import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.production.db.ConfiguratorProdProcessLine;
import com.axelor.apps.production.db.WorkCenter;
import com.axelor.apps.production.db.WorkCenterGroup;
import com.axelor.apps.production.db.repo.ConfiguratorProdProcessLineRepository;
import com.axelor.apps.production.db.repo.WorkCenterGroupRepository;
import com.axelor.apps.production.service.WorkCenterService;
import com.axelor.apps.production.service.configurator.ConfiguratorProdProcessLineService;
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
