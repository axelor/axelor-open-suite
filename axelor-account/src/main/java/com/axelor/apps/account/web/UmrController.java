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
package com.axelor.apps.account.web;

import com.axelor.apps.account.db.InvoicingPaymentSituation;
import com.axelor.apps.account.service.umr.UmrService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.service.exception.ErrorException;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;

public class UmrController {

  @ErrorException
  public void onNew(ActionRequest request, ActionResponse response) throws AxelorException {
    if (request.getContext().getParent() != null
        && InvoicingPaymentSituation.class.equals(
            request.getContext().getParent().getContextClass())) {
      InvoicingPaymentSituation invoicingPaymentSituation =
          request.getContext().getParent().asType(InvoicingPaymentSituation.class);
      if (invoicingPaymentSituation == null) {
        return;
      }

      response.setValues(Beans.get(UmrService.class).getOnNewValuesMap(invoicingPaymentSituation));
    }
  }
}
