package com.axelor.apps.account.db.repo;

import javax.persistence.PersistenceException;

import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.service.move.MoveSequenceService;
import com.axelor.apps.base.db.Period;
import com.axelor.apps.base.service.PeriodService;
import com.axelor.apps.base.service.administration.GeneralService;
import com.axelor.exception.AxelorException;
import com.axelor.inject.Beans;
import com.google.inject.Inject;

public class MoveManagementRepository extends MoveRepository {

	@Inject
	protected GeneralService generalService;

	@Override
	public Move copy(Move entity, boolean deep) {

		Move copy = super.copy(entity, deep);

		Period period=null;
		try {
			period = Beans.get(PeriodService.class).rightPeriod(entity.getDate(),entity.getCompany());
		} catch (AxelorException e) {
			throw new PersistenceException(e.getLocalizedMessage());
		}
		copy.setStatusSelect(STATUS_DRAFT);
		copy.setReference(null);
		copy.setDate(generalService.getTodayDate());
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
	
	
	@Override
	public Move save(Move move) {
		try {

			Beans.get(MoveSequenceService.class).setDraftSequence(move);

			return super.save(move);
		} catch (Exception e) {
			throw new PersistenceException(e.getLocalizedMessage());
		}
	}
	

	@Override
	public void remove(Move entity){

		//try{
			//Beans.get(PeriodService.class).testOpenPeriod(entity.getPeriod());
			super.remove(entity);
		//} catch (AxelorException e) {
		//	e.printStackTrace();
		//}

	}
}
