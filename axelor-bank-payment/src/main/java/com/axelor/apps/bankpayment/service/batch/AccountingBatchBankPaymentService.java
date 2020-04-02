/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2020 Axelor (<http://axelor.com>).
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
import com.axelor.apps.account.db.repo.AccountingBatchRepository;
import com.axelor.apps.account.service.batch.AccountingBatchService;
import com.axelor.apps.base.db.Batch;
import com.axelor.db.Model;
import com.axelor.exception.AxelorException;
import com.axelor.inject.Beans;

public class AccountingBatchBankPaymentService extends AccountingBatchService {

  @Override
  public Batch run(Model batchModel) throws AxelorException {
    Batch batch;
    AccountingBatch accountingBatch = (AccountingBatch) batchModel;

    switch (accountingBatch.getActionSelect()) {
      case AccountingBatchRepository.ACTION_DIRECT_DEBIT:
        batch = directDebit(accountingBatch);
        break;
      case AccountingBatchRepository.ACTION_BANK_STATEMENT:
        batch = bankStatement(accountingBatch);
        break;
      default:
        batch = super.run(accountingBatch);
    }

    return batch;
  }

  @Override
  public Batch directDebit(AccountingBatch accountingBatch) {
    Class<? extends BatchDirectDebit> batchClass;

    switch (accountingBatch.getDirectDebitDataTypeSelect()) {
      case AccountingBatchRepository.DIRECT_DEBIT_DATA_CUSTOMER_INVOICE:
        batchClass = BatchDirectDebitCustomerInvoice.class;
        break;
      case AccountingBatchRepository.DIRECT_DEBIT_DATA_PAYMENT_SCHEDULE:
        batchClass = BatchDirectDebitPaymentSchedule.class;
        break;
      case AccountingBatchRepository.DIRECT_DEBIT_DATA_MONTHLY_PAYMENT_SCHEDULE:
        batchClass = BatchDirectDebitMonthlyPaymentSchedule.class;
        break;
      default:
        throw new IllegalArgumentException("Invalid direct debit data type");
    }

    return Beans.get(batchClass).run(accountingBatch);
  }

  public Batch bankStatement(AccountingBatch accountingBatch) {
    return Beans.get(BatchBankStatement.class).run(accountingBatch);
  }
}
