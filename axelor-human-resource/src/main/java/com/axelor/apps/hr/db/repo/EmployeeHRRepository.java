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
import com.axelor.apps.base.db.repo.PartnerBaseRepository;
import com.axelor.apps.hr.db.Employee;
import com.axelor.auth.db.User;
import com.axelor.inject.Beans;

public class EmployeeHRRepository extends EmployeeRepository {

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

  @Override
  public void remove(Employee entity) {

    if (entity.getUser() != null) {
      UserHRRepository userRepo = Beans.get(UserHRRepository.class);
      User user = userRepo.find(entity.getUser().getId());
      if (user != null) {
        user.setEmployee(null);
        userRepo.save(user);
      }
    }

    if (entity.getContactPartner() != null) {
      PartnerBaseRepository partnerRepo = Beans.get(PartnerBaseRepository.class);
      Partner partner = partnerRepo.find(entity.getContactPartner().getId());
      if (partner != null) {
        partner.setEmployee(null);
        partnerRepo.save(partner);
      }
    }
    super.remove(entity);
  }
}
