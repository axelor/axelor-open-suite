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
package com.axelor.apps.supplychain.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.base.db.repo.PartnerRepository;
import com.axelor.apps.base.service.AddressServiceImpl;
import com.axelor.apps.sale.db.repo.SaleOrderRepository;
import com.axelor.apps.stock.db.repo.StockMoveRepository;
import com.axelor.inject.Beans;

import java.lang.invoke.MethodHandles;


public class AddressServiceSupplychainImpl extends AddressServiceImpl  {
	
	private static final Logger LOG = LoggerFactory.getLogger( MethodHandles.lookup().lookupClass() );
	
	@Override
	public boolean checkAddressUsed(Long addressId){
		LOG.debug("Address Id to be checked = {}",addressId);
		if(addressId != null){
			if(Beans.get(PartnerRepository.class).all().filter("self.mainInvoicingAddress.id = ?1 OR self.deliveryAddress.id = ?1",addressId).fetchOne() != null)
				return true;
			if(Beans.get(SaleOrderRepository.class).all().filter("self.mainInvoicingAddress.id = ?1 OR self.deliveryAddress.id = ?1",addressId).fetchOne() != null)
				return true;
			if(Beans.get(StockMoveRepository.class).all().filter("self.fromAddress.id = ?1 OR self.toAddress.id = ?1",addressId).fetchOne() != null)
				return true;
		}
		return false;
	}
	
	
}
