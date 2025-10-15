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

import com.axelor.apps.account.db.FiscalPosition;
import com.axelor.apps.account.db.Journal;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.PaymentMode;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.account.service.PaymentConditionService;
import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.account.service.move.MoveCreateServiceImpl;
import com.axelor.apps.bankpayment.service.bankdetails.BankDetailsBankPaymentService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.BankDetails;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Currency;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.service.PeriodService;
import com.axelor.apps.base.service.config.CompanyConfigService;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.time.LocalDate;

public class MoveCreateBankPaymentServiceImpl extends MoveCreateServiceImpl {
  protected final BankDetailsBankPaymentService bankDetailsBankPaymentService;

  @Inject
  public MoveCreateBankPaymentServiceImpl(
      AppAccountService appAccountService,
      PeriodService periodService,
      MoveRepository moveRepository,
      CompanyConfigService companyConfigService,
      PaymentConditionService paymentConditionService,
      BankDetailsBankPaymentService bankDetailsBankPaymentService) {
    super(
        appAccountService,
        periodService,
        moveRepository,
        companyConfigService,
        paymentConditionService);
    this.bankDetailsBankPaymentService = bankDetailsBankPaymentService;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public Move createMove(
      Journal journal,
      Company company,
      Currency currency,
      Partner partner,
      LocalDate date,
      LocalDate originDate,
      PaymentMode paymentMode,
      FiscalPosition fiscalPosition,
      BankDetails bankDetails,
      int technicalOriginSelect,
      int functionalOriginSelect,
      boolean ignoreInDebtRecoveryOk,
      boolean ignoreInAccountingOk,
      boolean autoYearClosureMove,
      String origin,
      String description,
      BankDetails companyBankDetails)
      throws AxelorException {

    Move move =
        super.createMove(
            journal,
            company,
            currency,
            partner,
            date,
            originDate,
            paymentMode,
            fiscalPosition,
            bankDetails,
            technicalOriginSelect,
            functionalOriginSelect,
            ignoreInDebtRecoveryOk,
            ignoreInAccountingOk,
            autoYearClosureMove,
            origin,
            description,
            companyBankDetails);

    bankDetailsBankPaymentService
        .getBankDetailsLinkedToActiveUmr(paymentMode, partner, company)
        .stream()
        .findAny()
        .ifPresent(move::setPartnerBankDetails);

    moveRepository.save(move);
    return move;
  }
}
