package com.axelor.apps.bankpayment.service.batch;

import com.axelor.apps.base.exceptions.IExceptionMessage;
import com.axelor.i18n.I18n;

public abstract class BatchDirectDebit extends com.axelor.apps.account.service.batch.BatchStrategy {

	@Override
	protected void stop() {
		StringBuilder sb = new StringBuilder();
		sb.append(I18n.get(IExceptionMessage.ABSTRACT_BATCH_REPORT));
		sb.append(String.format(I18n.get(IExceptionMessage.ABSTRACT_BATCH_DONE_SINGULAR,
				IExceptionMessage.ABSTRACT_BATCH_DONE_SINGULAR, batch.getDone()), batch.getDone()));
		sb.append(String.format(I18n.get(IExceptionMessage.ABSTRACT_BATCH_ANOMALY_SINGULAR,
				IExceptionMessage.ABSTRACT_BATCH_ANOMALY_PLURAL, batch.getAnomaly()), batch.getAnomaly()));
		addComment(sb.toString());
		super.stop();
	}

}
