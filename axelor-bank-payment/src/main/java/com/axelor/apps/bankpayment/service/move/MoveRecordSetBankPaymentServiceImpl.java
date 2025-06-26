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
package com.axelor.apps.bankpayment.service.move;

import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.PaymentMode;
import com.axelor.apps.account.service.JournalService;
import com.axelor.apps.account.service.PartnerAccountService;
import com.axelor.apps.account.service.PaymentConditionService;
import com.axelor.apps.account.service.PfpService;
import com.axelor.apps.account.service.invoice.InvoiceTermPfpToolService;
import com.axelor.apps.account.service.move.MoveToolService;
import com.axelor.apps.account.service.move.record.MoveRecordSetServiceImpl;
import com.axelor.apps.bankpayment.service.bankdetails.BankDetailsBankPaymentService;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.repo.PartnerRepository;
import com.axelor.apps.base.service.BankDetailsService;
import com.axelor.apps.base.service.PeriodService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.google.inject.Inject;

public class MoveRecordSetBankPaymentServiceImpl extends MoveRecordSetServiceImpl {
  protected final BankDetailsBankPaymentService bankDetailsBankPaymentService;

  @Inject
  public MoveRecordSetBankPaymentServiceImpl(
      PartnerRepository partnerRepository,
      BankDetailsService bankDetailsService,
      PeriodService periodService,
      PaymentConditionService paymentConditionService,
      AppBaseService appBaseService,
      PartnerAccountService partnerAccountService,
      JournalService journalService,
      PfpService pfpService,
      MoveToolService moveToolService,
      InvoiceTermPfpToolService invoiceTermPfpToolService,
      BankDetailsBankPaymentService bankDetailsBankPaymentService) {
    super(
        partnerRepository,
        bankDetailsService,
        periodService,
        paymentConditionService,
        appBaseService,
        partnerAccountService,
        journalService,
        pfpService,
        moveToolService,
        invoiceTermPfpToolService);
    this.bankDetailsBankPaymentService = bankDetailsBankPaymentService;
  }

  @Override
  public void setPartnerBankDetails(Move move) {
    super.setPartnerBankDetails(move);
    PaymentMode paymentMode = move.getPaymentMode();
    Partner partner = move.getPartner();
    Company company = move.getCompany();

    if (paymentMode != null && partner != null && company != null) {
      bankDetailsBankPaymentService
          .getBankDetailsLinkedToActiveUmr(paymentMode, partner, company)
          .stream()
          .findAny()
          .ifPresent(move::setPartnerBankDetails);
    }
  }
}
