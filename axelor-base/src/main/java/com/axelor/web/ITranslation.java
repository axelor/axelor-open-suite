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
package com.axelor.web;

public interface ITranslation {

  public static final String PREFIX = "MapRest.";

  public static final String PIN_CHAR_DEFAULT = /*$$(*/ "MapRest.PinCharDefault" /*)*/;
  public static final String PIN_CHAR_INVOICING = /*$$(*/ "MapRest.PinCharInvoicing" /*)*/;
  public static final String PIN_CHAR_DELIVERY = /*$$(*/ "MapRest.PinCharDelivery" /*)*/;

  public static final String DEFAULT = /*$$(*/ "MapRest.Default" /*)*/;
  public static final String INVOICING = /*$$(*/ "MapRest.Invoicing" /*)*/;
  public static final String DELIVERY = /*$$(*/ "MapRest.Delivery" /*)*/;

  public static final String MAP_ZERO_RESULTS = /*$$(*/ "MapRest.ZERO_RESULTS" /*)*/;
  public static final String CHECK_RESPONSE_RESPONSE = /*$$(*/
      "Here is the list of the warnings and errors" /*)*/;

  public static final String PRICING_OBSERVER_IDENTIFIED_PRICING = /*$$(*/
      "Identified pricing scale: %s"; /*)*/
  public static final String PRICING_OBSERVER_IDENTIFIED_CR = /*$$(*/
      "Classification rule used: %s"; /*)*/
  public static final String PRICING_OBSERVER_RESULT_CR = /*$$(*/
      "Result of the classification rule evaluation: %s"; /*)*/
  public static final String PRICING_OBSERVER_IDENTIFIED_RR = /*$$(*/
      "Evaluation of result rule: %s"; /*)*/
  public static final String PRICING_OBSERVER_RESULT_RR = /*$$(*/
      "Result of the evaluation of the result rule: %s"; /*)*/
  public static final String PRICING_OBSERVER_POPULATED_FIELD = /*$$(*/ "Populated field: %s"; /*)*/
  public static final String PRICING_OBSERVER_POPULATED_CUSTOM_FIELD = /*$$(*/
      "Populated custom field: %s"; /*)*/

  public static final String PRICING_OBSERVER_NO_PRICING = /*$$(*/
      "No pricing scale used for this product"; /*)*/
}
