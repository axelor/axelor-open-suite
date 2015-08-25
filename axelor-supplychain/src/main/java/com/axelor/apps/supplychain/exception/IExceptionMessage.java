/**
 *
 */
package com.axelor.apps.supplychain.exception;

/**
 * @author axelor
 *
 */
public interface IExceptionMessage {
	/**
	 * Purchase Order Invoice Service and controller
	 */

	static final String PO_INVOICE_1 = /*$$(*/ "Please, select a currency for the order %s" /*)*/;
	static final String PO_INVOICE_2 = /*$$(*/ "Invoice created" /*)*/;

	/**
	 * Purchase Order Service
	 */
	static final String PURCHASE_ORDER_1 = /*$$(*/ "%s please configure a virtual supplier stock location for the company %s" /*)*/;

	/**
	 * Sale Order Invoice Service
	 */
	static final String SO_INVOICE_6 = /*$$(*/ "Please, select a currency for the order %s" /*)*/;

	/**
	 * Sale Order Purchase Service
	 */
	static final String SO_PURCHASE_1 = /*$$(*/ "Please, select a supplier for the line %s" /*)*/;
	static final String SO_LINE_PURCHASE_AT_LEAST_ONE = /*$$(*/ "At least one sale order line must be selected" /*)*/;

	/**
	 * Stock Move Invoice Service
	 */
	static final String STOCK_MOVE_INVOICE_1 = /*$$(*/ "Incorrect product in the stock move %s" /*)*/;
	static final String STOCK_MOVE_MULTI_INVOICE_CURRENCY = /*$$(*/ "The currency is required and must be the same for all sale orders" /*)*/;
	static final String STOCK_MOVE_MULTI_INVOICE_CLIENT_PARTNER = /*$$(*/ "The client Partner is required and must be the same for all sale orders" /*)*/;
	static final String STOCK_MOVE_MULTI_INVOICE_SUPPLIER_PARTNER = /*$$(*/ "The supplier Partner is required and must be the same for all purchase orders" /*)*/;
	static final String STOCK_MOVE_MULTI_INVOICE_COMPANY_SO = /*$$(*/ "The company is required and must be the same for all sale orders" /*)*/;
	static final String STOCK_MOVE_MULTI_INVOICE_COMPANY_PO = /*$$(*/ "The company is required and must be the same for all purchase orders" /*)*/;
	static final String STOCK_MOVE_MULTI_INVOICE_IN_ATI = /*$$(*/ "Unit prices in A.T.I and in W.T. can't be mix" /*)*/;
	static final String STOCK_MOVE_NO_INVOICE_GENERATED = /*$$(*/ "No invoice was generated" /*)*/;
	static final String STOCK_MOVE_GENERATE_INVOICE = /*$$(*/ "The invoice for the stock move %s can't be generated because of this following error : %s" /*)*/;
	static final String OUTGOING_STOCK_MOVE_INVOICE_EXISTS = /*$$(*/ "An invoice not canceled already exists for the outgoing stock move %s" /*)*/;
	static final String INCOMING_STOCK_MOVE_INVOICE_EXISTS = /*$$(*/ "An invoice not canceled already exists for the incoming stock move %s" /*)*/;

	/**
	 * Batch Invoicing
	 */
	static final String BATCH_INVOICING_1 = /*$$(*/ "Compte rendu de génération de facture d'abonnement :\n" /*)*/;
	static final String BATCH_INVOICING_2 = /*$$(*/ "Order(s) processed" /*)*/;


}
