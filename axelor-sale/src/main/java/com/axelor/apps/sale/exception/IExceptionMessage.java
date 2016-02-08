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
package com.axelor.apps.sale.exception;

/**
 * Interface of Exceptions. Enum all exception of axelor-organisation.
 * 
 * @author dubaux
 * 
 */
public interface IExceptionMessage {

	/**
	 * Sales Order Stock Move Service
	 */
	static final String SALES_ORDER_STOCK_MOVE_1 = /*$$(*/ "La facturation par expédition impose que l'ensemble des lignes de devis comporte des produits de service ou stockable avec un approvisionnement depuis le stock" /*)*/ ;
	
	/**
	 * Sales Order Service Impl
	 */
	static final String SALES_ORDER_1 = /*$$(*/ "La société %s n'a pas de séquence de configurée pour les devis clients" /*)*/ ;
	
	/**
	 * Sale Config Service
	 */
	static final String SALE_CONFIG_1 = /*$$(*/ "%s :\n Veuillez configurer le module vente pour la société %s" /*)*/ ;
}
