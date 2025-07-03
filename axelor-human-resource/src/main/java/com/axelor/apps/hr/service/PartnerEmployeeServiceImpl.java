/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2025 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
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
