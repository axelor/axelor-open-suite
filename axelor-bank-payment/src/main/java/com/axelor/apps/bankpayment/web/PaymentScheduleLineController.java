/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2018 Axelor (<http://axelor.com>).
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

import com.axelor.apps.bankpayment.service.PaymentScheduleLineBankPaymentService;
import com.axelor.apps.base.db.Wizard;
import com.axelor.exception.service.TraceBackService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.meta.schema.actions.ActionView.ActionViewBuilder;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.common.base.MoreObjects;
import com.google.inject.Singleton;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Singleton
public class PaymentScheduleLineController {

  public void showRejectWizard(ActionRequest request, ActionResponse response) {
    try {
      @SuppressWarnings("unchecked")
      List<? extends Number> contextIdList =
          MoreObjects.firstNonNull(
              (List<? extends Number>) request.getContext().get("_ids"), Collections.emptyList());

      if (contextIdList.isEmpty()) {
        response.setError(
            I18n.get(com.axelor.apps.base.exceptions.IExceptionMessage.RECORD_NONE_SELECTED));
        return;
      }

      ActionViewBuilder view =
          ActionView.define(I18n.get("Payment schedule lines to reject"))
              .model(Wizard.class.getName())
              .add("form", "payment-schedule-line-reject-wizard-form")
              .param("popup", "reload")
              .param("show-toolbar", Boolean.FALSE.toString())
              .param("show-confirm", Boolean.FALSE.toString())
              .param("popup-save", Boolean.FALSE.toString())
              .context("idList", contextIdList);

      response.setView(view.map());
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void reject(ActionRequest request, ActionResponse response) {
    try {
      @SuppressWarnings("unchecked")
      List<? extends Number> contextIdList =
          MoreObjects.firstNonNull(
              (List<Long>) request.getContext().get("idList"), Collections.emptyList());
      List<Long> idList =
          contextIdList.stream().map(Number::longValue).collect(Collectors.toList());
      boolean represent = (boolean) request.getContext().get("represent");
      PaymentScheduleLineBankPaymentService paymentScheduleLineBankPaymentService =
          Beans.get(PaymentScheduleLineBankPaymentService.class);

      if (idList.size() == 1) {
        paymentScheduleLineBankPaymentService.reject(idList.get(0), represent);
      } else {
        int errorCount = paymentScheduleLineBankPaymentService.rejectFromIdList(idList, represent);

        if (errorCount != 0) {
          response.setError(
              String.format(
                  I18n.get("%d errors occurred. Please check tracebacks for details."),
                  errorCount));
          return;
        }
      }

      response.setFlash(
          String.format(
              I18n.get(
                  "%d line successfully rejected", "%d lines successfully rejected", idList.size()),
              idList.size()));
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
