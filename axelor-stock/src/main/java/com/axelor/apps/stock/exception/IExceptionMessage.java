/**
 * 
 */
package com.axelor.apps.stock.exception;

/**
 * @author axelor
 *
 */
public interface IExceptionMessage {
	
	/**
	 * Inventory service and controller
	 */
	static final String INVENTORY_1 = /*$$(*/ "Veuillez selectionner un entrepot" /*)*/;
	static final String INVENTORY_2 = /*$$(*/ "Aucune séquence configurée pour les inventaires pour la société" /*)*/;
	static final String INVENTORY_3 = /*$$(*/ "An error occurred while importing the file data. Please contact your application administrator to check Traceback logs." /*)*/;
	static final String INVENTORY_4 = /*$$(*/ "An error occurred while importing the file data, product not found with code :" /*)*/;
	static final String INVENTORY_5 = /*$$(*/ "There is currently no such file in the specified folder or the folder may not exists." /*)*/;
	static final String INVENTORY_6 = /*$$(*/ "Société manquante pour l'entrepot %s" /*)*/;
	static final String INVENTORY_7 = /*$$(*/ "Produit incorrect dans la ligne de l'inventaire" /*)*/;
	static final String INVENTORY_8 = /*$$(*/ "File %s successfully imported." /*)*/;
	static final String INVENTORY_9 = /*$$(*/ "Il n'y a aucun produit contenu dans l'emplacement de stock." /*)*/;
	static final String INVENTORY_10 = /*$$(*/ "La liste des lignes d'inventaire a été rempli." /*)*/;
	static final String INVENTORY_11 = /*$$(*/ "Aucune lignes d'inventaire n'a été créée." /*)*/;
	
	/**
	 * Location Line Service Impl
	 */
	static final String LOCATION_LINE_1 = /*$$(*/ "Les stocks du produit %s (%s) sont insuffisants pour réaliser la livraison" /*)*/;
	static final String LOCATION_LINE_2 = /*$$(*/ "Les stocks du produit %s (%s), numéro de suivi {}  sont insuffisants pour réaliser la livraison" /*)*/;
	
	/**
	 * Stock Move Service and Controller
	 */
	static final String STOCK_MOVE_1 = /*$$(*/ "Aucune séquence configurée pour les mouvements internes de stock pour la société %s" /*)*/;
	static final String STOCK_MOVE_2 = /*$$(*/ "Aucune séquence configurée pour les receptions de stock pour la société %s" /*)*/;
	static final String STOCK_MOVE_3 = /*$$(*/ "Aucune séquence configurée pour les livraisons de stock pour la société %s" /*)*/;
	static final String STOCK_MOVE_4 = /*$$(*/ "Type de mouvement de stock non déterminé" /*)*/;
	static final String STOCK_MOVE_5 = /*$$(*/ "Aucun emplacement source selectionné pour le mouvement de stock %s" /*)*/;
	static final String STOCK_MOVE_6 = /*$$(*/ "Aucun emplacement destination selectionné pour le mouvement de stock %s" /*)*/;
	static final String STOCK_MOVE_7 = /*$$(*/ "Partial stock move (From" /*)*/;
	static final String STOCK_MOVE_8 = /*$$(*/ "Reverse stock move (From" /*)*/;
	static final String STOCK_MOVE_9 = /*$$(*/ "A partial stock move has been generated (%s)" /*)*/;
	static final String STOCK_MOVE_10 = /*$$(*/ "Please select the StockMove(s) to print." /*)*/;
	static final String STOCK_MOVE_11 = /*$$(*/ "Company address is empty." /*)*/;
	static final String STOCK_MOVE_12 = /*$$(*/ "Feature currently not available with Open Street Maps." /*)*/;
	static final String STOCK_MOVE_13 = /*$$(*/ "<B>%s or %s</B> not found" /*)*/;
	static final String STOCK_MOVE_14 = /*$$(*/ "No move lines to split" /*)*/;
	static final String STOCK_MOVE_15 = /*$$(*/ "Please select lines to split" /*)*/;
	static final String STOCK_MOVE_16 = /*$$(*/ "Please entry proper split qty" /*)*/;
	
	/**
	 * Tracking Number Service
	 */
	static final String TRACKING_NUMBER_1 = /*$$(*/ "Aucune séquence configurée pour les Numéros de suivi pour le produit %s:%s" /*)*/;
	
	/**
	 * Stock Config Service
	 */
	static final String STOCK_CONFIG_1 = /*$$(*/ "Veuillez configurer le module stock pour la société %s" /*)*/;
	static final String STOCK_CONFIG_2 = /*$$(*/ "Veuillez configurer un Emplacement Virtuel Inventaire pour la société %s" /*)*/;
	static final String STOCK_CONFIG_3 = /*$$(*/ "Veuillez configurer un Emplacement Virtuel Fournisseur pour la société %s" /*)*/;
	static final String STOCK_CONFIG_4 = /*$$(*/ "Veuillez configurer un Emplacement Virtuel Client pour la société %s" /*)*/;
	
	/**
	 * Location Controller
	 */
	static final String LOCATION_1 = /*$$(*/ "Il existe déjà un entrepot par défaut, veuillez d'abord désactiver l'entrepot" /*)*/;
	static final String LOCATION_2 = /*$$(*/ "Please select the Stock Location(s) to print." /*)*/;
	
	
}