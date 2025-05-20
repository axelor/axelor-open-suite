package com.axelor.apps.hr.service;

import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.repo.PartnerRepository;
import com.axelor.apps.hr.db.Employee;
import com.axelor.apps.hr.service.app.AppHumanResourceService;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class PartnerEmployeeServiceImpl implements PartnerEmployeeService {

  protected final PartnerRepository partnerRepository;
  protected final AppHumanResourceService appHumanResourceService;

  @Inject
  public PartnerEmployeeServiceImpl(
      PartnerRepository partnerRepository, AppHumanResourceService appHumanResourceService) {
    this.partnerRepository = partnerRepository;
    this.appHumanResourceService = appHumanResourceService;
  }

  @Transactional
  @Override
  public void editPartner(Employee employee) {
    Partner partner = employee.getContactPartner();
    if (!partner.getIsEmployee()) {
      partner.setIsEmployee(true);

      if (employee.getExternal() || !appHumanResourceService.isApp("bank-payment")) {
        partner.setIsContact(true);
      } else {
        partner.setPartnerTypeSelect(PartnerRepository.PARTNER_TYPE_INDIVIDUAL);
      }

      partnerRepository.save(partner);
    }
  }

  @Transactional
  @Override
  public void convertToContactPartner(Partner partner) {
    Employee employee = partner.getEmployee();
    employee.setExternal(true);
    partner.setIsContact(true);
    partner.setPartnerTypeSelect(0);

    partnerRepository.save(partner);
  }
}
