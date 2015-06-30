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
package com.axelor.apps.hr.exception;

/**
 * Interface of Exceptions. Enum all exception of axelor-account.
 *
 * @author dubaux
 *
 */
public interface IExceptionMessage {

	static final String TIMESHEET_FROM_DATE = /*$$(*/ "Merci de rentrer une date de début pour la génération"/*)*/ ;
	static final String TIMESHEET_TO_DATE = /*$$(*/ "Merci de rentrer une date de fin pour la génération"/*)*/ ;
	static final String TIMESHEET_PRODUCT = /*$$(*/ "Merci de rentrer un produit"/*)*/ ;
	static final String TIMESHEET_EMPLOYEE_DAY_PLANNING = /*$$(*/ "Merci de rentrer un planning pour l'employé rattaché à l'utilisateur %s"/*)*/ ;
	static final String TIMESHEET_EMPLOYEE_DAILY_WORK_HOURS = /*$$(*/ "Please, enter the number of daily work hours per employee %s"/*)*/ ;

	static final String LEAVE_USER_EMPLOYEE = /*$$(*/ "Merci de créer un employé pour l'utilisateur %s"/*)*/ ;
	static final String LEAVE_LINE = /*$$(*/ "il n'y a pas de ligne de créée sur l'employé %s pour la raison %s"/*)*/ ;
	static final String LEAVE_ALLOW_NEGATIVE_VALUE_EMPLOYEE = /*$$(*/ "L'employé %s n'est pas autorisé à prendre des congés en avance"/*)*/ ;
	static final String LEAVE_ALLOW_NEGATIVE_VALUE_REASON = /*$$(*/ "Il n'est pas possible de prendre des congés en avance pour le motif %s"/*)*/ ;


}