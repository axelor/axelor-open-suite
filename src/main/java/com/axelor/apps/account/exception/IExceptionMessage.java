/**
 * Axelor Business Solutions
 *
 * Copyright (C) 2014 Axelor (<http://axelor.com>).
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
package com.axelor.apps.account.exception;

/**
 * Interface of Exceptions. Enum all exception of axelor-account.
 * 
 * @author dubaux
 * 
 */
public interface IExceptionMessage {

	
	
	/**
	 * Bank statement service
	 */
	
	static final String BANK_STATEMENT_1 = /*$$(*/ "%s :\n Computed balance and Ending Balance must be equal" /*)*/ ; 
	static final String BANK_STATEMENT_2 = /*$$(*/ "%s :\n MoveLine amount is not equals with bank statement line %s" /*)*/ ;
	static final String BANK_STATEMENT_3 = /*$$(*/ "%s :\n Bank statement line %s amount can't be null" /*)*/ ;
	
	/**
	 * Move service
	 */

	public final String NO_MOVES_SELECTED = /*$$(*/ "Please select 'Draft' or 'Simulated' moves" /*)*/ ; 
	public final String MOVE_VALIDATION_NOT_OK = /*$$(*/ "Error in move validation, please check the log" /*)*/ ;
	public final String MOVE_VALIDATION_OK = /*$$(*/ "Moves validated successfully" /*)*/;
	
	
	/**
	 * Account management service
	 */
	public  String ACCOUNT_MANAGEMENT_1_ACCOUNT = /*$$(*/ "Accounting configuration is missing for Product: %s (company: %s)" /*)*/ ;
	
}
