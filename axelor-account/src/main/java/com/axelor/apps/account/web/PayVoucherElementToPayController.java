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

import com.axelor.apps.account.db.PayVoucherElementToPay;
import com.axelor.apps.account.service.payment.paymentvoucher.PayVoucherElementToPayService;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Singleton;

@Singleton
public class PayVoucherElementToPayController {

  public void updateAmountToPayCurrency(ActionRequest request, ActionResponse response) {
    try {
      PayVoucherElementToPay elementToPayContext =
          request.getContext().asType(PayVoucherElementToPay.class);

      Beans.get(PayVoucherElementToPayService.class).updateAmountToPayCurrency(elementToPayContext);

      response.setValue("amountToPayCurrency", elementToPayContext.getAmountToPayCurrency());
      response.setValue(
          "remainingAmountAfterPayment", elementToPayContext.getRemainingAmountAfterPayment());
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void updateFinancialDiscount(ActionRequest request, ActionResponse response) {
    try {
      PayVoucherElementToPay payVoucherElementToPay =
          request.getContext().asType(PayVoucherElementToPay.class);

      Beans.get(PayVoucherElementToPayService.class)
          .updateFinancialDiscount(payVoucherElementToPay);

      response.setValues(payVoucherElementToPay);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
