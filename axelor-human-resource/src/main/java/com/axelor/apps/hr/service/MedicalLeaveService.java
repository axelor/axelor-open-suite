package com.axelor.apps.hr.service;

import com.axelor.apps.hr.db.Employee;
import com.axelor.apps.hr.db.EmployeeFile;
import java.time.LocalDate;
import java.util.List;

public interface MedicalLeaveService {

  LocalDate getLastMedicalLeaveDate(Employee employee);

  LocalDate getNextMedicalLeaveDate(Employee employee);

  List<EmployeeFile> addToEmployeeFiles(Employee employee);
}
