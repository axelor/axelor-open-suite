/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2018 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or  modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.axelor.apps.account.db.repo;

import javax.persistence.PersistenceException;

import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.exception.IExceptionMessage;
import com.axelor.apps.account.service.move.MoveSequenceService;
import com.axelor.apps.base.db.Period;
import com.axelor.apps.base.service.PeriodService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.exception.AxelorException;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;

public class MoveManagementRepository extends MoveRepository {

	@Override
	public Move copy(Move entity, boolean deep) {

		Move copy = super.copy(entity, deep);

		Period period=null;
		try {
			period = Beans.get(PeriodService.class).rightPeriod(entity.getDate(),entity.getCompany());
		} catch (AxelorException e) {
			throw new PersistenceException(e.getLocalizedMessage());
		}
		copy.setStatusSelect(STATUS_NEW);
		copy.setReference(null);
		copy.setDate(Beans.get(AppBaseService.class).getTodayDate());
		copy.setExportNumber(null);
		copy.setExportDate(null);
		copy.setAccountingReport(null);
		copy.setValidationDate(null);
		copy.setPeriod(period);
		copy.setAccountingOk(false);
		copy.setIgnoreInDebtRecoveryOk(false);
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
		
		if(!entity.getStatusSelect().equals(MoveRepository.STATUS_NEW)){
			throw new PersistenceException(I18n.get(IExceptionMessage.MOVE_ARCHIVE_NOT_OK));
		}else{
			entity.setArchived(true);
		}
	}
}
