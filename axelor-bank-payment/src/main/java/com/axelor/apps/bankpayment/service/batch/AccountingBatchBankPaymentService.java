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
package com.axelor.apps.bankpayment.service.batch;

import com.axelor.apps.account.db.AccountingBatch;
import com.axelor.apps.account.db.repo.AccountingBatchRepository;
import com.axelor.apps.account.service.batch.AccountingBatchService;
import com.axelor.apps.bankpayment.service.app.AppBankPaymentService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Batch;
import com.axelor.db.Model;
import com.axelor.inject.Beans;

public class AccountingBatchBankPaymentService extends AccountingBatchService {

  @Override
  public Batch run(Model batchModel) throws AxelorException {
    Batch batch;
    AccountingBatch accountingBatch = (AccountingBatch) batchModel;

    if (!Beans.get(AppBankPaymentService.class).isApp("bank-payment")) {
      return super.run(accountingBatch);
    }

    switch (accountingBatch.getActionSelect()) {
      case AccountingBatchRepository.ACTION_DIRECT_DEBIT:
        batch = directDebit(accountingBatch);
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

  public Batch billOfExchange(AccountingBatch accountingBatch) {

    switch (accountingBatch.getBillOfExchangeStepBatchSelect()) {
      case AccountingBatchRepository.BILL_OF_EXCHANGE_BATCH_STATUS_BOE_GENERATION:
        if (accountingBatch.getBillOfExchangeDataTypeSelect()
            == AccountingBatchRepository.BILL_OF_EXCHANGE_DATA_CUSTOMER_INVOICE) {
          return Beans.get(BatchBillOfExchange.class).run(accountingBatch);
        } else {
          throw new IllegalArgumentException("Invalid data type");
        }

      case AccountingBatchRepository.BILL_OF_EXCHANGE_BATCH_STATUS_SEND_BILLING:
        return Beans.get(BatchBillOfExchangeSendBilling.class).run(accountingBatch);

      case AccountingBatchRepository.BILL_OF_EXCHANGE_BATCH_STATUS_BANK_ORDER_GENERATION:
        return Beans.get(BatchBankOrderGenerationBillOfExchange.class).run(accountingBatch);
      default:
        throw new IllegalArgumentException("Invalid data type");
    }
  }
}
