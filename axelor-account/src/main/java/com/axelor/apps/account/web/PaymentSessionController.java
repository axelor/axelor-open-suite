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

import com.axelor.apps.account.db.AccountManagement;
import com.axelor.apps.account.db.PaymentSession;
import com.axelor.apps.base.db.BankDetails;
import com.axelor.exception.service.TraceBackService;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Singleton;
import java.util.Optional;
import org.apache.commons.collections.CollectionUtils;

@Singleton
public class PaymentSessionController {

  public void onChangeCompany(ActionRequest request, ActionResponse response) {
    try {
      PaymentSession paymentSession = request.getContext().asType(PaymentSession.class);
      if (paymentSession.getCompany() != null) {
        if (paymentSession.getPaymentMode() != null
            && CollectionUtils.isNotEmpty(
                paymentSession.getPaymentMode().getAccountManagementList())) {
          Optional<BankDetails> bankDetails =
              paymentSession.getPaymentMode().getAccountManagementList().stream()
                  .filter(
                      accountManagement ->
                          paymentSession.getCompany().equals(accountManagement.getCompany())
                              && accountManagement.getBankDetails() != null)
                  .map(AccountManagement::getBankDetails)
                  .findFirst();
          if (bankDetails.isPresent()) {
            response.setValue("bankDetails", bankDetails.get());
          }
        }
        response.setValue("currency", paymentSession.getCompany().getCurrency());
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
