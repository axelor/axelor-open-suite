/**
 * Axelor Business Solutions
 *
 * Copyright (C) 2012-2014 Axelor (<http://axelor.com>).
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
package com.axelor.apps.account.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.base.service.AddressServiceImpl;


public class AddressServiceAccountImpl extends AddressServiceImpl  {
	
	private static final Logger LOG = LoggerFactory.getLogger(AddressServiceAccountImpl.class);
	
	@Override
	public boolean checkAddressUsed(Long addressId){
		
		super.checkAddressUsed(addressId);

		if(addressId != null){
			
			if(Invoice.all_().filter("self.address.id = ?1",addressId).fetchOne() != null)
				return true;
		}
		return false;
	}
	
	
}
