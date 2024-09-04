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
package com.axelor.apps.businessproject.web;

import com.axelor.apps.base.service.exception.ErrorException;
import com.axelor.apps.businessproject.service.app.AppBusinessProjectService;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.studio.db.AppBusinessProject;

public class AppBusinessProjectController {

  public void generateBusinessProjectConfigurations(
      ActionRequest request, ActionResponse response) {

    Beans.get(AppBusinessProjectService.class).generateBusinessProjectConfigurations();

    response.setReload(true);
  }

  @ErrorException
  public void initCoefficientsFields(ActionRequest request, ActionResponse response) {
    AppBusinessProject appBusinessProject =
        Beans.get(AppBusinessProjectService.class).getAppBusinessProject();

    if (appBusinessProject != null) {
      Integer saleCoefficient = appBusinessProject.getSaleCoefficient();
      Integer riskCoefficient = appBusinessProject.getRiskCoefficient();

      response.setValue("saleCoefficient", saleCoefficient);
      response.setValue("riskCoefficient", riskCoefficient);
    }
  }
}
