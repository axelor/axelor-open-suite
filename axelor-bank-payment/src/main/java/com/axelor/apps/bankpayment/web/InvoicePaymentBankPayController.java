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
package com.axelor.apps.bankpayment.web;

import com.axelor.apps.account.db.InvoicePayment;
import com.axelor.apps.account.service.payment.invoice.payment.InvoicePaymentCreateService;
import com.axelor.apps.account.web.InvoicePaymentController;
import com.axelor.apps.bankpayment.db.BankOrder;
import com.axelor.common.ObjectUtils;
import com.axelor.exception.service.TraceBackService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.inject.Singleton;
import java.util.List;
import javax.annotation.Nullable;

@Singleton
public class InvoicePaymentBankPayController extends InvoicePaymentController {

  @Override
  public void validateMassPayment(ActionRequest request, ActionResponse response) {
    try {

      InvoicePayment invoicePayment = request.getContext().asType(InvoicePayment.class);

      if (!ObjectUtils.isEmpty(request.getContext().get("_selectedInvoices"))) {
        List<Long> invoiceIdList =
            Lists.transform(
                (List) request.getContext().get("_selectedInvoices"),
                new Function<Object, Long>() {
                  @Nullable
                  @Override
                  public Long apply(@Nullable Object input) {
                    return Long.parseLong(input.toString());
                  }
                });

        List<InvoicePayment> invoicePaymentList =
            Beans.get(InvoicePaymentCreateService.class)
                .createMassInvoicePayment(
                    invoiceIdList,
                    invoicePayment.getPaymentMode(),
                    invoicePayment.getCompanyBankDetails(),
                    invoicePayment.getPaymentDate(),
                    invoicePayment.getBankDepositDate(),
                    invoicePayment.getChequeNumber());

        if (!invoicePaymentList.isEmpty() && invoicePaymentList.get(0).getBankOrder() != null) {
          response.setView(
              ActionView.define(I18n.get("Bank order"))
                  .model(BankOrder.class.getName())
                  .add("form", "bank-order-form")
                  .add("grid", "bank-order-grid")
                  .param("search-filters", "bank-order-filters")
                  .param("forceEdit", "true")
                  .context(
                      "_showRecord",
                      String.valueOf(invoicePaymentList.get(0).getBankOrder().getId()))
                  .map());
        }
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
