/**
 * Axelor Business Solutions
 *
 * Copyright (C) 2017 Axelor (<http://axelor.com>).
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

import com.axelor.apps.account.db.BankOrder;
import com.axelor.apps.account.service.bankorder.BankOrderService;
import com.axelor.inject.Beans;

public class BankOrderManagementRepository extends BankOrderRepository {
	
	
	@Override
	public BankOrder save(BankOrder entity)  {
		
		try {
			
			BankOrderService bankOrderService = Beans.get(BankOrderService.class);
			bankOrderService.generateSequence(entity);
			if(entity.getStatusSelect() == BankOrderRepository.STATUS_DRAFT || entity.getStatusSelect() == BankOrderRepository.STATUS_AWAITING_SIGNATURE)  {
				bankOrderService.updateTotalAmounts(entity);
			}		
			
			return super.save(entity);
		} catch (Exception e) {
			throw new PersistenceException(e.getLocalizedMessage());
		}
	}
}
