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
package com.axelor.apps.hr.web;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Period;
import com.axelor.apps.base.db.repo.PeriodRepository;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.hr.db.Employee;
import com.axelor.apps.hr.db.repo.EmployeeRepository;
import com.axelor.apps.hr.service.HRDashboardService;
import com.axelor.apps.hr.service.employee.EmployeeService;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class HRDashboardController {

  public void getConnectedEmployeeLeaveData(ActionRequest request, ActionResponse response) {
    try {
      Employee employee = Beans.get(EmployeeService.class).getConnectedEmployee();
      Period period = Beans.get(HRDashboardService.class).getCurrentPeriod();
      List<Map<String, Object>> leaveData =
          Beans.get(HRDashboardService.class).getConnectedEmployeeLeaveData(employee, period);
      response.setData(leaveData);
    } catch (AxelorException e) {
      TraceBackService.trace(response, e);
    }
  }

  public void getConnectedEmployeeExpenseData(ActionRequest request, ActionResponse response) {
    try {
      Employee employee = Beans.get(EmployeeService.class).getConnectedEmployee();
      Period period = Beans.get(HRDashboardService.class).getCurrentPeriod();
      List<Map<String, Object>> expenseData =
          Beans.get(HRDashboardService.class).getExpenseData(employee, period);
      response.setData(expenseData);
    } catch (AxelorException e) {
      TraceBackService.trace(response, e);
    }
  }

  public void getConnectedEmployeeTimesheetData(ActionRequest request, ActionResponse response) {
    try {
      Employee employee = Beans.get(EmployeeService.class).getConnectedEmployee();
      Period period = Beans.get(HRDashboardService.class).getCurrentPeriod();
      List<Map<String, Object>> timesheetData =
          Beans.get(HRDashboardService.class).getTimesheetData(employee, period);
      response.setData(timesheetData);
    } catch (AxelorException e) {
      TraceBackService.trace(response, e);
    }
  }

  public void getConnectedEmployeeExtraHrsData(ActionRequest request, ActionResponse response) {
    try {
      Employee employee = Beans.get(EmployeeService.class).getConnectedEmployee();
      Period period = Beans.get(HRDashboardService.class).getCurrentPeriod();
      List<Map<String, Object>> extraHrsData =
          Beans.get(HRDashboardService.class).getExtraHrsData(employee, period);
      response.setData(extraHrsData);
    } catch (AxelorException e) {
      TraceBackService.trace(response, e);
    }
  }

  @SuppressWarnings("unchecked")
  public void getEmployeeLeaveData(ActionRequest request, ActionResponse response) {
    try {
      Employee employee = null;
      Period period = null;

      LinkedHashMap<String, Object> employeeMap =
          ((LinkedHashMap<String, Object>) request.getData().get("employee"));
      LinkedHashMap<String, Object> periodMap =
          (LinkedHashMap<String, Object>) request.getData().get("period");

      if (employeeMap != null) {
        employee =
            Beans.get(EmployeeRepository.class)
                .find(Long.parseLong(employeeMap.get("id").toString()));
      }
      if (periodMap != null) {
        period =
            Beans.get(PeriodRepository.class).find(Long.parseLong(periodMap.get("id").toString()));
      }
      List<Map<String, Object>> leaveData =
          Beans.get(HRDashboardService.class).getEmployeeLeaveData(employee, period);
      response.setData(leaveData);
    } catch (AxelorException e) {
      TraceBackService.trace(response, e);
    }
  }

  @SuppressWarnings("unchecked")
  public void getEmployeeExpenseData(ActionRequest request, ActionResponse response) {
    try {
      Employee employee = null;
      Period period = null;

      LinkedHashMap<String, Object> employeeMap =
          ((LinkedHashMap<String, Object>) request.getData().get("employee"));
      LinkedHashMap<String, Object> periodMap =
          (LinkedHashMap<String, Object>) request.getData().get("period");

      if (employeeMap != null) {
        employee =
            Beans.get(EmployeeRepository.class)
                .find(Long.parseLong(employeeMap.get("id").toString()));
      }
      if (periodMap != null) {
        period =
            Beans.get(PeriodRepository.class).find(Long.parseLong(periodMap.get("id").toString()));
      }
      List<Map<String, Object>> expenseData =
          Beans.get(HRDashboardService.class).getExpenseData(employee, period);
      response.setData(expenseData);
    } catch (AxelorException e) {
      TraceBackService.trace(response, e);
    }
  }

  @SuppressWarnings("unchecked")
  public void getEmployeeTimesheetData(ActionRequest request, ActionResponse response) {
    try {
      Employee employee = null;
      Period period = null;

      LinkedHashMap<String, Object> employeeMap =
          ((LinkedHashMap<String, Object>) request.getData().get("employee"));
      LinkedHashMap<String, Object> periodMap =
          (LinkedHashMap<String, Object>) request.getData().get("period");

      if (employeeMap != null) {
        employee =
            Beans.get(EmployeeRepository.class)
                .find(Long.parseLong(employeeMap.get("id").toString()));
      }
      if (periodMap != null) {
        period =
            Beans.get(PeriodRepository.class).find(Long.parseLong(periodMap.get("id").toString()));
      }
      if (employee == null || period == null) {
        return;
      }
      List<Map<String, Object>> timesheetData =
          Beans.get(HRDashboardService.class).getTimesheetData(employee, period);
      response.setData(timesheetData);
    } catch (AxelorException e) {
      TraceBackService.trace(response, e);
    }
  }

  @SuppressWarnings("unchecked")
  public void getEmployeeExtraHrsData(ActionRequest request, ActionResponse response) {
    try {
      Employee employee = null;
      Period period = null;

      LinkedHashMap<String, Object> employeeMap =
          ((LinkedHashMap<String, Object>) request.getData().get("employee"));
      LinkedHashMap<String, Object> periodMap =
          (LinkedHashMap<String, Object>) request.getData().get("period");

      if (employeeMap != null) {
        employee =
            Beans.get(EmployeeRepository.class)
                .find(Long.parseLong(employeeMap.get("id").toString()));
      }
      if (periodMap != null) {
        period =
            Beans.get(PeriodRepository.class).find(Long.parseLong(periodMap.get("id").toString()));
      }
      List<Map<String, Object>> extraHrsData =
          Beans.get(HRDashboardService.class).getExtraHrsData(employee, period);
      response.setData(extraHrsData);
    } catch (AxelorException e) {
      TraceBackService.trace(response, e);
    }
  }
}
