package com.axelor.apps.bankpayment.service.batch;

import com.axelor.apps.account.db.AccountingBatch;
import com.axelor.apps.account.db.repo.AccountingBatchRepository;
import com.axelor.apps.account.service.batch.AccountingBatchService;
import com.axelor.apps.base.db.Batch;
import com.axelor.db.Model;
import com.axelor.exception.AxelorException;
import com.axelor.inject.Beans;

public class AccountingBatchServiceBankPayment extends AccountingBatchService {

	@Override
	public Batch run(Model batchModel) throws AxelorException {
		Batch batch;
		AccountingBatch accountingBatch = (AccountingBatch) batchModel;

		switch (accountingBatch.getActionSelect()) {
		case AccountingBatchRepository.ACTION_DIRECT_DEBIT:
			batch = directDebit(accountingBatch);
			break;
		default:
			batch = super.run(accountingBatch);
		}

		return batch;
	}

	public Batch directDebit(AccountingBatch accountingBatch) {
		Class<? extends BatchDirectDebit> batchClass;

		switch (accountingBatch.getDirectDebitDataTypeSelect()) {
		case AccountingBatchRepository.DIRECT_DEBIT_DATA_CLIENT_INVOICE:
			batchClass = BatchDirectDebitInvoice.class;
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

}
