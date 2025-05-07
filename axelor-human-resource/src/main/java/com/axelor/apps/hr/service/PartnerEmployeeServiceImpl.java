package com.axelor.apps.hr.service;

import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.repo.PartnerRepository;
import com.axelor.apps.hr.db.Employee;
import com.axelor.apps.hr.db.repo.EmployeeRepository;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class PartnerEmployeeServiceImpl implements PartnerEmployeeService {

  protected final PartnerRepository partnerRepository;
  protected final EmployeeRepository employeeRepository;

  @Inject
  public PartnerEmployeeServiceImpl(
      PartnerRepository partnerRepository, EmployeeRepository employeeRepository) {
    this.partnerRepository = partnerRepository;
    this.employeeRepository = employeeRepository;
  }

  @Transactional
  @Override
  public void convertToContactPartner(Partner partner) {
    Employee employee = partner.getEmployee();
    employee.setExternal(true);
    partner.setIsContact(true);
    partner.setPartnerTypeSelect(0);

    partnerRepository.save(partner);
    employeeRepository.save(employee);
  }
}
