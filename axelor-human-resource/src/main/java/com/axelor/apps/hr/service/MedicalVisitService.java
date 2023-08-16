package com.axelor.apps.hr.service;

import com.axelor.apps.hr.db.Employee;
import com.axelor.apps.hr.db.EmployeeFile;
import java.util.List;

public interface MedicalVisitService {

  List<EmployeeFile> addToEmployeeFiles(Employee employee);
}
