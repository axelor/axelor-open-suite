/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2022 Axelor (<http://axelor.com>).
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
package com.axelor.apps.marketing.exception;

public interface IExceptionMessage {

  static final String EMPTY_TARGET = /*$$(*/ "Please select target" /*)*/;

  static final String EMAIL_ERROR1 = /*$$(*/
      "Error in sending an email to the following targets" /*)*/;

  static final String EMAIL_ERROR2 = /*$$(*/
      "Error in sending emails. Please check the log file generated." /*)*/;

  static final String EMAIL_SUCCESS = /*$$(*/ "Emails sent successfully" /*)*/;

  static final String REMINDER_EMAIL1 = /*$$(*/
      "Please add atleast one invited Partner or Lead." /*)*/;

  static final String CAMPAIGN_PARTNER_FILTER = /*$$(*/
      "Cannot generate targets. Please check partner filter of Target Model." /*)*/;

  static final String CAMPAIGN_LEAD_FILTER = /*$$(*/
      "Cannot generate targets. Please check lead filter of Target Model." /*)*/;
}
