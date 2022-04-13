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
package com.axelor.apps.production.web;

import com.axelor.apps.production.db.ProdProcessLine;
import com.axelor.apps.production.db.WorkCenter;
import com.axelor.apps.production.db.WorkCenterGroup;
import com.axelor.apps.production.db.repo.ProdProcessLineRepository;
import com.axelor.apps.production.db.repo.WorkCenterGroupRepository;
import com.axelor.apps.production.service.ProdProcessLineService;
import com.axelor.exception.service.TraceBackService;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Singleton;
import java.util.LinkedHashMap;
import java.util.Map;

@Singleton
public class ProdProcessLineController {

  public void updateDuration(ActionRequest request, ActionResponse response) {
    try {
      ProdProcessLine prodProcess = request.getContext().asType(ProdProcessLine.class);
      WorkCenter workCenter = prodProcess.getWorkCenter();
      if (workCenter != null) {
        response.setValue(
            "durationPerCycle",
            Beans.get(ProdProcessLineService.class)
                .getProdProcessLineDurationFromWorkCenter(workCenter));
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void updateCapacitySettings(ActionRequest request, ActionResponse response) {
    try {
      ProdProcessLine prodProcess = request.getContext().asType(ProdProcessLine.class);
      WorkCenter workCenter = prodProcess.getWorkCenter();
      if (workCenter != null) {
        response.setValue(
            "minCapacityPerCycle",
            Beans.get(ProdProcessLineService.class)
                .getProdProcessLineMinCapacityPerCycleFromWorkCenter(workCenter));
        response.setValue(
            "maxCapacityPerCycle",
            Beans.get(ProdProcessLineService.class)
                .getProdProcessLineMaxCapacityPerCycleFromWorkCenter(workCenter));
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void fillWorkCenter(ActionRequest request, ActionResponse response) {
    try {
      ProdProcessLine prodProcessLine = request.getContext().asType(ProdProcessLine.class);
      response.setValue(
          "workCenter",
          Beans.get(ProdProcessLineService.class).getMainWorkCenterFromGroup(prodProcessLine));
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void setWorkCenterGroup(ActionRequest request, ActionResponse response) {
    try {
      Long prodProcessId = request.getContext().asType(ProdProcessLine.class).getId();
      ProdProcessLine prodProcess = Beans.get(ProdProcessLineRepository.class).find(prodProcessId);
      Map<String, Object> workCenterGroupMap =
          ((LinkedHashMap<String, Object>) request.getContext().get("workCenterGroupWizard"));
      if (workCenterGroupMap != null && workCenterGroupMap.containsKey("id")) {
        WorkCenterGroup workCenterGroup =
            Beans.get(WorkCenterGroupRepository.class)
                .find(Long.valueOf(workCenterGroupMap.get("id").toString()));
        Beans.get(ProdProcessLineService.class).setWorkCenterGroup(prodProcess, workCenterGroup);
      }
      response.setCanClose(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
