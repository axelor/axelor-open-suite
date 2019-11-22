/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2019 Axelor (<http://axelor.com>).
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
package com.axelor.apps.supplychain.web;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.repo.SaleOrderRepository;
import com.axelor.apps.supplychain.db.Timetable;
import com.axelor.apps.supplychain.db.TimetableTemplate;
import com.axelor.apps.supplychain.db.repo.TimetableRepository;
import com.axelor.apps.supplychain.exception.IExceptionMessage;
import com.axelor.apps.supplychain.service.TimetableService;
import com.axelor.exception.AxelorException;
import com.axelor.exception.service.TraceBackService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Singleton
public class TimetableController {

  @Inject TimetableService timetableService;

  public void generateInvoice(ActionRequest request, ActionResponse response)
      throws AxelorException {
    Timetable timetable = request.getContext().asType(Timetable.class);
    timetable = Beans.get(TimetableRepository.class).find(timetable.getId());

    Context parentContext = request.getContext().getParent();
    if (parentContext != null && parentContext.getContextClass().equals(SaleOrder.class)) {
      SaleOrder saleOrder = parentContext.asType(SaleOrder.class);
      if (saleOrder.getStatusSelect() < SaleOrderRepository.STATUS_ORDER_CONFIRMED) {
        response.setAlert(I18n.get(IExceptionMessage.TIMETABLE_SALE_ORDER_NOT_CONFIRMED));
        return;
      }
    }

    if (timetable.getInvoice() != null) {
      response.setAlert(I18n.get(IExceptionMessage.TIMETABLE_INVOICE_ALREADY_GENERATED));
      return;
    }

    Invoice invoice = timetableService.generateInvoice(timetable);
    response.setReload(true);
    response.setView(
        ActionView.define(I18n.get("Invoice generated"))
            .model("com.axelor.apps.account.db.Invoice")
            .add("form", "invoice-form")
            .add("grid", "invoice-grid")
            .param("forceEdit", "true")
            .context("_showRecord", invoice.getId().toString())
            .map());
  }

  public void applyTemplate(ActionRequest request, ActionResponse response) {
    Context context = request.getContext();

    try {
      if (context.get("timetableTemplate") == null
          || context.get("exTaxTotal") == null
          || context.get("computationDate") == null) {
        return;
      }

      TimetableTemplate template = (TimetableTemplate) context.get("timetableTemplate");

      List<Timetable> timetableList =
          timetableService.applyTemplate(
              template,
              (BigDecimal) context.get("exTaxTotal"),
              (LocalDate) context.get("computationDate"));

      response.setValue("timetableList", timetableList);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
