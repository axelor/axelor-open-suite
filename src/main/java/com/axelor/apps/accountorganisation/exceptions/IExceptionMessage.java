/**
 * Copyright (c) 2012-2014 Axelor. All Rights Reserved.
 *
 * The contents of this file are subject to the Common Public
 * Attribution License Version 1.0 (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * http://license.axelor.com/.
 *
 * The License is based on the Mozilla Public License Version 1.1 but
 * Sections 14 and 15 have been added to cover use of software over a
 * computer network and provide for limited attribution for the
 * Original Developer. In addition, Exhibit A has been modified to be
 * consistent with Exhibit B.
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See
 * the License for the specific language governing rights and limitations
 * under the License.
 *
 * The Original Code is part of "Axelor Business Suite", developed by
 * Axelor exclusively.
 *
 * The Original Developer is the Initial Developer. The Initial Developer of
 * the Original Code is Axelor.
 *
 * All portions of the code written by Axelor are
 * Copyright (c) 2012-2014 Axelor. All Rights Reserved.
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
	static final String TASK_INVOICE_7 = "Veuillez configurer un produit profil pour l'employé %s %s";
	
	
	
	/**
	 * Task Sales Order Service
	 */
	static final String TASK_SALES_ORDER_1 = "La facturation par tâche impose que l'ensemble des lignes de devis comporte des produits de service à produire";
	
	
}
