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
package com.axelor.apps.sale.service;

import com.axelor.apps.base.service.AddressServiceImpl;
import com.axelor.apps.sale.db.repo.SaleOrderRepository;
import com.google.inject.Inject;


public class AddressServiceSaleImpl extends AddressServiceImpl  {
	
	@Inject
	private SaleOrderRepository saleOrderRepo;
	
	@Override
	public boolean checkAddressUsed(Long addressId){

		super.checkAddressUsed(addressId);
		
		if(addressId != null){
			if(saleOrderRepo.all().filter("self.mainInvoicingAddress.id = ?1 OR self.deliveryAddress.id = ?1",addressId).fetchOne() != null)
				return true;
		}
		return false;
	}
	
	
}
