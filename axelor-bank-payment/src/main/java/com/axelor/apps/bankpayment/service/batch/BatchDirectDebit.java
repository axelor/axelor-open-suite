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
package com.axelor.apps.bankpayment.service.batch;

import com.axelor.apps.account.db.AccountingBatch;
import com.axelor.apps.account.db.PaymentMode;
import com.axelor.apps.account.db.repo.PaymentScheduleLineRepository;
import com.axelor.apps.bankpayment.exception.IExceptionMessage;
import com.axelor.apps.base.db.BankDetails;
import com.axelor.apps.base.service.BankDetailsService;
import com.axelor.exception.AxelorException;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;

public abstract class BatchDirectDebit extends com.axelor.apps.account.service.batch.BatchStrategy {
  protected boolean generateBankOrderFlag;

  @Inject protected BankDetailsService bankDetailsService;

  @Inject protected BatchBankPaymentService batchBankPaymentService;

  @Inject PaymentScheduleLineRepository paymentScheduleLineRepo;

  @Override
  protected void start() throws IllegalAccessException, AxelorException {
    super.start();
    PaymentMode directDebitPaymentMode = batch.getAccountingBatch().getPaymentMode();
    generateBankOrderFlag =
        directDebitPaymentMode != null && directDebitPaymentMode.getGenerateBankOrder();
  }

  @Override
  protected void stop() {
    StringBuilder sb = new StringBuilder();
    sb.append(I18n.get(com.axelor.apps.base.exceptions.IExceptionMessage.ABSTRACT_BATCH_REPORT))
        .append(" ");
    sb.append(
        String.format(
            I18n.get(
                    com.axelor.apps.base.exceptions.IExceptionMessage.ABSTRACT_BATCH_DONE_SINGULAR,
                    com.axelor.apps.base.exceptions.IExceptionMessage.ABSTRACT_BATCH_DONE_PLURAL,
                    batch.getDone())
                + " ",
            batch.getDone()));
    sb.append(
        String.format(
            I18n.get(
                com.axelor.apps.base.exceptions.IExceptionMessage.ABSTRACT_BATCH_ANOMALY_SINGULAR,
                com.axelor.apps.base.exceptions.IExceptionMessage.ABSTRACT_BATCH_ANOMALY_PLURAL,
                batch.getAnomaly()),
            batch.getAnomaly()));
    addComment(sb.toString());
    super.stop();
  }

  protected BankDetails getCompanyBankDetails(AccountingBatch accountingBatch) {
    BankDetails companyBankDetails = accountingBatch.getBankDetails();

    if (companyBankDetails == null && accountingBatch.getCompany() != null) {
      companyBankDetails = accountingBatch.getCompany().getDefaultBankDetails();
    }

    if (companyBankDetails == null && generateBankOrderFlag) {
      throw new IllegalArgumentException(
          I18n.get(IExceptionMessage.BATCH_DIRECT_DEBIT_MISSING_COMPANY_BANK_DETAILS));
    }

    return companyBankDetails;
  }
}
