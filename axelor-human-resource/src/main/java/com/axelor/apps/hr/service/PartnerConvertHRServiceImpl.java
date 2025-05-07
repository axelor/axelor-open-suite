package com.axelor.apps.hr.service;

import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.service.PartnerConvertServiceImpl;
import com.axelor.apps.base.service.PartnerService;
import com.axelor.apps.hr.db.Employee;
import com.axelor.apps.hr.db.repo.EmployeeRepository;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class PartnerConvertHRServiceImpl extends PartnerConvertServiceImpl {

  protected final EmployeeRepository employeeRepository;

  @Inject
  public PartnerConvertHRServiceImpl(
      PartnerService partnerService, EmployeeRepository employeeRepository) {
    super(partnerService);
    this.employeeRepository = employeeRepository;
  }

  @Transactional
  @Override
  public void convertToIndividualPartner(Partner partner) {
    super.convertToIndividualPartner(partner);
    Employee employee = partner.getEmployee();
    if (employee == null) {
      return;
    }

    employee.setExternal(false);
  }
}
