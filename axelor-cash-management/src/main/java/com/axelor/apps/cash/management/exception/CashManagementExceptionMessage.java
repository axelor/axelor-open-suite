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
package com.axelor.apps.cash.management.exception;

public final class CashManagementExceptionMessage {

  private CashManagementExceptionMessage() {}

  public static final String FORECAST_COMPANY = /*$$(*/ "Please select a company" /*)*/;
  public static final String FORCAST_RECAP_SEQUENCE_ERROR = /*$$(*/
      "The company %s doesn't have any configured sequence for ForcastRecap" /*)*/;
  public static final String FORECAST_RECAP_MISSING_FORECAST_RECAP_LINE_TYPE = /*$$(*/
      "No move type found for element : %s" /*)*/;
  public static final String FORECAST_SEQUENCE_ERROR = /*$$(*/
      "The company %s doesn't have any configured sequence for Forecast" /*)*/;
  public static final String UNSUPPORTED_LINE_TYPE_FORECAST_RECAP_LINE_TYPE = /*$$(*/
      "Value %s is not supported for forecast recap line type." /*)*/;
}
