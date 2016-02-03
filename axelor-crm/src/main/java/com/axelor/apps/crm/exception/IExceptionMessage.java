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

	/**
	 * Configuration
	 */
	static final String CRM_CONFIG_1 = /*$$(*/ "Please configure informations for CRM for company %s" /*)*/;
	static final String CRM_CONFIG_TEMPLATES = /*$$(*/ "Please configure all templates in CRM configuration for company %s" /*)*/;
	static final String CRM_CONFIG_TEMPLATES_NONE = /*$$(*/ "No template created in CRM configuration for company %s, emails have not been sent" /*)*/;
	
	
	/**
	 * Calendar
	 */
	static final String CALENDAR_NOT_VALID = /*$$(*/ "Calendar configuration not valid" /*)*/;
	
	/*
	 * Recurrence
	 */
	static final String RECURRENCE_RECURRENCE_TYPE = /*$$(*/ "You must choose a recurrence type" /*)*/;
	static final String RECURRENCE_PERIODICITY = /*$$(*/ "Periodicity must be greater than 0" /*)*/;
	static final String RECURRENCE_DAYS_CHECKED = /*$$(*/ "You must choose at least one day in the week" /*)*/;
	static final String RECURRENCE_REPETITION_NUMBER = /*$$(*/ "The number of repetitions must be greater than 0" /*)*/;
	static final String RECURRENCE_END_DATE = /*$$(*/ "The end date must be after the start date" /*)*/;
}
