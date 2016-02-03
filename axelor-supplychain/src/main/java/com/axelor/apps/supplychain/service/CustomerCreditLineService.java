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
package com.axelor.apps.supplychain.service;

import java.util.Map;

import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.supplychain.db.CustomerCreditLine;
import com.axelor.exception.AxelorException;
import com.google.inject.persist.Transactional;


public interface CustomerCreditLineService {
	
	public CustomerCreditLine computeUsedCredit(CustomerCreditLine customerCreditLine);
	public Partner generateLines(Partner partner) throws AxelorException;
	public void updateLines(Partner partner) throws AxelorException;
	public Map<String,Object> updateLinesFromOrder(Partner partner,SaleOrder saleOrder) throws AxelorException;
	public boolean testUsedCredit(CustomerCreditLine customerCreditLine);
	
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public boolean checkBlockedPartner(Partner partner, Company company) throws AxelorException;
}
