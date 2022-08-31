package com.axelor.apps.contract.exception;

public final class ContractExceptionMessage {

  private ContractExceptionMessage() {}

  public static final String CONTRACT_MISSING_TERMINATE_DATE = /*$$(*/
      "Please enter a terminated date for this version." /*)*/;
  public static final String CONTRACT_MISSING_ENGAGEMENT_DATE = /*$$(*/
      "Please enter a engagement date." /*)*/;
  public static final String CONTRACT_ENGAGEMENT_DURATION_NOT_RESPECTED = /*$$(*/
      "Engagement duration is not fulfilled." /*)*/;
  public static final String CONTRACT_PRIOR_DURATION_NOT_RESPECTED = /*$$(*/
      "Prior notice duration is not respected." /*)*/;
  public static final String CONTRACT_UNVALIDE_TERMINATE_DATE = /*$$(*/
      "You cannot terminate a contract before version activation date." /*)*/;
  public static final String CONTRACT_CANT_REMOVE_INVOICED_LINE = /*$$(*/
      "You cannot remove a line which has been already invoiced." /*)*/;
  public static final String CONTRACT_EMPTY_PRODUCT = /*$$(*/ "The product can't be empty." /*)*/;
  public static final String CONTRACT_MISSING_FROM_VERSION = /*$$(*/
      "There is no contract associated with this version." /*)*/;
  public static final String CONTRACT_MISSING_FIRST_PERIOD = /*$$(*/
      "Please fill the first period end date and the invoice frequency." /*)*/;
  public static final String CONTRACT_VERSION_EMPTY_NEXT_CONTRACT = /*$$(*/
      "The next contract field is not set on the current contract version." /*)*/;
  public static final String CONTRACT_WAITING_WRONG_STATUS = /*$$(*/
      "Can only put on hold drafted contract." /*)*/;
  public static final String CONTRACT_ONGOING_WRONG_STATUS = /*$$(*/
      "Can only activate waiting contract." /*)*/;
  public static final String CONTRACT_TERMINATE_WRONG_STATUS = /*$$(*/
      "Can only terminate ongoing contract." /*)*/;
}
