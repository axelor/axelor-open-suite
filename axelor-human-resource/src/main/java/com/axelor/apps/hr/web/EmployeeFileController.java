/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2023 Axelor (<http://axelor.com>).
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

import com.axelor.apps.hr.db.EmployeeFile;
import com.axelor.apps.hr.db.repo.EmployeeFileRepository;
import com.axelor.apps.hr.service.EmployeeFileDMSService;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;

public class EmployeeFileController {
  public void setDMSFile(ActionRequest request, ActionResponse response) {
    EmployeeFile employeeFile = request.getContext().asType(EmployeeFile.class);
    employeeFile = Beans.get(EmployeeFileRepository.class).find(employeeFile.getId());
    Beans.get(EmployeeFileDMSService.class).setDMSFile(employeeFile);
    response.setReload(true);
  }

  public void setInlineUrl(ActionRequest request, ActionResponse response) {
    EmployeeFile employeeFile = request.getContext().asType(EmployeeFile.class);
    response.setValue(
        "$inlineUrl", Beans.get(EmployeeFileDMSService.class).getInlineUrl(employeeFile));
  }
}
