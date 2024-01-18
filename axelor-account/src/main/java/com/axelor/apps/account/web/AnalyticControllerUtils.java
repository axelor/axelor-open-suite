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
package com.axelor.apps.account.web;

import com.axelor.apps.account.db.AnalyticMoveLine;
import com.axelor.apps.account.db.repo.AnalyticLine;
import com.axelor.apps.account.db.repo.InvoiceLineRepository;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;

public class AnalyticControllerUtils {

  public static AnalyticLine getParentWithContext(
      ActionRequest request, ActionResponse response, AnalyticMoveLine analyticMoveLine) {
    Context parentContext = request.getContext().getParent();
    AnalyticLine parent = null;
    if (parentContext != null) {
      Class<?> parentClass = request.getContext().getParent().getContextClass();
      if (AnalyticLine.class.isAssignableFrom(parentClass)) {
        parent = request.getContext().getParent().asType(AnalyticLine.class);
      }
    } else {
      if (analyticMoveLine.getMoveLine() != null) {
        parent = analyticMoveLine.getMoveLine();
      } else if (analyticMoveLine.getInvoiceLine() != null) {
        parent = analyticMoveLine.getInvoiceLine();
      } else if (request.getContext().get("invoiceId") != null) {
        Long invoiceId = Long.valueOf((Integer) request.getContext().get("invoiceId"));
        parent = Beans.get(InvoiceLineRepository.class).find(invoiceId);
      }
    }
    return parent;
  }
}
