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
package com.axelor.apps.base.exceptions;

/**
 * Interface of Exceptions. Enum all exception of axelor-organisation.
 * 
 * @author dubaux
 * 
 */
public interface IExceptionMessage {

	static final public String NOT_IMPLEMENTED_METHOD = /*$$(*/ "Not implemented yet!" /*)*/;
	
	/**
	 * Currency service and controller
	 */
	
	static final String CURRENCY_1 = /*$$(*/ "Aucune conversion trouvée de la devise '%s' à la devise '%s' à la date du %s" /*)*/ ;
	static final String CURRENCY_2 = /*$$(*/ "Le taux de conversion de la devise '%s' à la devise '%s' à la date du %s doit être différent de zéro" /*)*/ ;
	static final String CURRENCY_3 = /*$$(*/ "ATTENTION : Veuillez clôturer la période actuelle de conversion pour en créer une nouvelle." /*)*/ ;
	static final String CURRENCY_4 = /*$$(*/ "La date de fin doit impérativement être égale ou supérieur à la date de début." /*)*/ ;
	static final String CURRENCY_5 = /*$$(*/ "Both currencies must be saved before currency rate apply" /*)*/ ;
	static final String CURRENCY_6 = /*$$(*/ "Currency conversion webservice not working" /*)*/ ;
	
	

	/**
	 * Unit conversion service
	 */
	
	static final String UNIT_CONVERSION_1 = /*$$(*/ "Veuillez configurer les conversions d'unités de '%s' à '%s'." /*)*/ ;
	static final String UNIT_CONVERSION_2 = /*$$(*/ "Veuillez configurer les conversions d'unités." /*)*/ ;
	
	static final String CURRENCY_CONVERSION_1 = /*$$(*/ "WARNING : Please close the current conversion period before creating new one" /*)*/ ;
	static final String CURRENCY_CONVERSION_2 = /*$$(*/ "WARNING : To Date must be after or equals to From Date" /*)*/ ;
	
	
	/**
	 * Account management service
	 */
	
	static final public  String ACCOUNT_MANAGEMENT_1 = /*$$(*/ "Tax configuration is missing for Product: %s (company: %s)" /*)*/ ;
	static final public  String ACCOUNT_MANAGEMENT_2 = /*$$(*/ "Aucune taxe trouvée pour le produit %s" /*)*/ ;
	
	/**
	 * Period service
	 */
	
	static final public String PERIOD_1 = /*$$(*/ "Aucune période trouvée ou celle-ci clôturée pour la société: %s" /*)*/ ;
	
	/**
	 * Abstract batch
	 */
	
	static final public String ABSTRACT_BATCH_1 =  /*$$(*/ "This batch is not runnable !" /*)*/;
	
	/**
	 * Indicator generator grouping service
	 */
	static final public String INDICATOR_GENERATOR_GROUPING_1 = /*$$(*/ "\nErreur : Aucun chemin d'export de paramétré" /*)*/;
	static final public String INDICATOR_GENERATOR_GROUPING_2 = /*$$(*/ "\nErreur : Aucun code de paramétré" /*)*/;
	static final public String INDICATOR_GENERATOR_GROUPING_3 = /*$$(*/ "Erreur lors de l'écriture du fichier" /*)*/;
	static final public String INDICATOR_GENERATOR_GROUPING_4 = /*$$(*/ "Resultat exporté" /*)*/;
	/**
	 * Indicator generator service 
	 */
	static final public String INDICATOR_GENERATOR_1 = /*$$(*/ "Erreur : Aucun requête de paramêtrée pour le générateur d'indicateur %s" /*)*/;
	static final public String INDICATOR_GENERATOR_2 = /*$$(*/ "Erreur : Requête incorrect pour le générateur d'indicateur %s" /*)*/;
	static final public String INDICATOR_GENERATOR_3 = /*$$(*/ "Requête exécutée" /*)*/;
	
	
	/**
	 * Alarm engine batch service
	 */
	static final public String ALARM_ENGINE_BATCH_1 = /*$$(*/ "Moteur d'alarme %s" /*)*/;
	static final public String ALARM_ENGINE_BATCH_2 = /*$$(*/ "Compte rendu de la relève des alarmes :\n" /*)*/;
	static final public String ALARM_ENGINE_BATCH_3 = /*$$(*/ "\t* %s objet(s) en alarme(s)\n" /*)*/;
	static final public String ALARM_ENGINE_BATCH_4 = /*$$(*/ "\t* %s anomalie(s)" /*)*/;
	static final public String ALARM_ENGINE_BATCH_5 = /*$$(*/ "Batch d'alarme" /*)*/;

	/**
	 * Base batch service
	 */
	static final public String BASE_BATCH_1 = /*$$(*/ "Action %s inconnu pour le traitement %s" /*)*/;
	static final public String BASE_BATCH_2 = /*$$(*/ "Batch %s inconnu" /*)*/;
	
	/**
	 * Importer
	 */
	static final public String IMPORTER_1 = /*$$(*/ "%s :\n Erreur : Fichier de mapping inacessible." /*)*/;
	
	/**
	 * Importer Listener 
	 */
	static final public String IMPORTER_LISTERNER_1 = /*$$(*/ "\nTotal : " /*)*/;
	static final public String IMPORTER_LISTERNER_2 = /*$$(*/ " - Réussi : " /*)*/;
	static final public String IMPORTER_LISTERNER_3 = /*$$(*/ "\nAnomalies générées : " /*)*/;
	static final public String IMPORTER_LISTERNER_4 = /*$$(*/ "La ligne ne peut être importée (import : %s)" /*)*/;
	
	/**
	 * Template message service base impl
	 */
	static final public String TEMPLATE_MESSAGE_BASE_1 = /*$$(*/ "%s : Le chemin vers le template Birt est incorrect" /*)*/;
	static final public String TEMPLATE_MESSAGE_BASE_2 = /*$$(*/ "Erreur lors de l'édition du fichier : \n %s" /*)*/;
	
	/**
	 * Querie Service and controller
	 */
	static final public String QUERIE_1 = /*$$(*/ "Error : There is no query set for the querie %s" /*)*/;
	static final public String QUERIE_2 = /*$$(*/ "Error : Incorrect query for the querie %s" /*)*/;
	static final public String QUERIE_3 = /*$$(*/ "Valid query." /*)*/;
	
	/**
	 * Scheduler service
	 */
	static final public String SCHEDULER_1 = /*$$(*/ "Veuillez saisir une périodicité pour le planificateur %s" /*)*/;
	
	/**
	 * Tax service
	 */
	static final public String TAX_1 = /*$$(*/ "Veuillez configurer une version de taxe pour la taxe %s" /*)*/;
	
	/**
	 * Template rule service
	 */
	static final public String TEMPLATE_RULE_1 = /*$$(*/ "Bean is not an instance of " /*)*/;
	
	/**
	 * Address controller
	 */
	static final public String ADDRESS_1 = /*$$(*/ "OK" /*)*/;
	static final public String ADDRESS_2 = /*$$(*/ "Service indisponible, veuillez contacter votre adminstrateur" /*)*/;
	static final public String ADDRESS_3 = /*$$(*/ "Aucune addresse correspondante dans la base QAS" /*)*/;
	static final public String ADDRESS_4 = /*$$(*/ "NA" /*)*/;
	static final public String ADDRESS_5 = /*$$(*/ "<B>%s</B> not found" /*)*/;
	static final public String ADDRESS_6 = /*$$(*/ "Feature currently not available with Open Street Maps." /*)*/;
	static final public String ADDRESS_7 = /*$$(*/ "Current user's partner delivery address not set" /*)*/;
	
	/**
	 * Bank details controller
	 */
	static final public String BANK_DETAILS_1 = /*$$(*/ "L'IBAN saisi est invalide. <br> Soit l'IBAN ne respecte pas la norme, soit le format de saisie n'est pas correct. L'IBAN doit être saisi sans espaces tel que présenté ci-dessous: <br> FR0000000000000000000000000" /*)*/;
	
	/**
	 * General controller
	 */
	static final public String GENERAL_1 = /*$$(*/ "No duplicate records found" /*)*/;
	static final public String GENERAL_2 = /*$$(*/ "Duplicate records" /*)*/;
	static final public String GENERAL_3 = /*$$(*/ "Please select key fields to check duplicate" /*)*/;
	static final public String GENERAL_4 = /*$$(*/ "Attachment directory OR Application source does not exist" /*)*/;
	static final public String GENERAL_5 = /*$$(*/ "Export Object" /*)*/;
	
	/**
	 * Messsage controller 
	 */
	static final public String MESSAGE_1 = /*$$(*/ "Error in print. Please check report configuration and print setting." /*)*/;
	static final public String MESSAGE_2 = /*$$(*/ "Please select the Message(s) to print." /*)*/;
	
	/**
	 * Partner controller
	 */
	static final public String PARTNER_1 = /*$$(*/ "Aucune séquence configurée pour les tiers" /*)*/;
	
	/**
	 * Product controller
	 */
	static final public String PRODUCT_1 = /*$$(*/ "Variants generated" /*)*/;
	static final public String PRODUCT_2 = /*$$(*/ "Prices updated" /*)*/;
	static final public String PRODUCT_3 = /*$$(*/ "Product Catalog" /*)*/;
	static final public String PRODUCT_4 = /*$$(*/ "Product" /*)*/;
}
