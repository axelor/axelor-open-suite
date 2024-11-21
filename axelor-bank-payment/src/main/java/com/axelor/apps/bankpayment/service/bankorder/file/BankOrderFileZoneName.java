/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2024 Axelor (<http://axelor.com>).
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
package com.axelor.apps.bankpayment.service.bankorder.file;

public interface BankOrderFileZoneName {

  // BANK ORDER FILE AFB 160 DCO - Sender record
  static final String BOF_AFB_160_DCO_SENDER_RECORD_ZONE_A = /*$$(*/ "A - Register code" /*)*/;
  static final String BOF_AFB_160_DCO_SENDER_RECORD_ZONE_B1 = /*$$(*/ "B1 - Operation code" /*)*/;
  static final String BOF_AFB_160_DCO_SENDER_RECORD_ZONE_B2 = /*$$(*/ "B2 - Numbering record" /*)*/;
  static final String BOF_AFB_160_DCO_SENDER_RECORD_ZONE_B3 = /*$$(*/ "B3 - Sender number" /*)*/;
  static final String BOF_AFB_160_DCO_SENDER_RECORD_ZONE_C1 = /*$$(*/ "C1 - Convention type" /*)*/;
  static final String BOF_AFB_160_DCO_SENDER_RECORD_ZONE_C2 = /*$$(*/ "C2 - Deposit date" /*)*/;
  static final String BOF_AFB_160_DCO_SENDER_RECORD_ZONE_C3 = /*$$(*/
      "C3 - Sender company name" /*)*/;
  static final String BOF_AFB_160_DCO_SENDER_RECORD_ZONE_D1 = /*$$(*/
      "D1 - Sender bank address" /*)*/;
  static final String BOF_AFB_160_DCO_SENDER_RECORD_ZONE_D21 = /*$$(*/ "D2-1 - Entry code" /*)*/;
  static final String BOF_AFB_160_DCO_SENDER_RECORD_ZONE_D22 = /*$$(*/ "D2-2 - Daily code" /*)*/;
  static final String BOF_AFB_160_DCO_SENDER_RECORD_ZONE_D23 = /*$$(*/ "D2-3 - Currency code" /*)*/;
  static final String BOF_AFB_160_DCO_SENDER_RECORD_ZONE_D3 = /*$$(*/ "D3 - Sender bank code" /*)*/;
  static final String BOF_AFB_160_DCO_SENDER_RECORD_ZONE_D4 = /*$$(*/
      "D4 - Sender bank sort code" /*)*/;
  static final String BOF_AFB_160_DCO_SENDER_RECORD_ZONE_D5 = /*$$(*/
      "D5 - Sender bank account number" /*)*/;
  static final String BOF_AFB_160_DCO_SENDER_RECORD_ZONE_F1 = /*$$(*/ "F1 - Value's date" /*)*/;
  static final String BOF_AFB_160_DCO_SENDER_RECORD_ZONE_F3 = /*$$(*/
      "F3 - Sender company siren number" /*)*/;
  static final String BOF_AFB_160_DCO_SENDER_RECORD_ZONE_G = /*$$(*/ "G - Deposit reference" /*)*/;

  // BANK ORDER FILE AFB 160 DCO - Main detail record

  static final String BOF_AFB_160_DCO_MAIN_DETAIL_RECORD_ZONE_A = /*$$(*/ "A - Register code" /*)*/;
  static final String BOF_AFB_160_DCO_MAIN_DETAIL_RECORD_ZONE_B1 = /*$$(*/
      "B1 - Operation code" /*)*/;
  static final String BOF_AFB_160_DCO_MAIN_DETAIL_RECORD_ZONE_B2 = /*$$(*/
      "B2 - Numbering record" /*)*/;
  static final String BOF_AFB_160_DCO_MAIN_DETAIL_RECORD_ZONE_C12 = /*$$(*/
      "C1-2 - Receiver reference" /*)*/;
  static final String BOF_AFB_160_DCO_MAIN_DETAIL_RECORD_ZONE_C2 = /*$$(*/
      "C2 - Receiver name" /*)*/;
  static final String BOF_AFB_160_DCO_MAIN_DETAIL_RECORD_ZONE_D1 = /*$$(*/
      "D1 - Bank domiciliation" /*)*/;
  static final String BOF_AFB_160_DCO_MAIN_DETAIL_RECORD_ZONE_D21 = /*$$(*/
      "D2-1 - Acceptation" /*)*/;
  static final String BOF_AFB_160_DCO_MAIN_DETAIL_RECORD_ZONE_D3 = /*$$(*/
      "D3 - Receiver bank code" /*)*/;
  static final String BOF_AFB_160_DCO_MAIN_DETAIL_RECORD_ZONE_D4 = /*$$(*/
      "D4 - Receiver bank sort code" /*)*/;
  static final String BOF_AFB_160_DCO_MAIN_DETAIL_RECORD_ZONE_D5 = /*$$(*/
      "D5 - Receiver bank account number" /*)*/;
  static final String BOF_AFB_160_DCO_MAIN_DETAIL_RECORD_ZONE_E1 = /*$$(*/ "E1 - Amount" /*)*/;
  static final String BOF_AFB_160_DCO_MAIN_DETAIL_RECORD_ZONE_F1 = /*$$(*/ "F1 - Due date" /*)*/;
  static final String BOF_AFB_160_DCO_MAIN_DETAIL_RECORD_ZONE_F21 = /*$$(*/
      "F2-1 - Creation date" /*)*/;
  static final String BOF_AFB_160_DCO_MAIN_DETAIL_RECORD_ZONE_F34 = /*$$(*/
      "F3-4 - SIREN number" /*)*/;
  static final String BOF_AFB_160_DCO_MAIN_DETAIL_RECORD_ZONE_G = /*$$(*/
      "G - Sender reference" /*)*/;

  // BANK ORDER FILE AFB 160 DCO - Endorsed detail record
  static final String BOF_AFB_160_DCO_ENDORSED_DETAIL_RECORD_ZONE_A = /*$$(*/
      "A - Register code" /*)*/;
  static final String BOF_AFB_160_DCO_ENDORSED_DETAIL_RECORD_ZONE_B1 = /*$$(*/
      "B1 - Operation code" /*)*/;
  static final String BOF_AFB_160_DCO_ENDORSED_DETAIL_RECORD_ZONE_B2 = /*$$(*/
      "B2 - Numbering record" /*)*/;
  static final String BOF_AFB_160_DCO_ENDORSED_DETAIL_RECORD_ZONE_C2 = /*$$(*/
      "C2 - Sender company name" /*)*/;
  static final String BOF_AFB_160_DCO_ENDORSED_DETAIL_RECORD_ZONE_E = /*$$(*/
      "E - Mandatory area" /*)*/;
  static final String BOF_AFB_160_DCO_ENDORSED_DETAIL_RECORD_ZONE_F3 = /*$$(*/
      "F3 - Sender bank siren number" /*)*/;

  // BANK ORDER FILE AFB 160 DCO - Total detail record
  static final String BOF_AFB_160_DCO_TOTAL_DETAIL_RECORD_ZONE_A = /*$$(*/
      "A - Register code" /*)*/;
  static final String BOF_AFB_160_DCO_TOTAL_DETAIL_RECORD_ZONE_B1 = /*$$(*/
      "B1 - Operation code" /*)*/;
  static final String BOF_AFB_160_DCO_TOTAL_DETAIL_RECORD_ZONE_B2 = /*$$(*/
      "B2 - Numbering record" /*)*/;
  static final String BOF_AFB_160_DCO_TOTAL_DETAIL_RECORD_ZONE_E1 = /*$$(*/
      "E1 - Total amount" /*)*/;
  static final String BOF_AFB_160_DCO_TOTAL_DETAIL_RECORD_ZONE_E2 = /*$$(*/
      "E2 - Mandatory area" /*)*/;

  // BANK ORDER FILE AFB 160 DCO - Additional detail record
  static final String BOF_AFB_160_DCO_ADDITIONAL_DETAIL_RECORD_ZONE_A = /*$$(*/
      "A - Register code" /*)*/;
  static final String BOF_AFB_160_DCO_ADDITIONAL_DETAIL_RECORD_ZONE_B1 = /*$$(*/
      "B1 - Operation code" /*)*/;
  static final String BOF_AFB_160_DCO_ADDITIONAL_DETAIL_RECORD_ZONE_B2 = /*$$(*/
      "B2 - Numbering record" /*)*/;
  static final String BOF_AFB_160_DCO_ADDITIONAL_DETAIL_RECORD_ZONE_C1 = /*$$(*/
      "C1 - Receiver address: Number and name lane" /*)*/;
  static final String BOF_AFB_160_DCO_ADDITIONAL_DETAIL_RECORD_ZONE_C2 = /*$$(*/
      "C2 - Receiver address: City" /*)*/;
  static final String BOF_AFB_160_DCO_ADDITIONAL_DETAIL_RECORD_ZONE_C3 = /*$$(*/
      "C3 - Receiver address: ZIP code" /*)*/;
}
