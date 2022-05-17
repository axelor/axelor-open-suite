package com.axelor.apps.message.exception;

public final class MessageExceptionMessage {
  private MessageExceptionMessage() {}

  /** Mail account service and controller */
  public static final String MAIL_ACCOUNT_1 = /*$$(*/ "Incorrect login or password" /*)*/;

  public static final String MAIL_ACCOUNT_2 = /*$$(*/
      "Unable to reach server. Please check Host,Port and SSL/TLS" /*)*/;
  public static final String MAIL_ACCOUNT_3 = /*$$(*/ "Connection successful" /*)*/;
  public static final String MAIL_ACCOUNT_4 = /*$$(*/
      "Provided settings are wrong, please modify them and try again" /*)*/;
  public static final String MAIL_ACCOUNT_5 = /*$$(*/ "There is already a default account" /*)*/;
  public static final String MAIL_ACCOUNT_6 = /*$$(*/ "There is no default email account" /*)*/;

  /** Template service */
  public static final String TEMPLATE_SERVICE_1 = /*$$(*/
      "Model empty. Please configure a model." /*)*/;

  public static final String TEMPLATE_SERVICE_2 = /*$$(*/
      "Your target receptor is not valid. Please check it." /*)*/;
  public static final String TEMPLATE_SERVICE_3 = /*$$(*/ "Waiting model: %s" /*)*/;

  /** General message controller */
  public static final String MESSAGE_1 = /*$$(*/ "Please configure a template" /*)*/;

  public static final String MESSAGE_2 = /*$$(*/ "Select template" /*)*/;
  public static final String MESSAGE_3 = /*$$(*/ "Create message" /*)*/;
  public static final String MESSAGE_4 = /*$$(*/ "Email sending is in progress" /*)*/;
  public static final String MESSAGE_5 = /*$$(*/ "Sender's email address is null or empty" /*)*/;
  public static final String MESSAGE_6 = /*$$(*/
      "TO/CC/BCC recipient's email address is empty" /*)*/;
  public static final String MESSAGE_MISSING_SELECTED_MESSAGES = /*$$(*/
      "Please select one or more messages." /*)*/;
  public static final String MESSAGES_SEND_IN_PROGRESS = /*$$(*/
      "Email sending is in progress for %d messages." /*)*/;
  public static final String MESSAGES_REGENERATED = /*$$(*/
      "%d messages has been regenerated successfully and %d errors append." /*)*/;
  public static final String SET_EMAIL_TEMPLATE_MESSAGE = /*$$(*/
      "Please set the email template to send" /*)*/;
  public static final String INVALID_MODEL_TEMPLATE_EMAIL = /*$$(*/
      "The email template model (%s) is different than the entity generating the email (%s)" /*)*/;

  public static final String SEND_EMAIL_EXCEPTION = /*$$(*/ "Error when sending email: %s" /*)*/;

  public static final String SMS_ERROR_MISSING_MOBILE_NUMBER = /*$$(*/
      "Please fill mobile phone number." /*)*/;

  public static final String TEMPORARY_EMAIL_MEDIA_TYPE_ERROR = /*$$(*/
      "Temporary email service only support Email media type." /*)*/;
}
