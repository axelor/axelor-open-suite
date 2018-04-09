/*
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
package com.axelor.apps.production.exceptions;

/**
 * Interface of Exceptions.
 *
 * @author dubaux
 *
 */
public interface IExceptionMessage {



	/**
	 * Production order service
	 */

	static final String PRODUCTION_ORDER_SEQ = /*$$(*/ "There's no configured sequence for production's orders" /*)*/;


	/**
	 * Production order sale order service
	 */

	static final String PRODUCTION_ORDER_SALES_ORDER_NO_BOM = /*$$(*/ "There no's defined nomenclature for product %s (%s)" /*)*/;



	/**
	 * Manuf order service
	 */

	static final String MANUF_ORDER_SEQ = /*$$(*/ "There's no configured sequence for fabrication's orders"/*)*/;
	static final String IN_OR_OUT_INVALID_ARG = /*$$(*/ "inOrOut is invalid"/*)*/;

	/**
	 * Bill of Material Service
	 */
	static final String BOM_1 = /*$$(*/ "Personalized" /*)*/;

	/**
	 * Production Order Wizard and controller
	 */
	static final String PRODUCTION_ORDER_1 = /*$$(*/ "Production's order created" /*)*/;
	static final String PRODUCTION_ORDER_2 = /*$$(*/ "Error during production's order's creation" /*)*/;
	static final String PRODUCTION_ORDER_3 = /*$$(*/ "You must add a positive quantity" /*)*/;
	static final String PRODUCTION_ORDER_4 = /*$$(*/ "You must select a nomenclature" /*)*/;

	/**
	 * Production Config Service
	 */
	static final String PRODUCTION_CONFIG_1 = /*$$(*/ "You must configure a production for company %s" /*)*/;
	static final String PRODUCTION_CONFIG_2 = /*$$(*/ "You must configure a production virtual stock location for company %s" /*)*/;
	static final String PRODUCTION_CONFIG_3 = /*$$(*/ "You must configure a waste stock location for company %s." /*)*/;
	static final String PRODUCTION_CONFIG_4 = /*$$(*/ "You must configure a finished products default stock location for company %s." /*)*/;
	static final String PRODUCTION_CONFIG_5 = /*$$(*/ "You must configure a component default stock location for company %s." /*)*/;

	/**
	 * Manuf Order Controller
	 */
	static final String MANUF_ORDER_1 = /*$$(*/ "Please select the Manufacturing order(s) to print." /*)*/;

	/**
	 * Operation Order Controller
	 */
	static final String OPERATION_ORDER_1 = /*$$(*/ "Please select the Operation order(s) to print." /*)*/;

	/**
	 * Sale order line Controller
	 */
	static final String SALE_ORDER_LINE_1 = /*$$(*/ "Personalized nomenclature created" /*)*/;

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

	/**
	 * Bill of material service
	 */
	static final String COST_TYPE_CANNOT_BE_CHANGED = /*$$(*/ "The product cost cannot be changed because the product cost type is not manual" /*)*/;

	/**
	 * Configurator Controller
	 */
	String BILL_OF_MATERIAL_GENERATED =  /*$$(*/ "The bill of material %s has been generated" /*)*/;

	/**
	 * Configurator Bom Service
	 */
	String CONFIGURATOR_BOM_TOO_MANY_CALLS = /*$$(*/ "Too many recursive calls to create the bill of material." /*)*/;

	/**
	 * Stock move line production controller
	 */
	String STOCK_MOVE_LINE_UNKNOWN_PARENT_CONTEXT = /*$$(*/ "Unknown parent context class." /*)*/;
}

