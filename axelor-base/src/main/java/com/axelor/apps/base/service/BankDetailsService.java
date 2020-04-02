/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2020 Axelor (<http://axelor.com>).
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
package com.axelor.apps.base.service;

import com.axelor.apps.account.db.PaymentMode;
import com.axelor.apps.base.db.Bank;
import com.axelor.apps.base.db.BankDetails;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.exception.AxelorException;
import org.iban4j.IbanFormatException;
import org.iban4j.InvalidCheckDigitException;
import org.iban4j.UnsupportedCountryException;

public interface BankDetailsService {

  /**
   * This method allows to extract information from iban Update following fields :
   *
   * <ul>
   *   <li>BankCode
   *   <li>SortCode
   *   <li>AccountNbr
   *   <li>BbanKey
   *   <li>Bank
   * </ul>
   *
   * @param bankDetails
   * @return BankDetails
   */
  BankDetails detailsIban(BankDetails bankDetails);

  /**
   * Method allowing to create a bank details
   *
   * @param accountNbr
   * @param bankCode
   * @param bbanKey
   * @param bank
   * @param ownerName
   * @param partner
   * @param sortCode
   * @return
   */
  BankDetails createBankDetails(
      String accountNbr,
      String bankCode,
      String bbanKey,
      Bank bank,
      String ownerName,
      Partner partner,
      String sortCode);

  /**
   * Create domain for the field companyBankDetails.
   *
   * @param company
   * @param paymentMode
   * @return
   * @throws AxelorException
   */
  String createCompanyBankDetailsDomain(
      Partner partner, Company company, PaymentMode paymentMode, Integer operationTypeSelect)
      throws AxelorException;

  /**
   * @param company
   * @param paymentMode
   * @param partner
   * @return default value for the field companyBankDetails
   * @throws AxelorException
   */
  BankDetails getDefaultCompanyBankDetails(
      Company company, PaymentMode paymentMode, Partner partner, Integer operationTypeSelect)
      throws AxelorException;

  /**
   * ABS method to validate a iban.
   *
   * @param iban
   * @throws IbanFormatException
   * @throws InvalidCheckDigitException
   * @throws UnsupportedCountryException
   */
  void validateIban(String iban)
      throws IbanFormatException, InvalidCheckDigitException, UnsupportedCountryException;
}
