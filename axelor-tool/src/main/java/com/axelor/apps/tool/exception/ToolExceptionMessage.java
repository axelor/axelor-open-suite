/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2023 Axelor (<http://axelor.com>).
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
package com.axelor.apps.tool.exception;

public final class ToolExceptionMessage {

  private ToolExceptionMessage() {}

  /** Period service */
  public static final String PERIOD_1 = /*$$(*/ "Years in 360 days" /*)*/;

  /** URL service */
  public static final String URL_SERVICE_1 = /*$$(*/
      "Can not opening the connection to a empty URL." /*)*/;

  public static final String URL_SERVICE_2 = /*$$(*/ "Url %s is malformed." /*)*/;
  public static final String URL_SERVICE_3 = /*$$(*/
      "An error occurs while opening the connection. Please verify the following URL : %s." /*)*/;

  /** Template maker */
  public static final String TEMPLATE_MAKER_1 = /*$$(*/ "No such template" /*)*/;

  public static final String TEMPLATE_MAKER_2 = /*$$(*/ "Templating can not be empty" /*)*/;

  public static final String RECORD_UNIQUE_FIELD = /*$$(*/ "This field needs to be unique." /*)*/;

  /** Pdf Tool */
  public static final String BAD_COPY_NUMBER_ARGUMENT = /*$$(*/
      "The parameter copyNumber should be superior to 0." /*)*/;

  // Meta tool
  public static final String ERROR_CONVERT_TYPE_TO_JSON_TYPE = /*$$(*/
      "Type %s could not be converted to json type." /*)*/;

  public static final String ERROR_CONVERT_JSON_TYPE_TO_TYPE = /*$$(*/
      "Meta json field type %s could not be converted to a meta field type." /*)*/;

  // Callable Tool
  public static final String PROCESS_BEING_COMPUTED = /*$$(*/ "Computation in progress..." /*)*/;
}
