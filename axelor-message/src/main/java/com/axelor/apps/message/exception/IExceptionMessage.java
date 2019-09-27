/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2019 Axelor (<http://axelor.com>).
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
/** */
package com.axelor.apps.message.exception;

/** @author axelor */
public interface IExceptionMessage {

  /** Mail account service and controller */
  static final String MAIL_ACCOUNT_1 = /*$$(*/ "Incorrect login or password" /*)*/;

  static final String MAIL_ACCOUNT_2 = /*$$(*/
      "Unable to reach server. Please check Host,Port and SSL/TLS" /*)*/;
  static final String MAIL_ACCOUNT_3 = /*$$(*/ "Connection successful" /*)*/;
  static final String MAIL_ACCOUNT_4 = /*$$(*/
      "Provided settings are wrong, please modify them and try again" /*)*/;
  static final String MAIL_ACCOUNT_5 = /*$$(*/ "There is already a default account" /*)*/;

  /** Template service */
  static final String TEMPLATE_SERVICE_1 = /*$$(*/ "Model empty. Please configure a model." /*)*/;

  static final String TEMPLATE_SERVICE_2 = /*$$(*/
      "Your target receptor is not valid. Please check it." /*)*/;
  static final String TEMPLATE_SERVICE_3 = /*$$(*/ "Waiting model: %s" /*)*/;

  /** General message controller */
  static final String MESSAGE_1 = /*$$(*/ "Please configure a template" /*)*/;

  static final String MESSAGE_2 = /*$$(*/ "Select template" /*)*/;
  static final String MESSAGE_3 = /*$$(*/ "Create message" /*)*/;
  static final String MESSAGE_4 = /*$$(*/ "Email sent" /*)*/;
  static final String MESSAGE_5 = /*$$(*/ "Message sent" /*)*/;
  static final String MESSAGE_6 = /*$$(*/ "Failed to send Email" /*)*/;
  static final String MESSAGE_7 = /*$$(*/ "Sender's email address is null or empty" /*)*/;
  static final String MESSAGE_8 = /*$$(*/ "TO/CC/BCC recipient's email address is empty" /*)*/;
  static final String MESSAGE_MISSING_SELECTED_MESSAGES = /*$$(*/
      "Please select one or more messages." /*)*/;
  static final String MESSAGES_SENT = /*$$(*/
      "%d messages has been sent successfully and %d errors append." /*)*/;
  static final String MESSAGES_REGENERATED = /*$$(*/
      "%d messages has been regenerated successfully and %d errors append." /*)*/;
}
