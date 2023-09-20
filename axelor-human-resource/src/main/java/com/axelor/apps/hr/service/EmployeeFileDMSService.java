package com.axelor.apps.hr.service;

import com.axelor.apps.hr.db.Employee;
import com.axelor.apps.hr.db.EmployeeFile;
import com.axelor.dms.db.DMSFile;

public interface EmployeeFileDMSService {

  void setDMSFile(EmployeeFile employeeFile);

  String getInlineUrl(EmployeeFile employeeFile);

  EmployeeFile createEmployeeFile(DMSFile dmsFile, Employee employee);
}
