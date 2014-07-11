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
 * Interface of Payment Mode model.
 */
public interface IPaymentMode {

	// TYPE
	static final int TYPE_OTHER = 1;
	static final int TYPE_DD = 2;
	static final int TYPE_IPO = 3;
	static final int TYPE_IPO_CHEQUE = 4;
	static final int TYPE_CASH = 5;
	static final int TYPE_BANK_CARD = 6;
	static final int TYPE_CHEQUE = 7;
	static final int TYPE_WEB = 8;
	static final int TYPE_TRANSFER = 9;

	// Sales or purchase
	static final int SALES = 1;
	static final int PURCHASES = 2;

}
