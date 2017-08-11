package com.axelor.apps.supplychain.service.batch;

import com.axelor.apps.base.service.administration.AbstractBatch;
import com.axelor.apps.supplychain.exception.IExceptionMessage;
import com.axelor.i18n.I18n;

public abstract class BatchOrderInvoicing extends AbstractBatch {

	@Override
	protected void stop() {
		StringBuilder sb = new StringBuilder();
		sb.append(I18n.get(IExceptionMessage.BATCH_ORDER_INVOICING_REPORT));
		sb.append(String.format(I18n.get(IExceptionMessage.BATCH_ORDER_INVOICING_DONE_SINGULAR,
				IExceptionMessage.BATCH_ORDER_INVOICING_DONE_PLURAL, batch.getDone()), batch.getDone()));
		sb.append(String
				.format(I18n.get(com.axelor.apps.base.exceptions.IExceptionMessage.ABSTRACT_BATCH_ANOMALY_SINGULAR,
						com.axelor.apps.base.exceptions.IExceptionMessage.ABSTRACT_BATCH_ANOMALY_PLURAL,
						batch.getAnomaly()), batch.getAnomaly()));
		addComment(sb.toString());
		super.stop();
	}

}
