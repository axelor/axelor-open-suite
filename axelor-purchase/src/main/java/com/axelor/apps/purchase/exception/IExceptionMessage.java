/**
 *
 */
package com.axelor.apps.purchase.exception;

/**
 * @author axelor
 *
 */
public interface IExceptionMessage {
	
	static final String PURCHASE_ORDER_LINE_TAX_LINE = /*$$(*/ "Il manque une ligne de taxe"/*)*/ ;
	/**
	 * Purchase order service impl
	 */
	static final public String PURCHASE_ORDER_1 = /*$$(*/ "La société %s n'a pas de séquence de configurée pour les commandes fournisseur" /*)*/;

	/**
	 * Purchase config service
	 */
	static final public String PURCHASE_CONFIG_1 = /*$$(*/ "%s :\n Veuillez configurer le module Achat pour la société %s" /*)*/;

	/**
	 * Merge purchase order
	 */

	static final public String PURCHASE_ORDER_MERGE_ERROR_CURRENCY = /*$$(*/ "The currency is required and must be the same for all purchase orders" /*)*/;
	static final public String PURCHASE_ORDER_MERGE_ERROR_SUPPLIER_PARTNER = /*$$(*/ "The supplier Partner is required and must be the same for all purchase orders" /*)*/;
	static final public String PURCHASE_ORDER_MERGE_ERROR_COMPANY = /*$$(*/ "The company is required and must be the same for all purchase orders" /*)*/;
}
