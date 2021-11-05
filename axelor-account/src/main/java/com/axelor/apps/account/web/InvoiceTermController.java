/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2021 Axelor (<http://axelor.com>).
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
package com.axelor.apps.account.web;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceTerm;
import com.axelor.apps.account.service.invoice.InvoiceTermService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.exception.service.TraceBackService;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Singleton;
import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.math.RoundingMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class InvoiceTermController {

  @SuppressWarnings("unused")
  private final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  public void computeCustomizedAmount(ActionRequest request, ActionResponse response) {
    InvoiceTerm invoiceTerm = request.getContext().asType(InvoiceTerm.class);
    try {
      BigDecimal inTaxTotal = invoiceTerm.getInvoice().getInTaxTotal();
      if (inTaxTotal.compareTo(BigDecimal.ZERO) == 0) {
        return;
      }
      BigDecimal amount =
          invoiceTerm
              .getPercentage()
              .multiply(inTaxTotal)
              .divide(
                  new BigDecimal(100),
                  AppBaseService.DEFAULT_NB_DECIMAL_DIGITS,
                  RoundingMode.HALF_UP);
      response.setValue("amount", amount);
      response.setValue("amountRemaining", amount);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void computeCustomizedPercentage(ActionRequest request, ActionResponse response) {
    InvoiceTerm invoiceTerm = request.getContext().asType(InvoiceTerm.class);
    try {
      BigDecimal inTaxTotal = invoiceTerm.getInvoice().getInTaxTotal();
      if (inTaxTotal.compareTo(BigDecimal.ZERO) == 0) {
        return;
      }
      BigDecimal percentage =
          invoiceTerm
              .getAmount()
              .multiply(new BigDecimal(100))
              .divide(inTaxTotal, AppBaseService.DEFAULT_NB_DECIMAL_DIGITS, RoundingMode.HALF_UP);
      response.setValue("percentage", percentage);
      response.setValue("amountRemaining", invoiceTerm.getAmount());
      response.setValue(
          "isCustomized",
          invoiceTerm.getPaymentConditionLine() == null
              || percentage.compareTo(invoiceTerm.getPaymentConditionLine().getPaymentPercentage())
                  != 0);

    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void initInvoiceTermFromInvoice(ActionRequest request, ActionResponse response) {
    try {
      InvoiceTerm invoiceTerm = request.getContext().asType(InvoiceTerm.class);
      Invoice invoice = request.getContext().getParent().asType(Invoice.class);
      if (invoice != null) {
        InvoiceTermService invoiceTermService = Beans.get(InvoiceTermService.class);
        invoiceTermService.initCustomizedInvoiceTerm(invoice, invoiceTerm);
        response.setValues(invoiceTerm);
      }

    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
