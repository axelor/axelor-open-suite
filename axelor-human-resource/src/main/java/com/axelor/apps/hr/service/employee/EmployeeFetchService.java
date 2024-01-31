package com.axelor.apps.hr.service.employee;

import com.axelor.apps.hr.db.Employee;
import com.axelor.apps.hr.db.HrBatch;
import java.util.List;

public interface EmployeeFetchService {
  List<Employee> getEmployees(HrBatch hrBatch);
}
