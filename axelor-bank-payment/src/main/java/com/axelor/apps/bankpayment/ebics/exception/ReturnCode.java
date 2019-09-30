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
package com.axelor.apps.bankpayment.ebics.exception;

import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;

/**
 * Representation of EBICS return codes. The return codes are described in chapter 13 of EBICS
 * specification.
 *
 * @author hachani
 */
public class ReturnCode {

  /**
   * Constructs a new <code>ReturnCode</code> with a given standard code, symbolic name and text
   *
   * @param code the given standard code.
   * @param symbolicName the symbolic name.
   * @param the code text
   */
  public ReturnCode(String code, String symbolicName, String text) {
    this.code = code;
    this.symbolicName = symbolicName;
    this.text = text;
  }

  /**
   * Throws an equivalent <code>EbicsException</code>
   *
   * @throws EbicsException
   */
  public void throwException() throws AxelorException {
    throw new AxelorException(TraceBackRepository.TYPE_FUNCTIONNAL, I18n.get(text));
  }

  /**
   * Tells if the return code is an OK one.
   *
   * @return True if the return code is OK one.
   */
  public boolean isOk() {
    return equals(EBICS_OK);
  }

  /**
   * Returns a slightly more human readable version of this return code.
   *
   * @return a slightly more human readable version of this return code.
   */
  public String getSymbolicName() {
    return symbolicName;
  }

  /**
   * Returns a display text for the default locale.
   *
   * @return a text that can be displayed.
   */
  public String getText() {
    return text;
  }

  /**
   * Returns the code.
   *
   * @return the code.
   */
  public int getCode() {
    return Integer.parseInt(code);
  }

  /**
   * Returns the equivalent <code>ReturnCode</code> of a given code
   *
   * @param code the given code
   * @param text the given code text
   * @return the equivalent <code>ReturnCode</code>
   */
  public static ReturnCode toReturnCode(String code, String text) {
    if (code.equals(EBICS_OK.code)) {
      return EBICS_OK;
    } else if (code.equals(EBICS_DOWNLOAD_POSTPROCESS_DONE.code)) {
      return EBICS_DOWNLOAD_POSTPROCESS_DONE;
    } else if (code.equals(EBICS_DOWNLOAD_POSTPROCESS_SKIPPED.code)) {
      return EBICS_DOWNLOAD_POSTPROCESS_SKIPPED;
    } else if (code.equals(EBICS_TX_SEGMENT_NUMBER_UNDERRUN.code)) {
      return EBICS_DOWNLOAD_POSTPROCESS_SKIPPED;
    } else if (code.equals(EBICS_AUTHENTICATION_FAILED.code)) {
      return EBICS_AUTHENTICATION_FAILED;
    } else if (code.equals(EBICS_INVALID_REQUEST.code)) {
      return EBICS_INVALID_REQUEST;
    } else if (code.equals(EBICS_INTERNAL_ERROR.code)) {
      return EBICS_INTERNAL_ERROR;
    } else if (code.equals(EBICS_TX_RECOVERY_SYNC.code)) {
      return EBICS_TX_RECOVERY_SYNC;
    } else if (code.equals(EBICS_INVALID_USER_OR_USER_STATE.code)) {
      return EBICS_INVALID_USER_OR_USER_STATE;
    } else if (code.equals(EBICS_USER_UNKNOWN.code)) {
      return EBICS_USER_UNKNOWN;
    } else if (code.equals(EBICS_INVALID_USER_STATE.code)) {
      return EBICS_INVALID_USER_STATE;
    } else if (code.equals(EBICS_INVALID_ORDER_TYPE.code)) {
      return EBICS_INVALID_ORDER_TYPE;
    } else if (code.equals(EBICS_UNSUPPORTED_ORDER_TYPE.code)) {
      return EBICS_UNSUPPORTED_ORDER_TYPE;
    } else if (code.equals(EBICS_USER_AUTHENTICATION_REQUIRED.code)) {
      return EBICS_USER_AUTHENTICATION_REQUIRED;
    } else if (code.equals(EBICS_BANK_PUBKEY_UPDATE_REQUIRED.code)) {
      return EBICS_BANK_PUBKEY_UPDATE_REQUIRED;
    } else if (code.equals(EBICS_SEGMENT_SIZE_EXCEEDED.code)) {
      return EBICS_SEGMENT_SIZE_EXCEEDED;
    } else if (code.equals(EBICS_TX_UNKNOWN_TXID.code)) {
      return EBICS_TX_UNKNOWN_TXID;
    } else if (code.equals(EBICS_TX_ABORT.code)) {
      return EBICS_TX_ABORT;
    } else if (code.equals(EBICS_TX_MESSAGE_REPLAY.code)) {
      return EBICS_TX_MESSAGE_REPLAY;
    } else if (code.equals(EBICS_TX_SEGMENT_NUMBER_EXCEEDED.code)) {
      return EBICS_TX_SEGMENT_NUMBER_EXCEEDED;
    } else if (code.equals(EBICS_X509_CERTIFICATE_NOT_VALID_YET.code)) {
      return EBICS_X509_CERTIFICATE_NOT_VALID_YET;
    } else if (code.equals(EBICS_MAX_TRANSACTIONS_EXCEEDED.code)) {
      return EBICS_MAX_TRANSACTIONS_EXCEEDED;
    } else if (code.equals(EBICS_SIGNATURE_VERIFICATION_FAILED.code)) {
      return EBICS_SIGNATURE_VERIFICATION_FAILED;
    } else if (code.equals(EBICS_NO_DOWNLOAD_DATA_AVAILABLE.code)) {
      return EBICS_NO_DOWNLOAD_DATA_AVAILABLE;
    } else if (code.equals(EBICS_ORDER_PARAMS_IGNORED.code)) {
      return EBICS_ORDER_PARAMS_IGNORED;
    } else if (code.equals(EBICS_INVALID_XML.code)) {
      return EBICS_INVALID_XML;
    } else if (code.equals(EBICS_INVALID_HOST_ID.code)) {
      return EBICS_INVALID_HOST_ID;
    } else if (code.equals(EBICS_INVALID_ORDER_PARAMS.code)) {
      return EBICS_INVALID_ORDER_PARAMS;
    } else if (code.equals(EBICS_INVALID_REQUEST_CONTENT.code)) {
      return EBICS_INVALID_REQUEST_CONTENT;
    } else if (code.equals(EBICS_MAX_ORDER_DATA_SIZE_EXCEEDED.code)) {
      return EBICS_MAX_ORDER_DATA_SIZE_EXCEEDED;
    } else if (code.equals(EBICS_MAX_SEGMENTS_EXCEEDED.code)) {
      return EBICS_MAX_SEGMENTS_EXCEEDED;
    } else if (code.equals(EBICS_PARTNER_ID_MISMATCH.code)) {
      return EBICS_PARTNER_ID_MISMATCH;
    } else if (code.equals(EBICS_INCOMPATIBLE_ORDER_ATTRIBUTE.code)) {
      return EBICS_INCOMPATIBLE_ORDER_ATTRIBUTE;
    } else {
      return new ReturnCode(code, text, text);
    }
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof ReturnCode) {
      return this.code.equals(((ReturnCode) obj).code);
    }

    return false;
  }

  @Override
  public int hashCode() {
    return Integer.parseInt(code);
  }

  // --------------------------------------------------------------------
  // DATA MEMBERS
  // --------------------------------------------------------------------

  private String code;
  private String symbolicName;
  private String text;
  public static final ReturnCode EBICS_OK;
  public static final ReturnCode EBICS_DOWNLOAD_POSTPROCESS_DONE;
  public static final ReturnCode EBICS_DOWNLOAD_POSTPROCESS_SKIPPED;
  public static final ReturnCode EBICS_TX_SEGMENT_NUMBER_UNDERRUN;
  public static final ReturnCode EBICS_AUTHENTICATION_FAILED;
  public static final ReturnCode EBICS_INVALID_REQUEST;
  public static final ReturnCode EBICS_INTERNAL_ERROR;
  public static final ReturnCode EBICS_TX_RECOVERY_SYNC;
  public static final ReturnCode EBICS_INVALID_USER_OR_USER_STATE;
  public static final ReturnCode EBICS_USER_UNKNOWN;
  public static final ReturnCode EBICS_INVALID_USER_STATE;
  public static final ReturnCode EBICS_INVALID_ORDER_TYPE;
  public static final ReturnCode EBICS_UNSUPPORTED_ORDER_TYPE;
  public static final ReturnCode EBICS_USER_AUTHENTICATION_REQUIRED;
  public static final ReturnCode EBICS_BANK_PUBKEY_UPDATE_REQUIRED;
  public static final ReturnCode EBICS_SEGMENT_SIZE_EXCEEDED;
  public static final ReturnCode EBICS_TX_UNKNOWN_TXID;
  public static final ReturnCode EBICS_TX_ABORT;
  public static final ReturnCode EBICS_TX_MESSAGE_REPLAY;
  public static final ReturnCode EBICS_TX_SEGMENT_NUMBER_EXCEEDED;
  public static final ReturnCode EBICS_X509_CERTIFICATE_NOT_VALID_YET;
  public static final ReturnCode EBICS_MAX_TRANSACTIONS_EXCEEDED;
  public static final ReturnCode EBICS_SIGNATURE_VERIFICATION_FAILED;
  public static final ReturnCode EBICS_NO_DOWNLOAD_DATA_AVAILABLE;
  public static final ReturnCode EBICS_ORDER_PARAMS_IGNORED;
  public static final ReturnCode EBICS_INVALID_XML;
  public static final ReturnCode EBICS_INVALID_HOST_ID;
  public static final ReturnCode EBICS_INVALID_ORDER_PARAMS;
  public static final ReturnCode EBICS_INVALID_REQUEST_CONTENT;
  public static final ReturnCode EBICS_MAX_ORDER_DATA_SIZE_EXCEEDED;
  public static final ReturnCode EBICS_MAX_SEGMENTS_EXCEEDED;
  public static final ReturnCode EBICS_PARTNER_ID_MISMATCH;
  public static final ReturnCode EBICS_INCOMPATIBLE_ORDER_ATTRIBUTE;

  static {
    EBICS_OK = new ReturnCode("000000", "EBICS_OK", /*$$(*/ "000000" /*)*/);
    EBICS_DOWNLOAD_POSTPROCESS_DONE =
        new ReturnCode("011000", "EBICS_DOWNLOAD_POSTPROCESS_DONE", /*$$(*/ "011000" /*)*/);
    EBICS_DOWNLOAD_POSTPROCESS_SKIPPED =
        new ReturnCode("011001", "EBICS_DOWNLOAD_POSTPROCESS_SKIPPED", /*$$(*/ "011001" /*)*/);
    EBICS_TX_SEGMENT_NUMBER_UNDERRUN =
        new ReturnCode("011101", "EBICS_TX_SEGMENT_NUMBER_UNDERRUN", /*$$(*/ "011101" /*)*/);
    EBICS_ORDER_PARAMS_IGNORED =
        new ReturnCode("031001", "EBICS_ORDER_PARAMS_IGNORED", /*$$(*/ "031001" /*)*/);
    EBICS_AUTHENTICATION_FAILED =
        new ReturnCode("061001", "EBICS_AUTHENTICATION_FAILED", /*$$(*/ "061001" /*)*/);
    EBICS_INVALID_REQUEST =
        new ReturnCode("061002", "EBICS_INVALID_REQUEST", /*$$(*/ "061002" /*)*/);
    EBICS_INTERNAL_ERROR = new ReturnCode("061099", "EBICS_INTERNAL_ERROR", /*$$(*/ "061099" /*)*/);
    EBICS_TX_RECOVERY_SYNC =
        new ReturnCode("061101", "EBICS_TX_RECOVERY_SYNC", /*$$(*/ "061101" /*)*/);
    EBICS_NO_DOWNLOAD_DATA_AVAILABLE =
        new ReturnCode("090005", "EBICS_NO_DOWNLOAD_DATA_AVAILABLE", /*$$(*/ "090005" /*)*/);
    EBICS_INVALID_USER_OR_USER_STATE =
        new ReturnCode("091002", "EBICS_INVALID_USER_OR_USER_STATE", /*$$(*/ "091002" /*)*/);
    EBICS_USER_UNKNOWN = new ReturnCode("091003", "EBICS_USER_UNKNOWN", /*$$(*/ "091003" /*)*/);
    EBICS_INVALID_USER_STATE =
        new ReturnCode("091004", "EBICS_INVALID_USER_STATE", /*$$(*/ "091004" /*)*/);
    EBICS_INVALID_ORDER_TYPE =
        new ReturnCode("091005", "EBICS_INVALID_ORDER_TYPE", /*$$(*/ "091005" /*)*/);
    EBICS_UNSUPPORTED_ORDER_TYPE =
        new ReturnCode("091006", "EBICS_UNSUPPORTED_ORDER_TYPE", /*$$(*/ "091006" /*)*/);
    EBICS_USER_AUTHENTICATION_REQUIRED =
        new ReturnCode("091007", "EBICS_USER_AUTHENTICATION_REQUIRED", /*$$(*/ "091007" /*)*/);
    EBICS_BANK_PUBKEY_UPDATE_REQUIRED =
        new ReturnCode("091008", "EBICS_BANK_PUBKEY_UPDATE_REQUIRED", /*$$(*/ "091008" /*)*/);
    EBICS_SEGMENT_SIZE_EXCEEDED =
        new ReturnCode("091009", "EBICS_SEGMENT_SIZE_EXCEEDED", /*$$(*/ "091009" /*)*/);
    EBICS_INVALID_XML = new ReturnCode("091010", "EBICS_INVALID_XML", /*$$(*/ "091010" /*)*/);
    EBICS_INVALID_HOST_ID =
        new ReturnCode("091011", "EBICS_INVALID_HOST_ID", /*$$(*/ "091011" /*)*/);
    EBICS_TX_UNKNOWN_TXID =
        new ReturnCode("091101", "EBICS_TX_UNKNOWN_TXID", /*$$(*/ "091101" /*)*/);
    EBICS_TX_ABORT = new ReturnCode("091102", "EBICS_TX_ABORT", /*$$(*/ "091102" /*)*/);
    EBICS_TX_MESSAGE_REPLAY =
        new ReturnCode("091103", "EBICS_TX_MESSAGE_REPLAY", /*$$(*/ "091103" /*)*/);
    EBICS_TX_SEGMENT_NUMBER_EXCEEDED =
        new ReturnCode("091104", "EBICS_TX_SEGMENT_NUMBER_EXCEEDED", /*$$(*/ "091104" /*)*/);
    EBICS_INVALID_ORDER_PARAMS =
        new ReturnCode("091112", "EBICS_INVALID_ORDER_PARAMS", /*$$(*/ "091112" /*)*/);
    EBICS_INVALID_REQUEST_CONTENT =
        new ReturnCode("091113", "EBICS_INVALID_REQUEST_CONTENT", /*$$(*/ "091113" /*)*/);
    EBICS_MAX_ORDER_DATA_SIZE_EXCEEDED =
        new ReturnCode("091117", "EBICS_MAX_ORDER_DATA_SIZE_EXCEEDED", /*$$(*/ "091117" /*)*/);
    EBICS_MAX_SEGMENTS_EXCEEDED =
        new ReturnCode("091118", "EBICS_MAX_SEGMENTS_EXCEEDED", /*$$(*/ "091118" /*)*/);
    EBICS_MAX_TRANSACTIONS_EXCEEDED =
        new ReturnCode("091119", "EBICS_MAX_TRANSACTIONS_EXCEEDED", /*$$(*/ "091119" /*)*/);
    EBICS_PARTNER_ID_MISMATCH =
        new ReturnCode("091120", "EBICS_PARTNER_ID_MISMATCH", /*$$(*/ "091120" /*)*/);
    EBICS_INCOMPATIBLE_ORDER_ATTRIBUTE =
        new ReturnCode("091121", "EBICS_INCOMPATIBLE_ORDER_ATTRIBUTE", /*$$(*/ "091121" /*)*/);
    EBICS_X509_CERTIFICATE_NOT_VALID_YET =
        new ReturnCode("091209", "EBICS_X509_CERTIFICATE_NOT_VALID_YET", /*$$(*/ "091209" /*)*/);
    EBICS_SIGNATURE_VERIFICATION_FAILED =
        new ReturnCode("091301", "EBICS_SIGNATURE_VERIFICATION_FAILED", /*$$(*/ "091301" /*)*/);
  }
}
