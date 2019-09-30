/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2019 Axelor (<http://axelor.com>).
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

import com.axelor.apps.account.db.InterbankCodeLine;
import com.axelor.apps.account.db.repo.InterbankCodeLineRepository;
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
import com.google.common.collect.Lists;
import com.google.inject.Singleton;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

@Singleton
public class PaymentScheduleLineController {

  @SuppressWarnings("unchecked")
  public void showRejectWizard(ActionRequest request, ActionResponse response) {
    try {
      List<? extends Number> idList;
      Number id = (Number) request.getContext().get("id");

      if (id != null) {
        idList = Lists.newArrayList(id);
      } else {
        idList =
            MoreObjects.firstNonNull(
                (List<? extends Number>) request.getContext().get("_ids"), Collections.emptyList());
      }

      if (idList.isEmpty()) {
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
              .context("idList", idList);

      response.setView(view.map());
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void reject(ActionRequest request, ActionResponse response) {
    try {
      @SuppressWarnings("unchecked")
      List<? extends Number> idList =
          MoreObjects.firstNonNull(
              (List<? extends Number>) request.getContext().get("idList"), Collections.emptyList());
      boolean represent = (boolean) request.getContext().get("represent");

      // TODO: one rejection reason per payment schedule line
      InterbankCodeLine interbankCodeLine;
      @SuppressWarnings("unchecked")
      Map<String, Object> interbankCodeLineMap =
          (Map<String, Object>) request.getContext().get("interbankCodeLine");

      if (interbankCodeLineMap != null) {
        interbankCodeLine =
            Beans.get(InterbankCodeLineRepository.class)
                .find(((Number) interbankCodeLineMap.get("id")).longValue());
      } else {
        interbankCodeLine = null;
      }

      Map<Long, InterbankCodeLine> idMap = new LinkedHashMap<>();
      idList.stream().map(Number::longValue).forEach(id -> idMap.put(id, interbankCodeLine));

      PaymentScheduleLineBankPaymentService paymentScheduleLineBankPaymentService =
          Beans.get(PaymentScheduleLineBankPaymentService.class);

      if (idMap.size() == 1) {
        Entry<Long, InterbankCodeLine> entry = idMap.entrySet().iterator().next();
        long id = entry.getKey();
        InterbankCodeLine rejectionReason = entry.getValue();
        paymentScheduleLineBankPaymentService.reject(id, rejectionReason, represent);
      } else {
        int errorCount = paymentScheduleLineBankPaymentService.rejectFromIdMap(idMap, represent);

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
                  "%d line successfully rejected", "%d lines successfully rejected", idMap.size()),
              idMap.size()));
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
