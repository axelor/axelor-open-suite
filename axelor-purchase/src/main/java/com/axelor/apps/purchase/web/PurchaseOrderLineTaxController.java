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
package com.axelor.apps.purchase.web;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.service.exception.ErrorException;
import com.axelor.apps.base.service.tax.OrderLineTaxService;
import com.axelor.apps.purchase.db.PurchaseOrder;
import com.axelor.apps.purchase.db.PurchaseOrderLineTax;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;

public class PurchaseOrderLineTaxController {

  @ErrorException
  public void recomputeAmounts(ActionRequest request, ActionResponse response)
      throws AxelorException {
    PurchaseOrderLineTax purchaseOrderLineTax =
        request.getContext().asType(PurchaseOrderLineTax.class);
    if (!Beans.get(OrderLineTaxService.class).isManageByAmount(purchaseOrderLineTax)) {
      return;
    }

    PurchaseOrder purchaseOrder = purchaseOrderLineTax.getPurchaseOrder();
    if (purchaseOrder == null) {
      purchaseOrder = request.getContext().getParent().asType(PurchaseOrder.class);
    }

    response.setValue(
        "inTaxTotal",
        Beans.get(OrderLineTaxService.class)
            .computeInTaxTotal(purchaseOrderLineTax, purchaseOrder.getCurrency()));
  }
}
