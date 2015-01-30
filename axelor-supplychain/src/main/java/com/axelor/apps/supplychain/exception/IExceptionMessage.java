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
	static final String SO_INVOICE_1 = /*$$(*/ "Il est nécessaire de définir un plannificateur." /*)*/;
	static final String SO_INVOICE_2 = /*$$(*/ "Il est nécessaire de définir une date de début d'abonnement." /*)*/;
	static final String SO_INVOICE_3 = /*$$(*/ "Il est nécessaire de définir une date de première facturation." /*)*/;
	static final String SO_INVOICE_4 = /*$$(*/ "Le devis %s sera facturé le %s." /*)*/;
	static final String SO_INVOICE_5 = /*$$(*/ "Le devis est déjà complêtement facturé" /*)*/;
	static final String SO_INVOICE_6 = /*$$(*/ "Veuillez selectionner une devise pour le devis %s" /*)*/;
	
	/**
	 * Sale Order Purchase Service
	 */
	static final String SO_PURCHASE_1 = /*$$(*/ "Veuillez choisir un fournisseur pour la ligne %s" /*)*/;
	
	/**
	 * Stock Move Invoice Service
	 */
	static final String STOCK_MOVE_INVOICE_1 = /*$$(*/ "Produit incorrect dans le mouvement de stock %s" /*)*/;
	
	/**
	 * Batch Invoicing
	 */
	static final String BATCH_INVOICING_1 = /*$$(*/ "Compte rendu de génération de facture d'abonnement :\n" /*)*/;
	static final String BATCH_INVOICING_2 = /*$$(*/ "Devis(s) traité(s)" /*)*/;
	
	
}
