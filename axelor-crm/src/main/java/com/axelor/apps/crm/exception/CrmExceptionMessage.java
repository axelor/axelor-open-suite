/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2023 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.axelor.apps.crm.exception;

public final class CrmExceptionMessage {

  private CrmExceptionMessage() {}

  /** Target service */
  public static final String TARGET_1 = /*$$(*/
      "Objective %s is in contradiction with objective's configuration %s" /*)*/;

  /** Batch event reminder */
  public static final String BATCH_EVENT_REMINDER_1 = /*$$(*/ "Event reminder %s" /*)*/;

  public static final String BATCH_EVENT_REMINDER_2 = /*$$(*/
      "Event's reminder's generation's reporting :" /*)*/;
  public static final String BATCH_EVENT_REMINDER_3 = /*$$(*/ "Reminder(s) treated" /*)*/;

  /** Batch event reminder message */
  public static final String BATCH_EVENT_REMINDER_MESSAGE_1 = /*$$(*/ "Reminder(s) treated" /*)*/;

  /** Batch target */
  public static final String BATCH_TARGET_1 = /*$$(*/ "Event reminder %s" /*)*/;

  public static final String BATCH_TARGET_2 = /*$$(*/ "Objectives' generation's reporting :" /*)*/;
  public static final String BATCH_TARGET_3 = /*$$(*/ "Treated objectives reporting" /*)*/;

  /** Convert lead wizard controller */
  public static final String CONVERT_LEAD_1 = /*$$(*/ "Lead converted" /*)*/;

  public static final String CONVERT_LEAD_MISSING = /*$$(*/ "Parent lead is missing." /*)*/;
  public static final String CONVERT_LEAD_ERROR = /*$$(*/ "Error in lead conversion" /*)*/;

  public static final String LEAD_PARTNER_MISSING_ADDRESS = /*$$(*/
      "Please complete the partner address." /*)*/;
  public static final String LEAD_CONTACT_MISSING_ADDRESS = /*$$(*/
      "Please complete the contact address." /*)*/;

  /** Event controller */
  public static final String EVENT_1 = /*$$(*/ "Input location please" /*)*/;

  public static final String EVENT_SAVED = /*$$(*/
      "Please save the event before setting the recurrence" /*)*/;

  public static final String LEAD_START_WRONG_STATUS = /*$$(*/
      "Can only start new or assigned lead." /*)*/;
  public static final String LEAD_RECYCLE_WRONG_STATUS = /*$$(*/
      "Can only recycle a lost lead." /*)*/;
  public static final String LEAD_CONVERT_WRONG_STATUS = /*$$(*/
      "Can only convert new, assigned or in process lead." /*)*/;
  public static final String LEAD_ASSIGN_TO_ME_WRONG_STATUS = /*$$(*/
      "Can only assign to yourself new, assigned or in process lead." /*)*/;
  public static final String LEAD_LOSE_WRONG_STATUS = /*$$(*/
      "Can not mark as lost an already lost lead." /*)*/;
  public static final String LEAD_CONVERT_KANBAN = /*$$(*/
      "Can only convert leads in form view." /*)*/;
  public static final String LEAD_LOSE_KANBAN = /*$$(*/ "Can only lose leads in form view." /*)*/;

  /** Opportunity */
  public static final String LEAD_PARTNER = /*$$(*/ "Please select a lead" /*)*/;

  public static final String OPPORTUNITY_CLOSE_LOST_KANBAN = /*$$(*/
      "Can only close lost opportunities in form view." /*)*/;

  /** Partner */
  public static final String PROSPECT_CLOSE_WIN_KANBAN = /*$$(*/
      "Prospect won and became client." /*)*/;

  public static final String PROSPECT_CLOSE_LOST_KANBAN = /*$$(*/
      "Can only close lost prospects in form view." /*)*/;

  /** Configuration */
  public static final String CRM_CONFIG_1 = /*$$(*/
      "Please configure information for CRM for company %s" /*)*/;

  public static final String CRM_CONFIG_USER_EMAIL = /*$$(*/
      "User %s does not have an email address configured nor is it linked to a partner with an email address configured." /*)*/;
  public static final String CRM_CONFIG_USER_COMPANY = /*$$(*/
      "User %s must have an active company to use templates" /*)*/;
  public static final String CRM_CONFIG_TEMPLATES = /*$$(*/
      "Please configure all templates in CRM configuration for company %s" /*)*/;
  public static final String CRM_CONFIG_TEMPLATES_NONE = /*$$(*/
      "No template created in CRM configuration for company %s, emails have not been sent" /*)*/;
  public static final String CRM_CLOSED_WIN_OPPORTUNITY_STATUS_MISSING = /*$$(*/
      "Please fill closed win opportunity status in App CRM configuration." /*)*/;
  public static final String CRM_CLOSED_LOST_OPPORTUNITY_STATUS_MISSING = /*$$(*/
      "Please fill closed lost opportunity status in App CRM configuration." /*)*/;
  public static final String CRM_CONVERTED_LEAD_STATUS_MISSING = /*$$(*/
      "Please fill converted lead status in App CRM configuration." /*)*/;
  public static final String CRM_LOST_LEAD_STATUS_MISSING = /*$$(*/
      "Please fill lost lead status in App CRM configuration." /*)*/;

  /*
   * Recurrence
   */
  public static final String RECURRENCE_RECURRENCE_TYPE = /*$$(*/
      "You must choose a recurrence type" /*)*/;
  public static final String RECURRENCE_PERIODICITY = /*$$(*/
      "Periodicity must be greater than 0" /*)*/;
  public static final String RECURRENCE_DAYS_CHECKED = /*$$(*/
      "You must choose at least one day in the week" /*)*/;
  public static final String RECURRENCE_REPETITION_NUMBER = /*$$(*/
      "The number of repetitions must be greater than 0" /*)*/;
  public static final String RECURRENCE_END_DATE = /*$$(*/
      "The end date must be after the start date" /*)*/;
  public static final String OPPORTUNITY_1 = /*$$(*/
      "There's no configured sequence for opportunities for the company %s" /*)*/;

  public static final String EVENT_USER_NO_ACTIVE_COMPANY = /*$$(*/
      "Please set an active company for user %s" /*)*/;

  /** CRM Reporting */
  public static final String CRM_REPORTING_TYPE_SELECT_MISSING = /*$$(*/
      "Crm Reporting type is missing!" /*)*/;

  public static final String CRM_SALES_PROPOSITION_STATUS_MISSING = /*$$(*/
      "Please fill sales proposition status in App CRM configuration." /*)*/;

  public static final String CRM_CLOSED_LOST_PARTNER_STATUS_MISSING = /*$$(*/
      "Please fill closed lost partner status in App CRM configuration." /*)*/;

  public static final String CRM_CLOSED_WIN_PARTNER_STATUS_MISSING = /*$$(*/
      "Please fill closed win partner status in App CRM configuration." /*)*/;

  public static final String PARTNER_LOSE_WRONG_STATUS = /*$$(*/
      "Can not mark as lost an already lost partner." /*)*/;

  public static final String CRM_DEFAULT_LEAD_STATUS_MISSING = /*$$(*/
      "Please fill default lead status in App CRM configuration." /*)*/;

  public static final String CRM_DEFAULT_OPPORTUNITY_STATUS_MISSING = /*$$(*/
      "Please fill default opportunity status in App CRM configuration." /*)*/;

  public static final String CRM_DEFAULT_PARTNER_STATUS_MISSING = /*$$(*/
      "Please fill default partner status in App CRM configuration." /*)*/;

  public static final String CRM_LEAD_NOT_FOUND = /*$$(*/ "Lead not found." /*)*/;

  public static final String CRM_PROSPECT_NOT_FOUND = /*$$(*/ "Prospect not found." /*)*/;

  public static final String CRM_EMAIL_DOMAIN_ALREADY_EXISTS = /*$$(*/
      "We found the following similar leads/contacts/partners in the system, do you still want to create this lead ?" /*)*/;
}
