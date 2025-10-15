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
package com.axelor.apps.bankpayment.service;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.PaymentMode;
import com.axelor.apps.account.db.PaymentSchedule;
import com.axelor.apps.account.db.repo.PaymentScheduleLineRepository;
import com.axelor.apps.account.db.repo.PaymentScheduleRepository;
import com.axelor.apps.account.service.PaymentScheduleLineService;
import com.axelor.apps.account.service.PaymentScheduleServiceImpl;
import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.bankpayment.service.bankdetails.BankDetailsBankPaymentService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.BankDetails;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.repo.PartnerRepository;
import com.axelor.apps.base.service.PartnerService;
import com.axelor.apps.base.service.administration.SequenceService;
import com.google.inject.Inject;
import java.time.LocalDate;

public class PaymentScheduleServiceBankPaymentImpl extends PaymentScheduleServiceImpl {
  protected BankDetailsBankPaymentService bankDetailsBankPaymentService;

  @Inject
  public PaymentScheduleServiceBankPaymentImpl(
      AppAccountService appAccountService,
      PaymentScheduleLineService paymentScheduleLineService,
      PaymentScheduleLineRepository paymentScheduleLineRepo,
      SequenceService sequenceService,
      PaymentScheduleRepository paymentScheduleRepo,
      PartnerRepository partnerRepository,
      PartnerService partnerService,
      BankDetailsBankPaymentService bankDetailsBankPaymentService) {
    super(
        appAccountService,
        paymentScheduleLineService,
        paymentScheduleLineRepo,
        sequenceService,
        paymentScheduleRepo,
        partnerRepository,
        partnerService);
    this.bankDetailsBankPaymentService = bankDetailsBankPaymentService;
  }

  @Override
  public PaymentSchedule createPaymentSchedule(
      Partner partner,
      Invoice invoice,
      Company company,
      LocalDate date,
      LocalDate startDate,
      int nbrTerm,
      BankDetails bankDetails,
      PaymentMode paymentMode)
      throws AxelorException {

    PaymentSchedule paymentSchedule =
        super.createPaymentSchedule(
            partner, invoice, company, date, startDate, nbrTerm, bankDetails, paymentMode);

    bankDetailsBankPaymentService
        .getBankDetailsLinkedToActiveUmr(paymentMode, partner, company)
        .stream()
        .findAny()
        .ifPresent(paymentSchedule::setBankDetails);

    return paymentSchedule;
  }
}
