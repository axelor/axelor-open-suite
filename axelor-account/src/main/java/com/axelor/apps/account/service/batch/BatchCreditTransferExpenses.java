package com.axelor.apps.account.service.batch;

import com.axelor.i18n.I18n;
import com.google.inject.Inject;

public class BatchCreditTransferExpenses extends BatchStrategy {

	@Override
	protected void process() {
		throw new UnsupportedOperationException(
				I18n.get("This batch requires the cash management module."));
	}

}
