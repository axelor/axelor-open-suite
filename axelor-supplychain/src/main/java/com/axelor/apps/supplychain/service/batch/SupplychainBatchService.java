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
package com.axelor.apps.supplychain.service.batch;

import com.axelor.apps.base.db.Batch;
import com.axelor.apps.base.exceptions.IExceptionMessage;
import com.axelor.apps.supplychain.db.SupplychainBatch;
import com.axelor.apps.supplychain.db.repo.SupplychainBatchRepository;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;

public class SupplychainBatchService {

	@Inject
	protected BatchSubscription batchSubscription;
	
	@Inject
	protected SupplychainBatchRepository supplychainBatchRepo;

	public Batch run(String batchCode) throws AxelorException {

		Batch batch;
		SupplychainBatch supplychainBatch = supplychainBatchRepo.findByCode(batchCode);

		if (batchCode != null){
			switch (supplychainBatch.getActionSelect()) {
			case SupplychainBatchRepository.ACTION_BILL_SUB:
				batch = billSubscriptions(supplychainBatch);
				break;
			default:
				throw new AxelorException(String.format(I18n.get(IExceptionMessage.BASE_BATCH_1), supplychainBatch.getActionSelect(), batchCode), IException.INCONSISTENCY);
			}
		}
		else {
			throw new AxelorException(String.format(I18n.get(IExceptionMessage.BASE_BATCH_2), batchCode), IException.INCONSISTENCY);
		}

		return batch;
	}

	public Batch billSubscriptions(SupplychainBatch supplychainBatch){
		return batchSubscription.run(supplychainBatch);
	}

}
