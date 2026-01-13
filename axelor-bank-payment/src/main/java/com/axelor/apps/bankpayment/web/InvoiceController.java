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
package com.axelor.apps.bankpayment.web;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.bankpayment.service.InvoiceCancelBillOfExchangeBankPaymentService;
import com.axelor.apps.bankpayment.service.bankdetails.BankDetailsServiceBankPaymentImpl;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.BankDetails;
import com.axelor.apps.base.service.exception.ErrorException;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;

public class InvoiceController {

  @ErrorException
  public void cancelBillOfExchange(ActionRequest request, ActionResponse response)
      throws AxelorException {
    Invoice invoice = request.getContext().asType(Invoice.class);

    Beans.get(InvoiceCancelBillOfExchangeBankPaymentService.class).cancelBillOfExchange(invoice);

    response.setReload(true);
  }

  @ErrorException
  public void getDefaultBankDetails(ActionRequest request, ActionResponse response) {
    Invoice invoice = request.getContext().asType(Invoice.class);
    BankDetails bankDetails =
        Beans.get(BankDetailsServiceBankPaymentImpl.class)
            .getDefaultBankDetails(
                invoice.getPartner(), invoice.getCompany(), invoice.getPaymentMode());
    response.setValue("bankDetails", bankDetails);
  }
}
