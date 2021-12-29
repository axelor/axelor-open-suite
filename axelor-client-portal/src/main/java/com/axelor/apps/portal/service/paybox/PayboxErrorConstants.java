/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2021 Axelor (<http://axelor.com>).
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
package com.axelor.apps.portal.service.paybox;

/** PayboxErrorConstants. Contains paybox error constants. */
public interface PayboxErrorConstants {
  /** Successful operation */
  public static final String CODE_ERROR_OPERATION_SUCCESSFUL = "00000";

  /** Connection to the authorization center failed. */
  public static final String CODE_ERROR_CONNECTION_FAILED = "00001";

  /** Payment refused by the authorization center */
  public static final String CODE_ERROR_PAYMENT_REFUSED = "001xx";

  /** Paybox error. */
  public static final String CODE_ERROR_PAYBOX = "00003";

  /** Invalid user number or visual cryptogram. */
  public static final String CODE_ERROR_WRONG_USER_NAME_OR_CRYPTOGRAM = "00004";

  /** Access denied or incorrect site / rank / username. */
  public static final String CODE_ERROR_ACCESS_DENIED = "00006";

  /** Incorrect expiry date. */
  public static final String CODE_ERROR_WRONG_EXPIRATION_DATE = "00008";

  /** Error creating a subscription. */
  public static final String CODE_ERROR_SUBSCRIPTION_CREATION = "00009";

  /** Unknown currency. */
  public static final String CODE_ERROR_UNKNOWN_CURRENCY = "00010";

  /** Incorrect amount. */
  public static final String CODE_ERROR_WRONG_AMOUNT = "00011";

  /** Payment already made. */
  public static final String CODE_ERROR_ALREADY_DONE = "00015";

  /**
   * Already existing subscriber (new subscriber registration).'U' value of the PBX_RETOUR variable.
   */
  public static final String CODE_ERROR_ALREADY_REGISTERED = "00016";

  /** Card not authorized. */
  public static final String CODE_ERROR_CARD_NOT_ALLOWED = "00021";

  /** Card not valid. */
  public static final String CODE_ERROR_CARD_INVALID = "00029";

  /** Waiting time > 15 minutes by the internet user/buyer at the payment page. */
  public static final String CODE_ERROR_TIMEOUT = "00030";

  /** Reserved */
  public static final String CODE_ERROR_RESERVED = "00031";

  /** Reserved */
  public static final String CODE_ERROR_RESERVED_2 = "00032";

  /** Country code of the IP address of the buyer's browser unauthorized. */
  public static final String CODE_ERROR_BLOCKED_IP = "00033";

  /** Operation without 3DSecure authentication, blocked by the filter. */
  public static final String CODE_ERROR_3DSECURE = "00040";
}
