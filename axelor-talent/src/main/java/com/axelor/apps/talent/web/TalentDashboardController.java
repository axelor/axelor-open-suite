package com.axelor.apps.talent.web;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Period;
import com.axelor.apps.base.db.repo.PeriodRepository;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.hr.db.Employee;
import com.axelor.apps.hr.db.repo.EmployeeRepository;
import com.axelor.apps.hr.service.HRDashboardService;
import com.axelor.apps.hr.service.employee.EmployeeService;
import com.axelor.apps.talent.service.TalentDashboardService;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class TalentDashboardController {

  public void getConnectedEmployeeTrainingData(ActionRequest request, ActionResponse response) {
    try {
      Employee employee = Beans.get(EmployeeService.class).getConnectedEmployee();
      Period period = Beans.get(HRDashboardService.class).getCurrentPeriod();
      List<Map<String, Object>> trainingData =
          Beans.get(TalentDashboardService.class).getTrainingData(employee, period);
      response.setData(trainingData);
    } catch (AxelorException e) {
      TraceBackService.trace(response, e);
    }
  }

  @SuppressWarnings("unchecked")
  public void getEmployeeTrainingData(ActionRequest request, ActionResponse response) {
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
      List<Map<String, Object>> managerTrainingData =
          Beans.get(TalentDashboardService.class).getTrainingData(employee, period);
      response.setData(managerTrainingData);
    } catch (AxelorException e) {
      TraceBackService.trace(response, e);
    }
  }

  @SuppressWarnings("unchecked")
  public void getRecruitmentData(ActionRequest request, ActionResponse response) {
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
      List<Map<String, Object>> recruitmentData =
          Beans.get(TalentDashboardService.class).getRecruitmentData(employee, period);
      response.setData(recruitmentData);
    } catch (AxelorException e) {
      TraceBackService.trace(response, e);
    }
  }
}
