package com.axelor.apps.account.db.repo;

import org.joda.time.LocalDate;

import com.axelor.apps.account.db.Move;
import com.axelor.apps.base.db.Period;
import com.axelor.apps.base.service.PeriodService;
import com.axelor.exception.AxelorException;
import com.axelor.inject.Beans;

public class MoveManagementRepository extends MoveRepository {
	@Override
	public Move copy(Move entity, boolean deep) {
		Period period=null;
		try {
			period = Beans.get(PeriodService.class).rightPeriod(entity.getDate(),entity.getCompany());
		} catch (AxelorException e) {
			e.printStackTrace();
		}
		entity.setStatusSelect(1);
		entity.setReference(null);
		entity.setDate(new LocalDate());
		entity.setExportNumber(null);
		entity.setExportDate(null);
		entity.setMoveLineReport(null);
		entity.setValidationDate(null);
		entity.setPeriod(period);
		return super.copy(entity, deep);
	}
}
