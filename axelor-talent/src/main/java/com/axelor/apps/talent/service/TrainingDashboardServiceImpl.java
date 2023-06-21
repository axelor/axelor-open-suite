package com.axelor.apps.talent.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Period;
import com.axelor.apps.hr.db.Employee;
import com.axelor.apps.hr.service.EmployeeDashboardService;
import com.axelor.apps.hr.service.employee.EmployeeService;
import com.axelor.apps.talent.db.TrainingRegister;
import com.axelor.apps.talent.db.repo.TrainingRegisterRepository;
import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TrainingDashboardServiceImpl implements TrainingDashboardService {

  protected TrainingRegisterRepository trainingRegisterRepo;
  protected EmployeeDashboardService employeeDashboardService;
  protected EmployeeService employeeService;

  @Inject
  public TrainingDashboardServiceImpl(
      TrainingRegisterRepository trainingRegisterRepo,
      EmployeeDashboardService employeeDashboardService,
      EmployeeService employeeService) {
    this.trainingRegisterRepo = trainingRegisterRepo;
    this.employeeDashboardService = employeeDashboardService;
    this.employeeService = employeeService;
  }

  @Override
  public List<Map<String, Object>> getTrainingData() throws AxelorException {
    Employee employee = employeeService.getConnectedEmployee();
    Period period = employeeDashboardService.getCurrentPeriod();
    List<TrainingRegister> trainingList =
        trainingRegisterRepo
            .all()
            .filter(
                "self.employee = :employee AND self.fromDate >= :fromDate AND self.fromDate <= :endDate")
            .bind("employee", employee)
            .bind("fromDate", period.getFromDate())
            .bind("endDate", period.getToDate())
            .fetch();

    List<Map<String, Object>> trainingData = new ArrayList<>();

    for (TrainingRegister trainingRegister : trainingList) {
      Map<String, Object> map = new HashMap<>();
      map.put("date", trainingRegister.getFromDate());
      map.put("training", trainingRegister.getTraining().getName());
      map.put("id", trainingRegister.getId());
      trainingData.add(map);
    }
    return trainingData;
  }
}
