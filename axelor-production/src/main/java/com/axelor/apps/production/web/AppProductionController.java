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
package com.axelor.apps.production.web;

import com.axelor.apps.base.service.exception.HandleExceptionResponse;
import com.axelor.apps.production.service.app.AppProductionService;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.studio.db.AppProduction;
import com.axelor.studio.db.repo.AppProductionRepository;
import com.google.inject.Singleton;

@Singleton
public class AppProductionController {

  public void generateProductionConfigurations(ActionRequest request, ActionResponse response) {

    Beans.get(AppProductionService.class).generateProductionConfigurations();

    response.setReload(true);
  }

  @HandleExceptionResponse
  public void checkIfOutsourcingDisabled(ActionRequest request, ActionResponse response) {
    AppProduction app = request.getContext().asType(AppProduction.class);
    if (app.getId() == null) {
      return;
    }
    boolean outsourcing =
        Beans.get(AppProductionRepository.class).find(app.getId()).getManageOutsourcing();
    boolean outsourcingContxt = app.getManageOutsourcing();
    if (outsourcing && !outsourcingContxt) {
      Beans.get(AppProductionService.class).updatePartnerSubcontractTag();
    }
  }
}
