package com.axelor.apps.account.service.batch;

import com.axelor.i18n.I18n;

public class BatchCreditTransferExpensePayment extends BatchStrategy {

	@Override
	protected void process() {
		throw new UnsupportedOperationException(I18n.get("This batch requires the human resources module."));
	}

}
