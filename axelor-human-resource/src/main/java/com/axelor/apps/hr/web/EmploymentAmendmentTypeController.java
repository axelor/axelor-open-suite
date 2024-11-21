/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2024 Axelor (<http://axelor.com>).
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
package com.axelor.apps.hr.web;

import com.axelor.apps.hr.db.EmploymentAmendmentType;
import com.axelor.apps.hr.service.employee.EmploymentAmendmentTypeService;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;

public class EmploymentAmendmentTypeController {

  public void setEmploymentContractSubTypeSetDomain(
      ActionRequest request, ActionResponse response) {
    EmploymentAmendmentType employmentAmendmentType =
        request.getContext().asType(EmploymentAmendmentType.class);

    String employmentContractSubTypeIds =
        Beans.get(EmploymentAmendmentTypeService.class)
            .getEmploymentContractSubTypeSetDomain(employmentAmendmentType);

    response.setAttr(
        "employmentContractSubTypeSet",
        "domain",
        "self.id IN (" + employmentContractSubTypeIds + ")");
  }

  public void setEmploymentContractSubTypeSet(ActionRequest request, ActionResponse response) {
    EmploymentAmendmentType employmentAmendmentType =
        request.getContext().asType(EmploymentAmendmentType.class);

    employmentAmendmentType =
        Beans.get(EmploymentAmendmentTypeService.class)
            .setEmploymentContractSubTypeSet(employmentAmendmentType);

    response.setValue(
        "employmentContractSubTypeSet", employmentAmendmentType.getEmploymentContractSubTypeSet());
  }
}
