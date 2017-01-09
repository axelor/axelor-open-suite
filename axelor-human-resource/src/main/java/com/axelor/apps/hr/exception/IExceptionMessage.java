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
package com.axelor.apps.hr.exception;

/**
 * Interface of Exceptions. Enum all exception of axelor-human-resource.
 *
 */
public interface IExceptionMessage {

	static final String HR_CONFIG = /*$$(*/ "Please configure informations for human resources for the company %s" /*)*/;
	static final String HR_CONFIG_EXPENSE_TYPE = /*$$(*/ "Please configure the expense type for kilometric allowance in HR config for the company %s" /*)*/;
	static final String HR_CONFIG_SENT_EXPENSE_TEMPLATE = /*$$(*/ "Please configure the sent expense template in HR config for the company %s" /*)*/;
	static final String HR_CONFIG_VALIDATED_EXPENSE_TEMPLATE = /*$$(*/ "Please configure the validated expense template in HR config for the company %s" /*)*/;
	static final String HR_CONFIG_REFUSED_EXPENSE_TEMPLATE = /*$$(*/ "Please configure the refused expense template in HR config for the company %s" /*)*/;
	static final String HR_CONFIG_SENT_TIMESHEET_TEMPLATE = /*$$(*/ "Please configure the sent timehsheet template in HR config for the company %s" /*)*/;
	static final String HR_CONFIG_VALIDATED_TIMESHEET_TEMPLATE = /*$$(*/ "Please configure the validated timehsheet template in HR config for the company %s" /*)*/;
	static final String HR_CONFIG_REFUSED_TIMESHEET_TEMPLATE = /*$$(*/ "Please configure the refused timehsheet template in HR config for the company %s" /*)*/;
	static final String HR_CONFIG_SENT_LEAVE_TEMPLATE = /*$$(*/ "Please configure the sent leave template in HR config for the company %s" /*)*/;
	static final String HR_CONFIG_VALIDATED_LEAVE_TEMPLATE = /*$$(*/ "Please configure the validated leave template in HR config for the company %s" /*)*/;
	static final String HR_CONFIG_REFUSED_LEAVE_TEMPLATE = /*$$(*/ "Please configure the refused leave template in HR config for the company %s" /*)*/;
	static final String HR_CONFIG_SENT_EXTRA_HOURS_TEMPLATE = /*$$(*/ "Please configure the sent extra hours template in HR config for the company %s" /*)*/;
	static final String HR_CONFIG_VALIDATED_EXTRA_HOURS_TEMPLATE = /*$$(*/ "Please configure the validated extra hours template in HR config for the company %s" /*)*/;
	static final String HR_CONFIG_REFUSED_EXTRA_HOURS_TEMPLATE = /*$$(*/ "Please configure the refused extra hours template in HR config for the company %s" /*)*/;
	static final String HR_CONFIG_LEAVE_REASON = /*$$(*/ "Please configure the leave reason in HR config for the company %s" /*)*/;
	static final String HR_CONFIG_LUNCH_VOUCHER_EXPORT_PATH = /*$$(*/ "Please configure the lunch voucher export path in HR config for the company %s" /*)*/;
	
	static final String TIMESHEET_FROM_DATE = /*$$(*/ "Merci de rentrer une date de début pour la génération"/*)*/ ;
	static final String TIMESHEET_TO_DATE = /*$$(*/ "Merci de rentrer une date de fin pour la génération"/*)*/ ;
	static final String TIMESHEET_PRODUCT = /*$$(*/ "Merci de rentrer un produit"/*)*/ ;
	static final String TIMESHEET_EMPLOYEE_DAY_PLANNING = /*$$(*/ "Merci de rentrer un planning pour l'employé rattaché à l'utilisateur %s"/*)*/ ;
	static final String TIMESHEET_EMPLOYEE_DAILY_WORK_HOURS = /*$$(*/ "Please, enter the number of daily work hours per employee %s"/*)*/ ;
	static final String TIMESHEET_DATE_CONFLICT = /*$$(*/ "There is a conflict between the dates entered and the dates in the lines : %s"/*)*/ ;
	static final String TIMESHEET_NULL_FROM_DATE = /*$$(*/ "From date can't be empty"/*)*/ ;
	static final String GENERAL_EMPLOYEE_ACTIVITY = /*$$(*/ "Please, enter an activity for the employee %s"/*)*/ ;

	static final String LEAVE_USER_EMPLOYEE = /*$$(*/ "Please create an employee for the user %s"/*)*/ ;
	static final String LEAVE_LINE = /*$$(*/ "There is no leave line for the employee %s and the reason %s."/*)*/ ;
	static final String LEAVE_ALLOW_NEGATIVE_VALUE_EMPLOYEE = /*$$(*/ "Employee %s is not allowed to take leave in advance."/*)*/ ;
	static final String LEAVE_ALLOW_NEGATIVE_VALUE_REASON = /*$$(*/ "You are not able to take leave in advance for the reason '%s'.\n Reminder : %s"/*)*/ ;
	static final String LEAVE_ALLOW_NEGATIVE_ALERT = /*$$(*/ "You now have a negative amount of leave available for the reason %s"/*)*/ ;

	static final String EMPLOYEE_PLANNING = /*$$(*/ "Please, add a planning for employee : %s"/*)*/ ;
	static final String EMPLOYEE_PUBLIC_HOLIDAY = /*$$(*/ "Please, add a public holiday planning for employee : %s"/*)*/ ;
	
	static final String BATCH_MISSING_FIELD = /*$$(*/ "Leave reason and day number have to be defined"/*)*/ ;
	static final String EMPLOYEE_DOUBLE_LEAVE_MANAGEMENT = /*$$(*/ "The employee %s has multiple %s leave lines"/*)*/ ;
	static final String EMPLOYEE_NO_LEAVE_MANAGEMENT = /*$$(*/ "The employee %s has no %s leave line"/*)*/ ;
	static final String EMPLOYEE_NO_SENIORITY_DATE = /*$$(*/ "The employee %s has no seniority date"/*)*/ ;
	static final String EMPLOYEE_NO_BIRTH_DATE = /*$$(*/ "The employee %s has no birth date"/*)*/ ;
	
	
	static final String BATCH_LEAVE_MANAGEMENT_ENDING_0 = /*$$(*/ "Employees' leaves attempted to be computed : %s"/*)*/ ;
	static final String BATCH_LEAVE_MANAGEMENT_ENDING_1 = /*$$(*/ "Employees' leaves successfully computed : %s"/*)*/ ;
	static final String BATCH_LEAVE_MANAGEMENT_ENDING_2 = /*$$(*/ "Employees' leaves failed to be computed due to configuration anomaly : %s"/*)*/ ;
	static final String BATCH_LEAVE_MANAGEMENT_ENDING_3 = /*$$(*/ "Employees' leaves failed to be computed due to missing data : %s"/*)*/ ;
	static final String BATCH_SENIORITY_LEAVE_MANAGEMENT_FORMULA = /*$$(*/ "There is an error in a formula"/*)*/ ;
	
	static final String LUNCH_VOUCHER_MIN_STOCK = /*$$(*/ "Minimum stock of lunch vouchers will be reached for the company %s.\n  Minimum Stock allowed : %s.\n Available Stock : %s" /*)*/ ;
	
	static final String KILOMETRIC_LOG_NO_YEAR = /*$$(*/ "There is no year for society %s which includes date %s"/*)*/ ;
	
	static final String KILOMETRIC_ALLOWANCE_NO_RULE = /*$$(*/ "There is no matching condition for the allowance %s"/*)*/ ;

}