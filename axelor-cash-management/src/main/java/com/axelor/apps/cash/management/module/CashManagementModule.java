package com.axelor.apps.cash.management.module;

import com.axelor.app.AxelorModule;
import com.axelor.apps.account.service.batch.BatchCreditTransferExpenses;
import com.axelor.apps.cash.management.service.batch.BatchCreditTransferExpensesCashManagement;

public class CashManagementModule extends AxelorModule {

	@Override
	protected void configure() {
		bind(BatchCreditTransferExpenses.class).to(BatchCreditTransferExpensesCashManagement.class);
	}

}
