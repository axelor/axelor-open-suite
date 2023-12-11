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
package com.axelor.apps.account.web;

import com.axelor.apps.account.db.FiscalPosition;
import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.PaymentCondition;
import com.axelor.apps.account.db.PaymentMode;
import com.axelor.apps.account.service.invoice.InvoiceMergingService;
import com.axelor.apps.account.service.invoice.InvoiceMergingService.InvoiceMergingResult;
import com.axelor.apps.account.service.invoice.InvoiceMergingViewService;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.PriceList;
import com.axelor.apps.base.db.TradingName;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.meta.schema.actions.ActionView.ActionViewBuilder;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;
import com.axelor.utils.helpers.MapHelper;
import java.time.LocalDate;
import java.util.List;
import org.apache.commons.collections.CollectionUtils;

public class InvoiceMergingController {

  /**
   * A controller to generate a single invoice from several.
   *
   * @param request
   * @param response
   */
  public void mergeInvoice(ActionRequest request, ActionResponse response) {
    try {
      InvoiceMergingService invoiceMergingService = Beans.get(InvoiceMergingService.class);
      List<Invoice> invoicesToMerge =
          MapHelper.getCollection(request.getContext(), Invoice.class, "invoiceToMerge");
      if (CollectionUtils.isNotEmpty(invoicesToMerge)) {
        InvoiceMergingResult result = invoiceMergingService.mergeInvoices(invoicesToMerge);
        if (result.isConfirmationNeeded()) {
          // Need to display intermediate screen to select some values
          ActionViewBuilder confirmView =
              Beans.get(InvoiceMergingViewService.class).buildConfirmView(result, invoicesToMerge);

          response.setView(confirmView.map());
          return;
        }
        setResponseView(response, result);
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  /**
   * A controller to generate a single invoice from several from confirmation popup.
   *
   * @param request
   * @param response
   */
  public void mergeInvoiceFromPopup(ActionRequest request, ActionResponse response) {
    try {
      Context context = request.getContext();
      InvoiceMergingService invoiceMergingService = Beans.get(InvoiceMergingService.class);
      List<Invoice> invoicesToMerge =
          MapHelper.getCollection(context, Invoice.class, "invoiceToMerge");
      Partner contactPartner = MapHelper.get(context, Partner.class, "contactPartner");
      PriceList priceList = MapHelper.get(context, PriceList.class, "priceList");
      PaymentMode paymentMode = MapHelper.get(context, PaymentMode.class, "paymentMode");
      PaymentCondition paymentCondition =
          MapHelper.get(context, PaymentCondition.class, "paymentCondition");
      TradingName tradingName = MapHelper.get(context, TradingName.class, "tradingName");
      FiscalPosition fiscalPosition =
          MapHelper.get(context, FiscalPosition.class, "fiscalPosition");
      if (CollectionUtils.isNotEmpty(invoicesToMerge)) {
        InvoiceMergingResult result =
            invoiceMergingService.mergeInvoices(
                invoicesToMerge,
                contactPartner,
                priceList,
                paymentMode,
                paymentCondition,
                tradingName,
                fiscalPosition);
        setResponseView(response, result);
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  /**
   * A controller to generate a single invoice from several from confirmation popup.
   *
   * @param request
   * @param response
   */
  public void mergeSupplInvoiceFromPopup(ActionRequest request, ActionResponse response) {
    try {
      Context context = request.getContext();
      InvoiceMergingService invoiceMergingService = Beans.get(InvoiceMergingService.class);
      List<Invoice> invoicesToMerge =
          MapHelper.getCollection(context, Invoice.class, "invoiceToMerge");
      Partner contactPartner = MapHelper.get(context, Partner.class, "contactPartner");
      PriceList priceList = MapHelper.get(context, PriceList.class, "priceList");
      PaymentMode paymentMode = MapHelper.get(context, PaymentMode.class, "paymentMode");
      PaymentCondition paymentCondition =
          MapHelper.get(context, PaymentCondition.class, "paymentCondition");
      TradingName tradingName = MapHelper.get(context, TradingName.class, "tradingName");
      FiscalPosition fiscalPosition =
          MapHelper.get(context, FiscalPosition.class, "fiscalPosition");
      String supplierInvoiceNb =
          request.getContext().get("supplierInvoiceNb") == null
              ? null
              : request.getContext().get("supplierInvoiceNb").toString();
      LocalDate originDate =
          request.getContext().get("originDate") == null
              ? null
              : LocalDate.parse(request.getContext().get("originDate").toString());
      if (CollectionUtils.isNotEmpty(invoicesToMerge)) {
        InvoiceMergingResult result =
            invoiceMergingService.mergeInvoices(
                invoicesToMerge,
                contactPartner,
                priceList,
                paymentMode,
                paymentCondition,
                tradingName,
                fiscalPosition,
                supplierInvoiceNb,
                originDate);
        setResponseView(response, result);
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void convertSelectedLinesToMergeLines(ActionRequest request, ActionResponse response) {
    try {
      @SuppressWarnings("unchecked")
      List<Integer> idList = (List<Integer>) request.getContext().get("_ids");
      List<Invoice> invoicesToMerge =
          Beans.get(InvoiceMergingService.class).convertSelectedLinesToMergeLines(idList);
      if (invoicesToMerge == null || invoicesToMerge.isEmpty()) {
        response.setError(I18n.get("You have to choose at least one invoice"));
        return;
      }
      InvoiceMergingService invoiceMergingService = Beans.get(InvoiceMergingService.class);
      if (CollectionUtils.isNotEmpty(invoicesToMerge)) {
        InvoiceMergingResult result = invoiceMergingService.mergeInvoices(invoicesToMerge);
        if (result.isConfirmationNeeded()) {
          // Need to display intermediate screen to select some values
          ActionViewBuilder confirmView =
              Beans.get(InvoiceMergingViewService.class).buildConfirmView(result, invoicesToMerge);

          response.setView(confirmView.map());
          return;
        }
        setResponseView(response, result);
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  protected void setResponseView(ActionResponse response, InvoiceMergingResult result) {
    if (result.getInvoice() != null) {
      // Open the generated invoice in a new tab
      response.setView(
          ActionView.define(I18n.get("Invoices"))
              .model(Invoice.class.getName())
              .add("grid", "invoice-grid")
              .add("form", "invoice-form")
              .param("search-filters", "customer-invoices-filters")
              .param("forceEdit", Boolean.TRUE.toString())
              .context("_showRecord", String.valueOf(result.getInvoice().getId()))
              .map());
      response.setCanClose(true);
    }
  }
}
