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

import com.axelor.apps.account.db.PaymentSession;
import com.axelor.apps.account.db.repo.PaymentSessionRepository;
import com.axelor.apps.account.service.PaymentSessionService;
import com.axelor.apps.account.service.invoice.InvoiceTermService;
import com.axelor.exception.service.TraceBackService;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Singleton;

@Singleton
public class PaymentSessionController {

  public void setBankDetails(ActionRequest request, ActionResponse response) {
    try {
      PaymentSession paymentSession = request.getContext().asType(PaymentSession.class);
      Beans.get(PaymentSessionService.class).setBankDetails(paymentSession);
      response.setValue("bankDetails", paymentSession.getBankDetails());
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void setJournal(ActionRequest request, ActionResponse response) {
    try {
      PaymentSession paymentSession = request.getContext().asType(PaymentSession.class);
      Beans.get(PaymentSessionService.class).setJournal(paymentSession);
      response.setValue("journal", paymentSession.getJournal());
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void computeTotal(ActionRequest request, ActionResponse response) {
    try {
      PaymentSession paymentSession = request.getContext().asType(PaymentSession.class);
      paymentSession = Beans.get(PaymentSessionRepository.class).find(paymentSession.getId());
      Beans.get(InvoiceTermService.class).computeTotalPaymentSession(paymentSession);
      response.setAttr("searchPanel", "refresh", true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
