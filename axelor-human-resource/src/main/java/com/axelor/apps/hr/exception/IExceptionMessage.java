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


	static final String LEAVE_USER_EMPLOYEE = /*$$(*/ "Merci de créer un employé pour l'utilisateur %s"/*)*/ ;
	static final String LEAVE_LINE = /*$$(*/ "il n'y a pas de ligne de créée sur l'employé %s pour la raison %s"/*)*/ ;
	static final String LEAVE_ALLOW_NEGATIVE_VALUE_EMPLOYEE = /*$$(*/ "L'employé %s n'est pas autorisé à prendre des congés en avance"/*)*/ ;
	static final String LEAVE_ALLOW_NEGATIVE_VALUE_REASON = /*$$(*/ "Il n'est pas possible de prendre des congés en avance pour le motif %s"/*)*/ ;
	
}