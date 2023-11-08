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
package com.axelor.apps.maintenance.web;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.maintenance.service.BillOfMaterialMaintenanceService;
import com.axelor.apps.production.db.BillOfMaterial;
import com.axelor.inject.Beans;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class BillOfMaterialController {

  private static final Logger LOG = LoggerFactory.getLogger(BillOfMaterialController.class);

  public void print(ActionRequest request, ActionResponse response) throws AxelorException {

    BillOfMaterial billOfMaterial = request.getContext().asType(BillOfMaterial.class);
    BillOfMaterialMaintenanceService billOfMaterialService =
        Beans.get(BillOfMaterialMaintenanceService.class);

    String name = billOfMaterialService.getFileName(billOfMaterial);

    String fileLink = billOfMaterialService.getReportLink(billOfMaterial, name);

    LOG.debug("Printing " + name);

    response.setView(ActionView.define(name).add("html", fileLink).map());
  }
}
