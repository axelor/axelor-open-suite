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
import org.apache.commons.collections.CollectionUtils;

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

  public void loadPaymentVoucher(ActionRequest request, ActionResponse response) {
    DepositSlipService depositSlipService = Beans.get(DepositSlipService.class);

    List paymentVoucherDueList = (List) request.getContext().get("__paymentVoucherDueList");
    if (CollectionUtils.isEmpty(paymentVoucherDueList)
        || Integer.class.getName().equals(paymentVoucherDueList.get(0).getClass().getName())) {
      return;
    }

    List<Integer> selectedPaymentVoucherDueIdList =
        depositSlipService.getSelectedPaymentVoucherDueIdList(paymentVoucherDueList);
    if (CollectionUtils.isEmpty(selectedPaymentVoucherDueIdList)) {
      return;
    }

    response.setAttr("paymentVoucherList", "value:add", selectedPaymentVoucherDueIdList);
  }

  public void updateInvoicePayments(ActionRequest request, ActionResponse response) {
    try {
      DepositSlip depositSlip = request.getContext().asType(DepositSlip.class);
      LocalDate depositDate =
          (LocalDate)
              Adapter.adapt(
                  request.getContext().get("__depositDate"),
                  LocalDate.class,
                  LocalDate.class,
                  null);
      Beans.get(DepositSlipService.class).updateInvoicePayments(depositSlip, depositDate);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
