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
package com.axelor.apps.cash.management.exception;


public interface IExceptionMessage {

	static final String FORECAST_COMPANY = /*$$(*/ "Please select a company" /*)*/;

	static final String BATCH_CREDIT_TRANSFER_EXPENSES_REPORT_TITLE = /*$$(*/ "Report for credit transfers on expenses: " /*)*/;
	static final String BATCH_CREDIT_TRANSFER_EXPENSES_DONE_SINGULAR = /*$$(*/ "%d expense treated successfully, " /*)*/;
	static final String BATCH_CREDIT_TRANSFER_EXPENSES_DONE_PLURAL = /*$$(*/ "%d expenses treated successfully, " /*)*/;
	static final String BATCH_CREDIT_TRANSFER_EXPENSES_ANOMALY_SINGULAR = /*$$(*/ "%d anomaly." /*)*/;
	static final String BATCH_CREDIT_TRANSFER_EXPENSES_ANOMALY_PLURAL = /*$$(*/ "%d anomalies." /*)*/;

}
