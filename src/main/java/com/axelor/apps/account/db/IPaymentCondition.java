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
package com.axelor.apps.account.db;

/**
 * Interface of PaymentCondition object. Enum all static variable of packages.
 */
public interface IPaymentCondition {


	// TYPE SELECT
	static final int TYPE_NET = 1;
	static final int TYPE_END_OF_MONTH_N_DAYS = 2;
	static final int TYPE_N_DAYS_END_OF_MONTH = 3;
	static final int TYPE_N_DAYS_END_OF_MONTH_AT = 4;

	

}
