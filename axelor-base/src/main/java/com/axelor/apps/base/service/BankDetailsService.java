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
package com.axelor.apps.base.service;

import com.axelor.apps.account.db.PaymentMode;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Bank;
import com.axelor.apps.base.db.BankDetails;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Currency;
import com.axelor.apps.base.db.Partner;
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
   * Get active company bank details filtered on a currency
   *
   * @param company
   * @param currency
   * @return A string field that can used as domain (Jpql WHERE clause)
   */
  String getActiveCompanyBankDetails(Company company, Currency currency);

  /**
   * Get active company bank details
   *
   * @param company
   * @return A string field that can used as domain (Jpql WHERE clause)
   */
  String getActiveCompanyBankDetails(Company company);

  /**
   * Method to validate a iban.
   *
   * @param iban
   * @throws IbanFormatException
   * @throws InvalidCheckDigitException
   * @throws UnsupportedCountryException
   */
  void validateIban(String iban)
      throws IbanFormatException, InvalidCheckDigitException, UnsupportedCountryException;
}
