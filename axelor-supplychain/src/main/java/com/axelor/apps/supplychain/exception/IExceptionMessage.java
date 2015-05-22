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

	static final String PO_INVOICE_1 = /*$$(*/ "Veuillez selectionner une devise pour la commande %s" /*)*/;
	static final String PO_INVOICE_2 = /*$$(*/ "Facture créée" /*)*/;

	/**
	 * Purchase Order Service
	 */
	static final String PURCHASE_ORDER_1 = /*$$(*/ "%s Veuillez configurer un entrepot virtuel fournisseur pour la société %s" /*)*/;

	/**
	 * Sale Order Invoice Service
	 */
	static final String SO_INVOICE_1 = /*$$(*/ "Il est nécessaire de définir un planificateur." /*)*/;
	static final String SO_INVOICE_2 = /*$$(*/ "Il est nécessaire de définir une date de début d'abonnement." /*)*/;
	static final String SO_INVOICE_3 = /*$$(*/ "Il est nécessaire de définir une date de première facturation." /*)*/;
	static final String SO_INVOICE_4 = /*$$(*/ "Le devis %s sera facturé le %s." /*)*/;
	static final String SO_INVOICE_5 = /*$$(*/ "Le devis est déjà complêtement facturé" /*)*/;
	static final String SO_INVOICE_6 = /*$$(*/ "Veuillez selectionner une devise pour le devis %s" /*)*/;

	/**
	 * Sale Order Purchase Service
	 */
	static final String SO_PURCHASE_1 = /*$$(*/ "Veuillez choisir un fournisseur pour la ligne %s" /*)*/;
	static final String SO_LINE_PURCHASE_AT_LEAST_ONE = /*$$(*/ "At least one sale order line must be selected" /*)*/;

	/**
	 * Stock Move Invoice Service
	 */
	static final String STOCK_MOVE_INVOICE_1 = /*$$(*/ "Produit incorrect dans le mouvement de stock %s" /*)*/;
	static final String STOCK_MOVE_MULTI_INVOICE_CURRENCY = /*$$(*/ "The currency is required and must be the same for all sale orders" /*)*/;
	static final String STOCK_MOVE_MULTI_INVOICE_CLIENT_PARTNER = /*$$(*/ "The client Partner is required and must be the same for all sale orders" /*)*/;
	static final String STOCK_MOVE_MULTI_INVOICE_SUPPLIER_PARTNER = /*$$(*/ "The supplier Partner is required and must be the same for all sale orders" /*)*/;
	static final String STOCK_MOVE_MULTI_INVOICE_COMPANY = /*$$(*/ "The company is required and must be the same for all sale orders" /*)*/;
	static final String STOCK_MOVE_NO_INVOICE_GENERATED = /*$$(*/ "No invoice was generated" /*)*/;
	static final String STOCK_MOVE_GENERATE_INVOICE = /*$$(*/ "The invoice for the stock move %s can't be generated because of this following error : %s" /*)*/;
	static final String OUTGOING_STOCK_MOVE_INVOICE_EXISTS = /*$$(*/ "An invoice not canceled already exists for the outgoing stock move %s" /*)*/;
	static final String INCOMING_STOCK_MOVE_INVOICE_EXISTS = /*$$(*/ "An invoice not canceled already exists for the incoming stock move %s" /*)*/;

	/**
	 * Batch Invoicing
	 */
	static final String BATCH_INVOICING_1 = /*$$(*/ "Compte rendu de génération de facture d'abonnement :\n" /*)*/;
	static final String BATCH_INVOICING_2 = /*$$(*/ "Devis(s) traité(s)" /*)*/;


}
