/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2020 Axelor (<http://axelor.com>).
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
package com.axelor.apps.docusign.exceptions;

public interface IExceptionMessage {

  public static final String DOCUSIGN_ACCOUNT_EMPTY = /*$$(*/ "The DocuSign account is empty" /*)*/;
  public static final String DOCUSIGN_EMAIL_ADDRESS_EMPTY = /*$$(*/
      "The email address is empty for the partner" /*)*/;
  public static final String DOCUSIGN_PARAM_ITEM_UNKNOWN_TYPE = /*$$(*/
      "Param item unknown type" /*)*/;
  public static final String DOCUSIGN_ENVELOPE_ID_NULL = /*$$(*/
      "DocuSign Service return any envelope id" /*)*/;
  public static final String DOCUSIGN_IN_PERSON_SIGNER_NOT_FOUND = /*$$(*/
      "The in person signer is not found" /*)*/;
  public static final String DOCUSIGN_SIGNER_NOT_FOUND = /*$$(*/ "The signer is not found" /*)*/;
  public static final String DOCUSIGN_ENVELOPE_SETTING_EMPTY = /*$$(*/
      "The envelope setting is empty" /*)*/;
}
