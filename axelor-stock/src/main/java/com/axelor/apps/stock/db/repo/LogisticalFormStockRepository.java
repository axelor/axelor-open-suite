package com.axelor.apps.stock.db.repo;

import javax.persistence.PersistenceException;

import com.axelor.apps.base.db.Sequence;
import com.axelor.apps.base.service.administration.SequenceService;
import com.axelor.apps.stock.db.LogisticalForm;
import com.axelor.apps.stock.exception.IExceptionMessage;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.google.common.base.Strings;

public class LogisticalFormStockRepository extends LogisticalFormRepository {

	@Override
	public LogisticalForm save(LogisticalForm logisticalForm) {
		try {
			if (Strings.isNullOrEmpty(logisticalForm.getDeliveryNumber()) && logisticalForm.getCompany() != null) {
				String sequenceNumber = Beans.get(SequenceService.class).getSequenceNumber("logisticalForm",
						logisticalForm.getCompany());
				if (Strings.isNullOrEmpty(sequenceNumber)) {
					throw new AxelorException(Sequence.class, IException.NO_VALUE,
							I18n.get(IExceptionMessage.LOGISTICAL_FORM_MISSING_SEQUENCE),
							logisticalForm.getCompany().getName());
				}
				logisticalForm.setDeliveryNumber(sequenceNumber);
			}

			return super.save(logisticalForm);
		} catch (Exception e) {
			throw new PersistenceException(e.getLocalizedMessage());
		}
	}

}
