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
package com.axelor.apps.production.exceptions;

/**
 * Interface of Exceptions. Enum all exception of axelor-organisation.
 *
 * @author dubaux
 *
 */
public interface IExceptionMessage {



	/**
	 * Production order service
	 */

	static final String PRODUCTION_ORDER_SEQ = /*$$(*/ "Aucune séquence configurée pour les Ordres de production" /*)*/;


	/**
	 * Production order sale order service
	 */

	static final String PRODUCTION_ORDER_SALES_ORDER_NO_BOM = /*$$(*/ "Aucune nomenclature définie pour le produit %s (%s)" /*)*/;



	/**
	 * Manuf order service
	 */

	static final String MANUF_ORDER_SEQ = /*$$(*/ "Aucune séquence configurée pour les Ordres de fabrication" /*)*/;

	/**
	 * Bill of Material Service
	 */
	static final String BOM_1 = /*$$(*/ "Personalized" /*)*/;

	/**
	 * Production Order Wizard and controller
	 */
	static final String PRODUCTION_ORDER_1 = /*$$(*/ "Ordre de production créé" /*)*/;
	static final String PRODUCTION_ORDER_2 = /*$$(*/ "Erreur lors de la création de l'ordre de production" /*)*/;
	static final String PRODUCTION_ORDER_3 = /*$$(*/ "Veuillez entrer une quantité positive" /*)*/;
	static final String PRODUCTION_ORDER_4 = /*$$(*/ "Veuillez sélectionner une nomenclature" /*)*/;

	/**
	 * Production Config Service
	 */
	static final String PRODUCTION_CONFIG_1 = /*$$(*/ "Veuillez configurer la production pour la société %s" /*)*/;
	static final String PRODUCTION_CONFIG_2 = /*$$(*/ "Veuillez configurer un Emplacement Virtuel Production pour la société %s" /*)*/;

	/**
	 * Manuf Order Controller
	 */
	static final String MANUF_ORDER_1 = /*$$(*/ "Please select the Manufacturing order(s) to print." /*)*/;

	/**
	 * Operation Order Controller
	 */
	static final String OPERATION_ORDER_1 = /*$$(*/ "Please select the Operation order(s) to print." /*)*/;

	/**
	 * Sale Order Line Controller
	 */
	static final String SALE_ORDER_LINE_1 = /*$$(*/ "Nomenclature personnalisé créée" /*)*/;

	/**
	 * Production Order Controller
	 */
	static final String PRODUCTION_ORDER_NO_GENERATION = /*$$(*/ "No production order has been generated" /*)*/;
	
	/**
	 * ProdProcess service
	 */
	static final String PROD_PROCESS_USELESS_PRODUCT = /*$$(*/ "The product %s is not in the bill of material related to this production process" /*)*/;
	static final String PROD_PROCESS_MISS_PRODUCT = /*$$(*/ "Not enough quantity in products to consume for: %s" /*)*/;
	
	static final String CHARGE_MACHINE_DAYS = /*$$(*/ "Too many days" /*)*/;
}

