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
package com.axelor.apps.production.web;

import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.production.db.BillOfMaterial;
import com.axelor.apps.production.db.BillOfMaterialLine;
import com.axelor.apps.production.service.BillOfMaterialLineService;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;
import com.google.inject.Singleton;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class BillOfMaterialLineController {

  private static final Logger LOG = LoggerFactory.getLogger(BillOfMaterialLineController.class);

  public void productOnChange(ActionRequest request, ActionResponse response) {

    try {
      BillOfMaterialLine billOfMaterialLine = request.getContext().asType(BillOfMaterialLine.class);
      Context parent = request.getContext().getParent();
      BillOfMaterial billOfMaterial = null;
      if (parent != null) {
        billOfMaterial = parent.asType(BillOfMaterial.class);
      }

      BillOfMaterialLineService billOfMaterialLineService =
          Beans.get(BillOfMaterialLineService.class);
      billOfMaterialLineService.fillBom(
          billOfMaterialLine,
          Optional.ofNullable(billOfMaterial).map(BillOfMaterial::getCompany).orElse(null));
      billOfMaterialLineService.fillHasNoManageStock(billOfMaterialLine);
      billOfMaterialLineService.fillUnit(billOfMaterialLine);

      response.setValue("billOfMaterial", billOfMaterialLine.getBillOfMaterial());
      response.setValue("hasNoManageStock", billOfMaterialLine.getHasNoManageStock());
      response.setValue("unit", billOfMaterialLine.getUnit());

    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
