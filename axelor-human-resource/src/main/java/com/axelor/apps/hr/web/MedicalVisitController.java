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

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.hr.db.MedicalVisit;
import com.axelor.apps.hr.db.repo.MedicalVisitRepository;
import com.axelor.apps.hr.service.MedicalVisitWorkflowService;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;

public class MedicalVisitController {

  public void plan(ActionRequest request, ActionResponse response) throws AxelorException {
    MedicalVisit medicalVisit = request.getContext().asType(MedicalVisit.class);
    medicalVisit = Beans.get(MedicalVisitRepository.class).find(medicalVisit.getId());
    Beans.get(MedicalVisitWorkflowService.class).plan(medicalVisit);
    response.setReload(true);
  }

  public void realize(ActionRequest request, ActionResponse response) throws AxelorException {
    MedicalVisit medicalVisit = request.getContext().asType(MedicalVisit.class);
    medicalVisit = Beans.get(MedicalVisitRepository.class).find(medicalVisit.getId());
    Beans.get(MedicalVisitWorkflowService.class).realize(medicalVisit);
    response.setReload(true);
  }

  public void cancel(ActionRequest request, ActionResponse response) throws AxelorException {
    MedicalVisit medicalVisit = request.getContext().asType(MedicalVisit.class);
    medicalVisit = Beans.get(MedicalVisitRepository.class).find(medicalVisit.getId());
    Beans.get(MedicalVisitWorkflowService.class).cancel(medicalVisit);
    response.setReload(true);
  }
}
