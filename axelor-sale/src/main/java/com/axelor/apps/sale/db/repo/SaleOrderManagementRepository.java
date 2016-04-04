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
package com.axelor.apps.sale.db.repo;

import javax.persistence.PersistenceException;

import com.axelor.apps.base.service.administration.GeneralService;
import com.axelor.apps.sale.db.ISaleOrder;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.service.SaleOrderService;
import com.axelor.db.JPA;
import com.axelor.inject.Beans;
import com.google.common.base.Strings;
import com.google.inject.Inject;

public class SaleOrderManagementRepository extends SaleOrderRepository {

	@Inject
	protected GeneralService generalService;

	@Override
	public SaleOrder copy(SaleOrder entity, boolean deep) {

		SaleOrder copy = super.copy(entity, deep);

		copy.setStatusSelect(ISaleOrder.STATUS_DRAFT);
		copy.setSaleOrderSeq(null);
		copy.clearBatchSet();
		copy.setImportId(null);
		copy.setCreationDate(generalService.getTodayDate());
		copy.setConfirmationDate(null);
		copy.setConfirmedByUser(null);
		copy.setOrderDate(null);
		copy.setOrderNumber(null);
		copy.setVersionNumber(1);

		return copy;
	}

	@Override
	public SaleOrder save(SaleOrder saleOrder) {
		try {
			computeSeq(saleOrder);
			computeFullName(saleOrder);
			return super.save(saleOrder);
		} catch (Exception e) {
			throw new PersistenceException(e.getLocalizedMessage());
		}
	}
	
	public void computeSeq(SaleOrder saleOrder){
		try{
			if((saleOrder.getSaleOrderSeq() == null || Strings.isNullOrEmpty(saleOrder.getSaleOrderSeq())) && !saleOrder.getTemplate())
				saleOrder.setSaleOrderSeq(Beans.get(SaleOrderService.class).getSequence(saleOrder.getCompany()));
		}
		catch (Exception e) {
			throw new PersistenceException(e.getLocalizedMessage());
		}
	}
	
	public void computeFullName(SaleOrder saleOrder){
		try{
			if(!Strings.isNullOrEmpty(saleOrder.getSaleOrderSeq()))
				saleOrder.setFullName(saleOrder.getSaleOrderSeq()+"-"+saleOrder.getClientPartner().getName());
			else
				saleOrder.setFullName(saleOrder.getClientPartner().getName());
		}
		catch (Exception e) {
			throw new PersistenceException(e.getLocalizedMessage());
		}
	}
}
