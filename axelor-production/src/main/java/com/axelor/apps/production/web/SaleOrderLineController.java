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
import com.axelor.apps.production.exceptions.ProductionExceptionMessage;
import com.axelor.apps.production.service.BillOfMaterialService;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Singleton;

@Singleton
public class SaleOrderLineController {

  public void customizeBillOfMaterial(ActionRequest request, ActionResponse response) {
    try {
      SaleOrderLine saleOrderLine = request.getContext().asType(SaleOrderLine.class);

      BillOfMaterial copyBillOfMaterial =
          Beans.get(BillOfMaterialService.class).customizeBillOfMaterial(saleOrderLine);

      if (copyBillOfMaterial != null) {

        response.setValue("billOfMaterial", copyBillOfMaterial);
        response.setInfo(I18n.get(ProductionExceptionMessage.SALE_ORDER_LINE_1));
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
