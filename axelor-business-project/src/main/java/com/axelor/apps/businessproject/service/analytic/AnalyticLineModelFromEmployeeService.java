package com.axelor.apps.businessproject.service.analytic;

import com.axelor.apps.hr.db.Employee;
import com.axelor.apps.supplychain.model.AnalyticLineModel;

public interface AnalyticLineModelFromEmployeeService {
  void copyAnalyticsDataFromEmployee(Employee employee, AnalyticLineModel analyticLineModel);
}
