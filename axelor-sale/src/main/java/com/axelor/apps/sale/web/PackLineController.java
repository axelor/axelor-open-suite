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
package com.axelor.apps.sale.web;

import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.sale.db.Pack;
import com.axelor.apps.sale.db.PackLine;
import com.axelor.apps.sale.service.PackLineService;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;

public class PackLineController {

  public void getProductInformation(ActionRequest request, ActionResponse response) {
    Context context = request.getContext();
    PackLine packLine = context.asType(PackLine.class);
    Pack pack = this.getPack(context);
    Product product = packLine.getProduct();
    PackLineService packLineService = Beans.get(PackLineService.class);
    if (product == null) {
      packLine = packLineService.resetProductInformation(packLine);
      response.setValues(packLine);
      return;
    }

    try {
      packLine = packLineService.computeProductInformation(pack, packLine);
      response.setValues(packLine);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public Pack getPack(Context context) {
    Context parentContext = context.getParent();
    PackLine packLine = context.asType(PackLine.class);
    Pack pack = packLine.getPack();
    if (parentContext != null && parentContext.getContextClass().equals(Pack.class)) {
      pack = parentContext.asType(Pack.class);
    }
    return pack;
  }
}
