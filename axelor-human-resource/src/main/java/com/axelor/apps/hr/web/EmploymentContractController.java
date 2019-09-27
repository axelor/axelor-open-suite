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
package com.axelor.apps.hr.web;

import com.axelor.apps.hr.db.EmploymentContract;
import com.axelor.apps.hr.db.repo.EmploymentContractRepository;
import com.axelor.apps.hr.service.EmploymentContractService;
import com.axelor.exception.service.TraceBackService;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class EmploymentContractController {

  @Inject private EmploymentContractService employmentContractService;

  @Inject private EmploymentContractRepository employmentContractRepo;

  public void addAmendment(ActionRequest request, ActionResponse response) {

    EmploymentContract employmentContract = request.getContext().asType(EmploymentContract.class);

    try {

      employmentContractService.addAmendment(
          employmentContractRepo.find(employmentContract.getId()));
      response.setFlash(
          String.format(
              "Contrat %s - avenant %s",
              employmentContract.getFullName(), employmentContract.getEmploymentContractVersion()));
      response.setReload(true);

    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
