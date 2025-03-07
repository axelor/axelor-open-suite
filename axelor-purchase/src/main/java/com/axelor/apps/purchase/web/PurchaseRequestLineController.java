/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2025 Axelor (<http://axelor.com>).
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
package com.axelor.apps.purchase.web;

import com.axelor.apps.purchase.db.PurchaseRequestLine;
import com.axelor.apps.purchase.service.PurchaseRequestLineService;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import java.util.Map;

public class PurchaseRequestLineController {

  public void setDefaultQuantity(ActionRequest request, ActionResponse response) {
    PurchaseRequestLine purchaseRequestLine =
        request.getContext().asType(PurchaseRequestLine.class);
    response.setValue(
        "quantity",
        Beans.get(PurchaseRequestLineService.class).getDefaultQuantity(purchaseRequestLine));
  }

  public void setProductInformation(ActionRequest request, ActionResponse response) {
    PurchaseRequestLine purchaseRequestLine =
        request.getContext().asType(PurchaseRequestLine.class);
    Map<String, Object> values =
        Beans.get(PurchaseRequestLineService.class).getProductInformation(purchaseRequestLine);
    response.setValues(values);
  }
}
