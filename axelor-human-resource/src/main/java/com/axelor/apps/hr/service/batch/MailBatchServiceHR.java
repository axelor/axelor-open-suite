/**
 * Axelor Business Solutions
 *
 * Copyright (C) 2016 Axelor (<http://axelor.com>).
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
package com.axelor.apps.hr.service.batch;

import com.axelor.apps.base.db.Batch;
import com.axelor.apps.base.db.MailBatch;
import com.axelor.apps.base.db.repo.MailBatchRepository;
import com.axelor.apps.base.exceptions.IExceptionMessage;
import com.axelor.apps.base.service.batch.MailBatchService;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;

public class MailBatchServiceHR extends MailBatchService{
	@Override
	public Batch run(String batchCode) throws AxelorException {
		Batch batch = super.run(batchCode);
		MailBatch mailBatch = mailBatchRepo.findByCode(batchCode);
		
		if (batchCode != null){
			switch (mailBatch.getActionSelect()) {
			case MailBatchRepository.ACTION_REMIN_TIMESHEET:
				batch = reminderTimesheet(mailBatch);
				break;
			
			default:
				throw new AxelorException(String.format(I18n.get(IExceptionMessage.BASE_BATCH_1), mailBatch.getActionSelect(), batchCode), IException.INCONSISTENCY);
			}
		}
		else {
			throw new AxelorException(String.format(I18n.get(IExceptionMessage.BASE_BATCH_2), batchCode), IException.INCONSISTENCY);
		}
		
		return batch;
	}
	
	
	public Batch reminderTimesheet(MailBatch mailBatch) {
		
		return Beans.get(BatchReminderTimesheet.class).run(mailBatch);
		
	}
	
}
