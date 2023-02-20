/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2023 Axelor (<http://axelor.com>).
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

import com.axelor.apps.account.db.DepositSlip;
import com.axelor.apps.account.db.PaymentVoucher;
import com.axelor.apps.account.db.repo.DepositSlipRepository;
import com.axelor.apps.account.service.DepositSlipService;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.db.mapper.Adapter;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Singleton;
import java.time.LocalDate;
import java.util.List;

@Singleton
public class DepositSlipController {

  public void loadPaymentVoucherDueList(ActionRequest request, ActionResponse response) {

    DepositSlip depositSlip = request.getContext().asType(DepositSlip.class);
    DepositSlipService depositSlipService = Beans.get(DepositSlipService.class);
    response.setValue(
        "__paymentVoucherDueList", depositSlipService.fetchPaymentVouchers(depositSlip));
  }

  public void updateDepositChequeDate(ActionRequest request, ActionResponse response) {

    LocalDate chequeDate =
        (LocalDate)
            Adapter.adapt(
                request.getContext().get("__depositDate"), LocalDate.class, LocalDate.class, null);
    DepositSlip depositSlip = request.getContext().asType(DepositSlip.class);
    List<PaymentVoucher> paymentVouchers = depositSlip.getPaymentVoucherList();
    paymentVouchers.stream().forEach(paymentVoucher -> paymentVoucher.setChequeDate(chequeDate));
    response.setValue("paymentVoucherList", paymentVouchers);
  }

  public void publish(ActionRequest request, ActionResponse response) {
    DepositSlip depositSlip = request.getContext().asType(DepositSlip.class);
    depositSlip = Beans.get(DepositSlipRepository.class).find(depositSlip.getId());
    DepositSlipService depositSlipService = Beans.get(DepositSlipService.class);

    try {
      depositSlipService.publish(depositSlip);
      response.setNotify(I18n.get("The deposit slip is published"));
      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void validate(ActionRequest request, ActionResponse response) {
    DepositSlip depositSlip = request.getContext().asType(DepositSlip.class);
    depositSlip = Beans.get(DepositSlipRepository.class).find(depositSlip.getId());
    DepositSlipService depositSlipService = Beans.get(DepositSlipService.class);

    try {
      depositSlipService.validate(depositSlip);
      response.setNotify(I18n.get("The deposit slip is validated"));
      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
