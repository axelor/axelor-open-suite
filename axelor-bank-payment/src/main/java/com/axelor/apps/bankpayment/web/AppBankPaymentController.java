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
package com.axelor.apps.bankpayment.web;

import com.axelor.apps.bankpayment.service.app.AppBankPaymentService;
import com.axelor.apps.bankpayment.service.bankorder.BankOrderEncryptionService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.studio.db.AppBankPayment;
import com.google.inject.Singleton;

@Singleton
public class AppBankPaymentController {

  public void generateBankPaymentConfigurations(ActionRequest request, ActionResponse response) {
    try {
      Beans.get(AppBankPaymentService.class).generateBankPaymentConfigurations();
      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void checkPasswordPresentInConfig(ActionRequest request, ActionResponse response)
      throws AxelorException {
    AppBankPayment appBankPayment = request.getContext().asType(AppBankPayment.class);

    if (appBankPayment.getEnableBankOrderFileEncryption()) {
      try {
        Beans.get(BankOrderEncryptionService.class).checkAndGetEncryptionPassword();
      } catch (AxelorException e) {
        response.setValue("enableBankOrderFileEncryption", false);
        response.setError(e.getMessage());
      }
    }
  }
}
