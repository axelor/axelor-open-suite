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
