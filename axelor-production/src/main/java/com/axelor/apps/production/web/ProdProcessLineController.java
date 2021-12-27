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
package com.axelor.apps.production.web;

import com.axelor.apps.production.db.ProdProcessLine;
import com.axelor.apps.production.db.WorkCenter;
import com.axelor.apps.production.db.WorkCenterGroup;
import com.axelor.apps.production.db.repo.ProdProcessLineRepository;
import com.axelor.apps.production.db.repo.WorkCenterGroupRepository;
import com.axelor.apps.production.service.ProdProcessLineService;
import com.axelor.apps.production.service.WorkCenterService;
import com.axelor.exception.AxelorException;
import com.axelor.exception.service.HandleExceptionResponse;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Singleton;
import java.util.LinkedHashMap;
import java.util.Map;

@Singleton
public class ProdProcessLineController {

  public void updateDuration(ActionRequest request, ActionResponse response) {
    ProdProcessLine prodProcess = request.getContext().asType(ProdProcessLine.class);
    WorkCenter workCenter = prodProcess.getWorkCenter();
    if (workCenter != null) {
      response.setValue(
          "durationPerCycle",
          Beans.get(WorkCenterService.class).getDurationFromWorkCenter(workCenter));
    }
  }

  public void updateCapacitySettings(ActionRequest request, ActionResponse response) {
    ProdProcessLine prodProcess = request.getContext().asType(ProdProcessLine.class);
    WorkCenter workCenter = prodProcess.getWorkCenter();
    if (workCenter != null) {
      response.setValue(
          "minCapacityPerCycle",
          Beans.get(WorkCenterService.class).getMinCapacityPerCycleFromWorkCenter(workCenter));
      response.setValue(
          "maxCapacityPerCycle",
          Beans.get(WorkCenterService.class).getMaxCapacityPerCycleFromWorkCenter(workCenter));
    }
  }

  @HandleExceptionResponse
  public void fillWorkCenter(ActionRequest request, ActionResponse response)
      throws AxelorException {
    ProdProcessLine prodProcessLine = request.getContext().asType(ProdProcessLine.class);
    response.setValue(
        "workCenter",
        Beans.get(WorkCenterService.class)
            .getMainWorkCenterFromGroup(prodProcessLine.getWorkCenterGroup()));
  }

  @HandleExceptionResponse
  public void setWorkCenterGroup(ActionRequest request, ActionResponse response)
      throws AxelorException {
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
  }
}
