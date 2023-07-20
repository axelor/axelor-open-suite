package com.axelor.apps.hr.service;

import com.axelor.apps.hr.db.EmployeeFile;

public interface EmployeeFileDMSService {

  void setDMSFile(EmployeeFile employeeFile);

  String getInlineUrl(EmployeeFile employeeFile);
}
