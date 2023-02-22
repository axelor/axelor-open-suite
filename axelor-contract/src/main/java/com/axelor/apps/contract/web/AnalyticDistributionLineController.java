/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2023 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or  modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.axelor.apps.contract.web;

import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.exception.service.TraceBackService;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;

public class AnalyticDistributionLineController {

  public void linkWithContract(ActionRequest request, ActionResponse response) {
    try {
      Class<?> parentClass = request.getContext().getParent().getContextClass();
      if ((InvoiceLine.class).equals(parentClass)) {
        InvoiceLine invoiceLine = request.getContext().getParent().asType(InvoiceLine.class);
        response.setValue("contractLine", invoiceLine.getContractLine());
      }

    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
