package com.axelor.apps.marketing.exception;

public final class MarketingExceptionMessage {

  private MarketingExceptionMessage() {}

  public static final String EMPTY_TARGET = /*$$(*/ "Please select target" /*)*/;

  public static final String EMAIL_ERROR1 = /*$$(*/
      "Error in sending an email to the following targets" /*)*/;

  public static final String EMAIL_ERROR2 = /*$$(*/
      "Error in sending emails. Please check the log file generated." /*)*/;

  public static final String EMAIL_SUCCESS = /*$$(*/ "Emails sent successfully" /*)*/;

  public static final String REMINDER_EMAIL1 = /*$$(*/
      "Please add atleast one invited Partner or Lead." /*)*/;

  public static final String CAMPAIGN_PARTNER_FILTER = /*$$(*/
      "Cannot generate targets. Please check partner filter of Target Model." /*)*/;

  public static final String CAMPAIGN_LEAD_FILTER = /*$$(*/
      "Cannot generate targets. Please check lead filter of Target Model." /*)*/;
}
