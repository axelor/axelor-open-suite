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
package com.axelor.apps.accountorganisation.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.account.db.TaxLine;
import com.axelor.apps.base.service.tax.AccountManagementService;
import com.axelor.apps.organisation.db.Expense;
import com.axelor.apps.organisation.db.ExpenseLine;
import com.axelor.apps.organisation.db.repo.ExpenseLineRepository;
import com.axelor.exception.AxelorException;
import com.google.inject.Inject;


public class ExpenseLineService extends ExpenseLineRepository{

	private static final Logger LOG = LoggerFactory.getLogger(ExpenseLineService.class);
	
	
	@Inject
	private AccountManagementService accountManagementService;
	
	
	public TaxLine getTaxLine(Expense expense, ExpenseLine expenseLine) throws AxelorException  {
		
		return accountManagementService.getTaxLine(
				expenseLine.getDate(), expenseLine.getProduct(), expense.getCompany(), null, true);
		
	}
	
}
