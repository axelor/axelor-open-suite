package com.axelor.apps.account.db.repo;

import com.axelor.apps.account.db.Move;
import com.axelor.apps.base.db.Period;
import com.axelor.apps.base.service.PeriodService;
import com.axelor.apps.base.service.administration.GeneralService;
import com.axelor.exception.AxelorException;
import com.axelor.inject.Beans;

public class MoveManagementRepository extends MoveRepository {
	@Override
	public Move copy(Move entity, boolean deep) {
		
		Move copy = super.copy(entity, deep);
		
		Period period=null;
		try {
			period = Beans.get(PeriodService.class).rightPeriod(entity.getDate(),entity.getCompany());
		} catch (AxelorException e) {
			e.printStackTrace();
		}
		copy.setStatusSelect(STATUS_DRAFT);
		copy.setReference(null);
		copy.setDate(GeneralService.getTodayDate());
		copy.setExportNumber(null);
		copy.setExportDate(null);
		copy.setMoveLineReport(null);
		copy.setValidationDate(null);
		copy.setPeriod(period);
		copy.setAccountingOk(false);
		copy.setIgnoreInReminderOk(false);
		copy.setPaymentVoucher(null);
		copy.setRejectOk(false);
		
		return copy;
	}
}
