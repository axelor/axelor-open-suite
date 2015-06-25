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
package com.axelor.apps.project.exception;

/**
 * Interface of Exceptions. Enum all exception of axelor-account.
 *
 * @author dubaux
 *
 */
public interface IExceptionMessage {

	static final String PROJECT_PLANNING_NO_TASK = /*$$(*/ "Vous n'êtes sur aucun projet ou aucune tâche, votre planning ne sera pas généré."/*)*/ ;
	static final String PROJECT_PLANNING_NO_TASK_TEAM = /*$$(*/ "Votre équipe n'est sur aucun projet ou aucune tâche, votre planning ne sera pas généré."/*)*/ ;

}