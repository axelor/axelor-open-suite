/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2020 Axelor (<http://axelor.com>).
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
import com.axelor.apps.base.service.PartnerService;
import com.axelor.apps.hr.db.Employee;
import com.axelor.apps.hr.db.EmploymentContract;
import com.axelor.auth.db.User;
import com.axelor.inject.Beans;
import java.time.LocalDate;
import java.util.Objects;

public class EmployeeHRRepository extends EmployeeRepository {

  @Override
  public Employee save(Employee entity) {
    Partner partner = entity.getContactPartner();
    if (!partner.getIsContact() && partner.getPartnerTypeSelect() == 0) {
      partner.setIsContact(true);
      partner.setIsEmployee(true);
      Beans.get(PartnerHRRepository.class).save(partner);
    } else {
      Beans.get(PartnerService.class).setPartnerFullName(partner);
    }

    EmploymentContract employmentContract = entity.getMainEmploymentContract();
    if (employmentContract != null && employmentContract.getEmployee() == null) {
      employmentContract.setEmployee(entity);
    }

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
    entity.setHireDate(null);
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
  public void remove(Employee employee) {

    if (employee.getUser() != null) {
      UserHRRepository userRepo = Beans.get(UserHRRepository.class);
      User user = userRepo.find(employee.getUser().getId());
      if (user != null) {
        user.setEmployee(null);
        userRepo.save(user);
      }
    }

    super.remove(employee);

    if (employee.getContactPartner() != null) {
      PartnerBaseRepository partnerRepo = Beans.get(PartnerBaseRepository.class);
      Partner partner = partnerRepo.find(employee.getContactPartner().getId());
      if (partner != null) {
        partner.setEmployee(null);
        partnerRepo.save(partner);
      }
    }
  }

  /**
   * Return true if given employee is a New employee or a Former employee according to hire date and
   * leaving date.
   *
   * @param employee
   * @return
   */
  public static boolean isEmployeeFormerOrNew(Employee employee) {
    Objects.requireNonNull(employee);
    return (employee.getLeavingDate() != null
            && employee.getLeavingDate().compareTo(LocalDate.now()) < 0)
        || (employee.getHireDate() != null
            && employee.getHireDate().compareTo(LocalDate.now()) > 0);
  }
}
