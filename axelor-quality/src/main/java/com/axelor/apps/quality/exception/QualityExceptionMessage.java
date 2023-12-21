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
package com.axelor.apps.quality.exception;

public final class QualityExceptionMessage {

  private QualityExceptionMessage() {}

  public static final String QUALITY_IMPROVEMENT_SEQUENCE_ERROR = /*$$(*/
      "The company %s doesn't have any configured sequence for Quality improvement" /*)*/;

  public static final String QI_RESOLUTION_DECISION_INVALID_SUM_OF_QUANTITIES = /*$$(*/
      "Sum of quantities of the decisions for a default must not be greater than the default's quantity" /*)*/;

  public static final String DEFAULT_QI_STATUS_NOT_FOUND = /*$$(*/
      "No status have been found for the QA, please register at least one in the configurations" /*)*/;

  public static final String EXPECTED_INT_RESULT_FORMULA = /*$$(*/
          "Excepted result for formula is either 1 (not controlled), 2 (compliant) or 3 (non compliant). Current result = %s" /*)*/;

  public static final String CAN_NOT_FETCH_FORMULA = /*$$(*/
          "Conformity formula can not be fetched" /*)*/;
}
