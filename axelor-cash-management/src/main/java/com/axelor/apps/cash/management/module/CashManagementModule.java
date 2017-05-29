package com.axelor.apps.cash.management.module;

import com.axelor.app.AxelorModule;
import com.axelor.apps.account.service.batch.BatchCreditTransferExpensePayment;
import com.axelor.apps.cash.management.service.batch.BatchCreditTransferExpensePaymentCashManagement;

public class CashManagementModule extends AxelorModule {

	@Override
	protected void configure() {
		bind(BatchCreditTransferExpensePayment.class).to(BatchCreditTransferExpensePaymentCashManagement.class);
	}

}
