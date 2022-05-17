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
