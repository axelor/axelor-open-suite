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
package com.axelor.apps.cash.management.exception;

public interface IExceptionMessage {

  static final String FORECAST_COMPANY = /*$$(*/ "Please select a company" /*)*/;
  static final String FORCAST_RECAP_SEQUENCE_ERROR = /*$$(*/
      "The company %s doesn't have any configured sequence for ForcastRecap" /*)*/;
  static final String FORECAST_RECAP_MISSING_FORECAST_RECAP_LINE_TYPE = /*$$(*/
      "No move type found for element : %s" /*)*/;
  static final String FORECAST_SEQUENCE_ERROR = /*$$(*/
      "The company %s doesn't have any configured sequence for Forecast" /*)*/;
  static final String UNSUPPORTED_LINE_TYPE_FORECAST_RECAP_LINE_TYPE = /*$$(*/
      "Value %s is not supported for forecast recap line type." /*)*/;
}
