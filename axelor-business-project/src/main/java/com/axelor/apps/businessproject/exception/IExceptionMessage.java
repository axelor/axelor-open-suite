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
package com.axelor.apps.businessproject.exception;

/**
 * Interface of Exceptions. Enum all exception of axelor-account.
 *
 * @author dubaux
 *
 */
public interface IExceptionMessage {

	static final String FOLDER_TEMPLATE = /*$$(*/ "Merci de rentrer un modèle de devis"/*)*/ ;
	static final String INVOICING_PROJECT_EMPTY = /*$$(*/ "Vous n'avez sélectionné aucun élément à facturer"/*)*/ ;
	static final String INVOICING_PROJECT_USER = /*$$(*/ "Le projet/tâche sélectionné(e) ne contient pas de responsable"/*)*/ ;
	static final String INVOICING_PROJECT_PROJECT_TASK = /*$$(*/ "Veuillez sélectionner un(e) projet/tâche"/*)*/ ;
	static final String INVOICING_PROJECT_PROJECT_TASK_PRODUCT = /*$$(*/ "Vous n'avez pas sélectionné de produit de facturation pour la tâche %s"/*)*/ ;
	static final String INVOICING_PROJECT_PROJECT_TASK_COMPANY = /*$$(*/ "Vous n'avez pas sélectionné de compagnie sur le projet racine"/*)*/ ;
	static final String SALE_ORDER_NO_PROJECT = /*$$(*/ "No Project selected"/*)*/ ;
	static final String SALE_ORDER_NO_LINES = /*$$(*/ "No Line can be used for tasks"/*)*/ ;
}