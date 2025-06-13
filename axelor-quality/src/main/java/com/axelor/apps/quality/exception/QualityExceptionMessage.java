/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2025 Axelor (<http://axelor.com>).
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

  public static final String EXPECTED_BOOLEAN_RESULT_FORMULA = /*$$(*/
      "Excepted result for formula is either true (compliant) or false (not compliant). Current result = %s" /*)*/;

  public static final String EVAL_FORMULA_NULL_FIELDS = /*$$(*/
      "Evaluation of the conformity formula failed, please make sure that every required fields for the evaluation are filled." /*)*/;

  public static final String CAN_NOT_FETCH_FORMULA = /*$$(*/
      "Conformity formula can not be fetched" /*)*/;

  public static final String QUALITY_CONFIG = /*$$(*/
      "Please configure information for quality for the company %s" /*)*/;

  public static final String QI_ACTION_DISTRIBUTION_SEQUENCE_NOT_SET = /*$$(*/
      "Please configure a sequence for action distribution." /*)*/;

  public static final String QI_DECISION_DISTRIBUTION_SEQUENCE_NOT_FOUND = /*$$(*/
      "Please configure a sequence for decision distribution." /*)*/;

  public static final String API_NO_CHARACTERISTIC_OR_SAMPLE_ID = /*$$(*/
      "Please provide one of the two fields, characteristicId or sampleId, in request body." /*)*/;

  public static final String API_CHARACTERISTIC_NOT_IN_CONTROL_ENTRY = /*$$(*/
      "Characteristic or sample does not belong to this control entry." /*)*/;

  public static final String API_SUPPLIER_NOT_PROVIDED = /*$$(*/
      "Detection origin is supplier but no supplier is provided." /*)*/;

  public static final String API_CUSTOMER_NOT_PROVIDED = /*$$(*/
      "Detection origin is customer but no customer is provided." /*)*/;

  public static final String API_DATA_DOES_NOT_MATCH_DETECTION_ORIGIN = /*$$(*/
      "Provided data does not match with detection origin %s." /*)*/;

  public static final String API_DETECTION_DEFAULT_LINE_NOT_IN_QI = /*$$(*/
      "The resolution default line with id %s doesn't belong to this quality improvement." /*)*/;

  public static final String QUALITY_IMPROVEMENT_UPDATED = /*$$(*/
      "Quality improvement successfully updated." /*)*/;

  public static final String API_TYPE_SYSTEM_PRODUCT_MANUF_ORDER_FILLED = /*$$(*/
      "Product/ManufOrder fields should not be filled if type is 'System'." /*)*/;

  public static final String API_QI_CREATION_MESSAGE = /*$$(*/
      "The QualityImprovement has been correctly created with the id : %s." /*)*/;

  public static final String API_QI_RESOLUTION_DEFAULT_ERROR = /*$$(*/
      "Some defaults have been skipped as they were incompatibles. (%s line(s))" /*)*/;
}
