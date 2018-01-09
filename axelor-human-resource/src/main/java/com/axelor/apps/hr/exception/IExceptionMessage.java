/**
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
package com.axelor.apps.hr.exception;

/**
 * Interface of Exceptions. Enum all exception of axelor-account.
 *
 * @author dubaux
 *
 */
public interface IExceptionMessage {

	static final String HR_CONFIG_1 = /*$$(*/ "Please configure informations for human resources for the company %s" /*)*/;
	static final String HR_CONFIG_2 = /*$$(*/ "Please configure the expense type for kilometric allowance in HR config for the company %s" /*)*/;
	static final String HR_CONFIG_TEMPLATES = /*$$(*/ "No email sent, please configure HR config for the company %s"/*)*/ ;
	
	static final String TIMESHEET_FROM_DATE = /*$$(*/ "Please add a start date for generation"/*)*/ ;
	static final String TIMESHEET_TO_DATE = /*$$(*/ "Please add an end date for generation"/*)*/ ;
	static final String TIMESHEET_PRODUCT = /*$$(*/ "Please add a product"/*)*/ ;
	static final String TIMESHEET_EMPLOYEE_DAY_PLANNING = /*$$(*/ "Please add an employee's planning related to user %s"/*)*/ ;
	static final String TIMESHEET_EMPLOYEE_DAILY_WORK_HOURS = /*$$(*/ "Please, enter the number of daily work hours per employee %s"/*)*/ ;
	static final String GENERAL_EMPLOYEE_ACTIVITY = /*$$(*/ "Please, enter an activity for the employee %s"/*)*/ ;

	static final String LEAVE_USER_EMPLOYEE = /*$$(*/ "Please create an employee for user %s"/*)*/ ;
	static final String LEAVE_LINE = /*$$(*/ "There's no line created for employee %s for the reason %s"/*)*/ ;
	static final String LEAVE_ALLOW_NEGATIVE_VALUE_EMPLOYEE = /*$$(*/ "Employee %s is not authorized to take leaves in advance"/*)*/ ;
	static final String LEAVE_ALLOW_NEGATIVE_VALUE_REASON = /*$$(*/ "It's not possible to take leaves in advance for the reason %s"/*)*/ ;

	static final String EMPLOYEE_PLANNING = /*$$(*/ "Please, add a planning for employee : %s"/*)*/ ;
	static final String EMPLOYEE_PUBLIC_HOLIDAY = /*$$(*/ "Please, add a public holiday planning for employee : %s"/*)*/ ;
	
	static final String BATCH_MISSING_FIELD = /*$$(*/ "Leave reason and day number have to be defined"/*)*/ ;
	static final String EMPLOYEE_DOUBLE_LEAVE_MANAGEMENT = /*$$(*/ "The employee %s has multiple %s leave lines"/*)*/ ;
	static final String EMPLOYEE_NO_LEAVE_MANAGEMENT = /*$$(*/ "The employee %s has no %s leave line"/*)*/ ;
	
	static final String BATCH_LEAVE_MANAGEMENT_ENDING_0 = /*$$(*/ "Employees' leaves attempted to be computed : %s"/*)*/ ;
	static final String BATCH_LEAVE_MANAGEMENT_ENDING_1 = /*$$(*/ "Employees' leaves successfully computed : %s"/*)*/ ;
	static final String BATCH_LEAVE_MANAGEMENT_ENDING_2 = /*$$(*/ "Employees' leaves failed to be computed due to configuration anomaly : %s"/*)*/ ;
	static final String BATCH_LEAVE_MANAGEMENT_ENDING_3 = /*$$(*/ "Employees' leaves failed to be computed due to missing data : %s"/*)*/ ;
	
	


}