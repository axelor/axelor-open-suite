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

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.businessproject.service.InvoiceServiceProject;
import com.axelor.apps.businessproject.service.InvoiceServiceProjectImpl;
import com.axelor.inject.Beans;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Singleton;
import java.util.List;

@Singleton
public class InvoiceController {

  public void exportAnnex(ActionRequest request, ActionResponse response) throws AxelorException {

    Invoice invoice =
        Beans.get(InvoiceRepository.class).find(request.getContext().asType(Invoice.class).getId());

    try {
      List<String> reportInfo =
          Beans.get(InvoiceServiceProjectImpl.class)
              .editInvoiceAnnex(invoice, invoice.getId().toString(), false);

      if (reportInfo == null || reportInfo.isEmpty()) {
        return;
      }

      response.setView(ActionView.define(reportInfo.get(0)).add("html", reportInfo.get(1)).map());
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void updateLines(ActionRequest request, ActionResponse response) throws AxelorException {
    try {
      Invoice invoice = request.getContext().asType(Invoice.class);
      invoice = Beans.get(InvoiceRepository.class).find(invoice.getId());
      invoice = Beans.get(InvoiceServiceProject.class).updateLines(invoice);
      response.setValue("invoiceLineList", invoice.getInvoiceLineList());
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
