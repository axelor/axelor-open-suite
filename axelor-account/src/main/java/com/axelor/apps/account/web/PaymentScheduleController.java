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
package com.axelor.apps.account.web;

import com.axelor.apps.account.db.PaymentMode;
import com.axelor.apps.account.db.PaymentSchedule;
import com.axelor.apps.account.db.repo.PaymentScheduleRepository;
import com.axelor.apps.account.exception.IExceptionMessage;
import com.axelor.apps.account.service.IrrecoverableService;
import com.axelor.apps.account.service.PaymentScheduleService;
import com.axelor.apps.base.db.BankDetails;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.repo.PartnerRepository;
import com.axelor.apps.base.db.repo.SequenceRepository;
import com.axelor.apps.base.service.BankDetailsService;
import com.axelor.apps.base.service.PartnerService;
import com.axelor.apps.base.service.administration.SequenceService;
import com.axelor.exception.AxelorException;
import com.axelor.exception.ResponseMessageType;
import com.axelor.exception.service.TraceBackService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.common.base.Strings;
import com.google.inject.Singleton;

@Singleton
public class PaymentScheduleController {

  // Validation button
  public void validate(ActionRequest request, ActionResponse response) {

    PaymentSchedule paymentSchedule = request.getContext().asType(PaymentSchedule.class);
    paymentSchedule = Beans.get(PaymentScheduleRepository.class).find(paymentSchedule.getId());

    try {
      Beans.get(PaymentScheduleService.class).validatePaymentSchedule(paymentSchedule);
      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  // Cancel button
  public void cancel(ActionRequest request, ActionResponse response) {

    PaymentSchedule paymentSchedule = request.getContext().asType(PaymentSchedule.class);
    paymentSchedule = Beans.get(PaymentScheduleRepository.class).find(paymentSchedule.getId());

    try {
      Beans.get(PaymentScheduleService.class).toCancelPaymentSchedule(paymentSchedule);
      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  // Called on onSave event
  public void paymentScheduleScheduleId(ActionRequest request, ActionResponse response) {
    try {
      PaymentSchedule paymentSchedule = request.getContext().asType(PaymentSchedule.class);
      Beans.get(PaymentScheduleService.class).checkTotalLineAmount(paymentSchedule);

      if (Strings.isNullOrEmpty(paymentSchedule.getPaymentScheduleSeq())) {

        String num =
            Beans.get(SequenceService.class)
                .getSequenceNumber(
                    SequenceRepository.PAYMENT_SCHEDULE, paymentSchedule.getCompany());

        if (Strings.isNullOrEmpty(num)) {
          response.setError(
              String.format(
                  I18n.get(IExceptionMessage.PAYMENT_SCHEDULE_5),
                  paymentSchedule.getCompany().getName()));
        } else {
          response.setValue("paymentScheduleSeq", num);
        }
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
  }

  /**
   * Called on partner, company or payment mode change. Fill the bank details with a default value.
   *
   * @param request
   * @param response
   * @throws AxelorException
   */
  public void fillCompanyBankDetails(ActionRequest request, ActionResponse response)
      throws AxelorException {
    PaymentSchedule paymentSchedule = request.getContext().asType(PaymentSchedule.class);
    PaymentMode paymentMode = paymentSchedule.getPaymentMode();
    Company company = paymentSchedule.getCompany();
    Partner partner = paymentSchedule.getPartner();

    if (company == null) {
      return;
    }
    if (partner != null) {
      partner = Beans.get(PartnerRepository.class).find(partner.getId());
    }
    BankDetails defaultBankDetails =
        Beans.get(BankDetailsService.class)
            .getDefaultCompanyBankDetails(company, paymentMode, partner, null);
    response.setValue("companyBankDetails", defaultBankDetails);
  }

  /**
   * Called on partner change. Fill the bank details with a default value.
   *
   * @param request
   * @param response
   */
  public void fillPartnerBankDetails(ActionRequest request, ActionResponse response) {
    PaymentSchedule paymentSchedule = request.getContext().asType(PaymentSchedule.class);
    Partner partner = paymentSchedule.getPartner();

    if (partner != null) {
      partner = Beans.get(PartnerRepository.class).find(partner.getId());
    }
    BankDetails defaultBankDetails = Beans.get(PartnerService.class).getDefaultBankDetails(partner);
    response.setValue("bankDetails", defaultBankDetails);
  }

  // Creating payment schedule lines button
  public void createPaymentScheduleLines(ActionRequest request, ActionResponse response) {

    PaymentSchedule paymentSchedule = request.getContext().asType(PaymentSchedule.class);
    paymentSchedule = Beans.get(PaymentScheduleRepository.class).find(paymentSchedule.getId());

    Beans.get(PaymentScheduleService.class).createPaymentScheduleLines(paymentSchedule);
    response.setReload(true);
  }

  public void passInIrrecoverable(ActionRequest request, ActionResponse response) {

    PaymentSchedule paymentSchedule = request.getContext().asType(PaymentSchedule.class);
    paymentSchedule = Beans.get(PaymentScheduleRepository.class).find(paymentSchedule.getId());

    try {
      Beans.get(IrrecoverableService.class).passInIrrecoverable(paymentSchedule);
      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void notPassInIrrecoverable(ActionRequest request, ActionResponse response) {

    PaymentSchedule paymentSchedule = request.getContext().asType(PaymentSchedule.class);
    paymentSchedule = Beans.get(PaymentScheduleRepository.class).find(paymentSchedule.getId());

    try {
      Beans.get(IrrecoverableService.class).notPassInIrrecoverable(paymentSchedule);
      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
