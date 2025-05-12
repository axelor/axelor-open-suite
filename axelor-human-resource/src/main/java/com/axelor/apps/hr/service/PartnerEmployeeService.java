package com.axelor.apps.hr.service;

import com.axelor.apps.base.db.Partner;
import com.axelor.apps.hr.db.Employee;

public interface PartnerEmployeeService {

  void editPartner(Employee employee);

  void convertToContactPartner(Partner partner);
}
