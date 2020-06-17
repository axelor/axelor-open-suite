/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2020 Axelor (<http://axelor.com>).
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
import com.axelor.apps.base.db.Product;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.repo.SaleOrderRepository;
import com.axelor.apps.supplychain.db.Timetable;
import com.axelor.apps.supplychain.db.repo.TimetableRepository;
import com.axelor.apps.supplychain.exception.IExceptionMessage;
import com.axelor.apps.supplychain.service.TimetableService;
import com.axelor.exception.AxelorException;
import com.axelor.i18n.I18n;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class TimetableController {

  @Inject protected TimetableService timetableService;

  @Inject protected TimetableRepository timeTableRepo;

  public void generateInvoice(ActionRequest request, ActionResponse response)
      throws AxelorException {
    Timetable timetable = request.getContext().asType(Timetable.class);
    timetable = timeTableRepo.find(timetable.getId());

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

  /**
   * Called by the timetable grid and form. Update all fields when the product is changed.
   *
   * @param request
   * @param response
   */
  public void getProductInformation(ActionRequest request, ActionResponse response)
      throws AxelorException {

    Context context = request.getContext();

    Timetable timetable = context.asType(Timetable.class);

    Product product = timetable.getProduct();

    if (product != null) {
      try {
        timetableService.computeProductInformation(timetable);

        response.setValue("productName", timetable.getProductName());
        response.setValue("unit", timetable.getUnit());
      } catch (Exception e) {
        response.setFlash(e.getMessage());
      }
    }
  }
}
