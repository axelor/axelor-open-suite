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
package com.axelor.apps.gdpr.exception;

public final class GdprExceptionMessage {

  public static final String FIELD_NOT_FOUND = /*$$(*/ "This field doesn't exist." /*)*/;

  public static final String NO_LINE_SELECTED = /*$$(*/ "Please select a line" /*)*/;

  public static final String TOO_MUCH_LINE_SELECTED = /*$$(*/ "Please select only one line" /*)*/;

  public static final String MISSING_ACCESS_REQUEST_RESPONSE_MAIL_TEMPLATE = /*$$(*/
      "Please configure a mail template for access request response." /*)*/;

  public static final String MISSING_ERASURE_REQUEST_RESPONSE_MAIL_TEMPLATE = /*$$(*/
      "Please configure a mail template for erasure request response." /*)*/;

  public static final String SENDING_MAIL_ERROR = /*$$(*/ "Error while sending the mail : %s" /*)*/;
}
