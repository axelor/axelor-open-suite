/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2019 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or  modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.axelor.apps.hr.db.repo;

import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.service.PartnerService;
import com.axelor.apps.hr.db.Employee;
import com.google.inject.Inject;

public class EmployeeHRRepository extends EmployeeRepository {

  @Inject PartnerService partnerService;

  @Override
  public Employee save(Employee entity) {
    Partner partner = entity.getContactPartner();
    partner.setSimpleFullName(partnerService.computeSimpleFullName(partner));
    partner.setFullName(partnerService.computeFullName(partner));
    return super.save(entity);
  }

  @Override
  public Employee copy(Employee entity, boolean deep) {

    entity.setContactPartner(null);
    entity.setFixedProPhone(null);
    entity.setMobileProPhone(null);
    entity.setPhoneAtCustomer(null);
    entity.setEmergencyContact(null);
    entity.setEmergencyNumber(null);
    entity.setDateOfHire(null);
    entity.setSeniorityDate(null);
    entity.setProfitSharingBeneficiary(null);
    entity.setMainEmploymentContract(null);
    entity.setExportCode(null);
    entity.setEmploymentContractList(null);
    entity.setLunchVoucherAdvanceList(null);
    entity.setEmployeeAdvanceList(null);
    entity.setKilometricLogList(null);
    entity.setLeaveLineList(null);

    Employee copy = super.copy(entity, deep);

    return copy;
  }
}
