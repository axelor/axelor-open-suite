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
package com.axelor.apps.accountorganisation.exceptions;

/**
 * Interface of Exceptions. Enum all exception of axelor-organisation.
 * 
 * @author dubaux
 * 
 */
public interface IExceptionMessage {

	/**
	 * Expense Line Service
	 */
	
	
	/**
	 * Project Invoice Service
	 */
	
	
	/**
	 * Task Invoice Service
	 */
	static final String TASK_INVOICE_1 = "Veuillez configurer un employé pour l'utilisateur %s";
	static final String TASK_INVOICE_2 = "Veuillez configurer un produit à facturer pour la tache";
	static final String TASK_INVOICE_3 = "Veuillez configurer une quantité à facturer pour la tache";
	static final String TASK_INVOICE_4 = "Veuillez configurer un prix unitaire à facturer pour la tache";
	static final String TASK_INVOICE_5 = "Veuillez configurer un montant à facturer pour la tache";
	static final String TASK_INVOICE_6 = "Le montant à facturer ne correspond pas à la quantité et au prix unitaire à facturer pour la tache";
	static final String TASK_INVOICE_7 = "Veuillez configurer un produit profil pour l'employé %s";
	
	
	
	/**
	 * Task Sales Order Service
	 */
	static final String TASK_SALES_ORDER_1 = "La facturation par tâche impose que l'ensemble des lignes de devis comporte des produits de service à produire";
	
	
}
