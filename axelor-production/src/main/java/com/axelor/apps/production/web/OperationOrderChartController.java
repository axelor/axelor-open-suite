/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2023 Axelor (<http://axelor.com>).
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

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.production.db.Machine;
import com.axelor.apps.production.db.WorkCenter;
import com.axelor.apps.production.db.WorkCenterGroup;
import com.axelor.apps.production.db.repo.MachineRepository;
import com.axelor.apps.production.db.repo.WorkCenterGroupRepository;
import com.axelor.apps.production.service.operationorder.OperationOrderChartService;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Singleton;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Singleton
public class OperationOrderChartController {

  public void chargeByMachineHours(ActionRequest request, ActionResponse response) {
    try {
      LocalDateTime fromDateTime =
          LocalDateTime.parse(
              request.getContext().get("fromDateTime").toString(), DateTimeFormatter.ISO_DATE_TIME);
      LocalDateTime toDateTime =
          LocalDateTime.parse(
              request.getContext().get("toDateTime").toString(), DateTimeFormatter.ISO_DATE_TIME);

      List<Map<String, Object>> dataList =
          Beans.get(OperationOrderChartService.class)
              .chargeByMachineHours(fromDateTime, toDateTime);

      response.setData(dataList);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void chargeByMachineDays(ActionRequest request, ActionResponse response) {

    try {
      LocalDateTime fromDateTime =
          LocalDateTime.parse(
              request.getContext().get("fromDateTime").toString(), DateTimeFormatter.ISO_DATE_TIME);
      LocalDateTime toDateTime =
          LocalDateTime.parse(
              request.getContext().get("toDateTime").toString(), DateTimeFormatter.ISO_DATE_TIME);

      List<Map<String, Object>> dataList =
          Beans.get(OperationOrderChartService.class).chargeByMachineDays(fromDateTime, toDateTime);
      response.setData(dataList);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void chargePerMachineDays(ActionRequest request, ActionResponse response)
      throws AxelorException {

    LocalDateTime fromDateTime =
        LocalDateTime.parse(
            request.getContext().get("fromDateTime").toString(), DateTimeFormatter.ISO_DATE_TIME);
    LocalDateTime toDateTime =
        LocalDateTime.parse(
            request.getContext().get("toDateTime").toString(), DateTimeFormatter.ISO_DATE_TIME);
    Object object = request.getContext().get("_id");
    if (object != null) {
      Set<Machine> machineSet = new HashSet<>();
      Long id = Long.valueOf(object.toString());
      machineSet.add(Beans.get(MachineRepository.class).find((id)));
      List<Map<String, Object>> dataList =
          Beans.get(OperationOrderChartService.class)
              .chargePerMachineDays(fromDateTime, toDateTime, machineSet);
      response.setData(dataList);
    }
  }

  public void calculateHourlyMachineCharge(ActionRequest request, ActionResponse response)
      throws AxelorException {
    LocalDateTime fromDateTime =
        LocalDateTime.parse(
            request.getContext().get("fromDateTime").toString(), DateTimeFormatter.ISO_DATE_TIME);
    LocalDateTime toDateTime =
        LocalDateTime.parse(
            request.getContext().get("toDateTime").toString(), DateTimeFormatter.ISO_DATE_TIME);
    Object object = request.getContext().get("_id");
    if (object != null) {
      Long id = Long.valueOf(object.toString());
      Machine machine = Beans.get(MachineRepository.class).find((id));
      List<Map<String, Object>> dataList =
          Beans.get(OperationOrderChartService.class)
              .calculateHourlyMachineCharge(fromDateTime, toDateTime, machine);
      response.setData(dataList);
    }
  }

  public void chargePerDayForWorkCenterGroupMachines(ActionRequest request, ActionResponse response)
      throws AxelorException {

    LocalDateTime fromDateTime =
        LocalDateTime.parse(
            request.getContext().get("fromDateTime").toString(), DateTimeFormatter.ISO_DATE_TIME);
    LocalDateTime toDateTime =
        LocalDateTime.parse(
            request.getContext().get("toDateTime").toString(), DateTimeFormatter.ISO_DATE_TIME);
    Object object = request.getContext().get("_id");
    if (object != null) {
      Long id = Long.valueOf(object.toString());
      WorkCenterGroup workCenterGroup = Beans.get(WorkCenterGroupRepository.class).find((id));
      if (workCenterGroup != null) {
        Set<Machine> machineSet =
            workCenterGroup.getWorkCenterSet().stream()
                .map(WorkCenter::getMachine)
                .collect(Collectors.toSet());
        List<Map<String, Object>> dataList =
            Beans.get(OperationOrderChartService.class)
                .chargePerMachineDays(fromDateTime, toDateTime, machineSet);
        response.setData(dataList);
      }
    }
  }
}
