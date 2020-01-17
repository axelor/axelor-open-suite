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

import com.axelor.apps.account.db.repo.PartnerAccountRepository;
import com.axelor.apps.account.service.AccountingSituationService;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.service.app.AppService;
import com.axelor.apps.hr.db.Employee;
import com.axelor.inject.Beans;
import com.google.inject.Inject;

public class PartnerHRRepository extends PartnerAccountRepository {

  @Inject
  public PartnerHRRepository(
      AppService appService, AccountingSituationService accountingSituationService) {
    super(appService, accountingSituationService);
  }

  @Override
  public void remove(Partner partner) {
    if (partner.getEmployee() != null) {
      EmployeeHRRepository employeeRepo = Beans.get(EmployeeHRRepository.class);
      Employee employee = employeeRepo.find(partner.getEmployee().getId());
      if (employee != null) {
        employeeRepo.remove(employee);
      }
    }
    super.remove(partner);
  }
}
