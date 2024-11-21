/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2024 Axelor (<http://axelor.com>).
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
package com.axelor.apps.marketing.exception;

public final class MarketingExceptionMessage {

  private MarketingExceptionMessage() {}

  public static final String EMPTY_TARGET = /*$$(*/ "Please select target" /*)*/;

  public static final String EMAIL_ERROR1 = /*$$(*/
      "Error in sending an email to the following targets" /*)*/;

  public static final String EMAIL_ERROR2 = /*$$(*/
      "Error in sending emails. Please check the log file generated." /*)*/;

  public static final String EMAIL_SUCCESS = /*$$(*/ "Emails sent successfully" /*)*/;

  public static final String REMINDER_EMAIL1 = /*$$(*/
      "Please add atleast one invited Partner or Lead." /*)*/;

  public static final String CAMPAIGN_PARTNER_FILTER = /*$$(*/
      "Cannot generate targets. Please check partner filter of Target Model." /*)*/;

  public static final String CAMPAIGN_LEAD_FILTER = /*$$(*/
      "Cannot generate targets. Please check lead filter of Target Model." /*)*/;
}
