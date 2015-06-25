/**
 * 
 */
package com.axelor.apps.crm.exception;

/**
 * @author axelor
 *
 */
public interface IExceptionMessage {
	
	/**
	 * Target service
	 */
	static final String TARGET_1 = /*$$(*/ "L'objectif %s est en contradiction avec la configuration d'objectif %s" /*)*/;
	
	/**
	 * Batch event reminder 
	 */
	static final String BATCH_EVENT_REMINDER_1 = /*$$(*/ "Event reminder %s" /*)*/;
	static final String BATCH_EVENT_REMINDER_2 = /*$$(*/ "Compte rendu de la génération de rappel des évènements :\n" /*)*/;
	static final String BATCH_EVENT_REMINDER_3 = /*$$(*/ "Rappel(s) traité(s)" /*)*/;
	
	/**
	 * Batch event reminder message
	 */
	static final String BATCH_EVENT_REMINDER_MESSAGE_1 = /*$$(*/ "Rappel(s) traité(s)" /*)*/;
	
	/**
	 * Batch target
	 */
	static final String BATCH_TARGET_1 = /*$$(*/ "Event reminder %s" /*)*/;
	static final String BATCH_TARGET_2 = /*$$(*/ "Compte rendu de la génération des objectifs :\n" /*)*/;
	static final String BATCH_TARGET_3 = /*$$(*/ "Configuration des objectifs(s) traité(s)" /*)*/;
	
	/**
	 * Convert lead wizard controller
	 */
	static final String CONVERT_LEAD_1 = /*$$(*/ "Prospect converti" /*)*/;
	
	/**
	 * Event controller
	 */
	static final String EVENT_1 = /*$$(*/ "Aucune séquence configurée pour les tickets" /*)*/;
	static final String EVENT_2 = /*$$(*/ "Input location please" /*)*/;
	
	/**
	 * Lead controller
	 */
	static final String LEAD_1 = /*$$(*/ "Please select the Lead(s) to print." /*)*/;
	static final String LEAD_2 = /*$$(*/ "Can not open map, Please Configure Application Home First." /*)*/;
	static final String LEAD_3 = /*$$(*/ "Can not open map, Please Check your Internet connection." /*)*/;
	static final String LEAD_4 = /*$$(*/ "No lead import configuration found" /*)*/;
	static final String LEAD_5 = /*$$(*/ "Import lead" /*)*/;
	
	/**
	 * Opportunity
	 */
	static final String LEAD_PARTNER = /*$$(*/ "Veuillez selectionner une piste" /*)*/;

}
