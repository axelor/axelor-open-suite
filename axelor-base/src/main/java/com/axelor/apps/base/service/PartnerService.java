/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2023 Axelor (<http://axelor.com>).
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

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Address;
import com.axelor.apps.base.db.BankDetails;
import com.axelor.apps.base.db.Currency;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.PriceList;
import com.axelor.message.db.EmailAddress;
import com.axelor.meta.CallMethod;
import java.util.List;
import java.util.Map;

public interface PartnerService {

  Partner createPartner(
      String name,
      String firstName,
      String fixedPhone,
      String mobilePhone,
      EmailAddress emailAddress,
      Currency currency,
      Address deliveryAddress,
      Address mainInvoicingAddress,
      boolean createContact);

  Partner createContact(
      Partner partner,
      String name,
      String firstName,
      String fixedPhone,
      String mobilePhone,
      EmailAddress emailAddress,
      Address mainAddress);

  void onSave(Partner partner) throws AxelorException;

  void setPartnerFullName(Partner partner);

  @CallMethod
  String computeFullName(Partner partner);

  @CallMethod
  String computeSimpleFullName(Partner partner);

  Map<String, String> getSocialNetworkUrl(String name, String firstName, Integer typeSelect);

  @Deprecated
  List<Long> findPartnerMails(Partner partner, int emailType);

  List<Long> findMailsFromPartner(Partner partner, int emailType);

  void resetDefaultAddress(Partner partner, String addrTypeQuery);

  Partner addPartnerAddress(
      Partner partner, Address address, Boolean isDefault, Boolean isInvoicing, Boolean isDelivery);

  void addContactToPartner(Partner contact);

  @CallMethod
  Address getInvoicingAddress(Partner partner);

  @CallMethod
  Address getDeliveryAddress(Partner partner);

  Address getDefaultAddress(Partner partner);

  Partner savePartner(Partner partner);

  BankDetails getDefaultBankDetails(Partner partner);

  String getSIRENNumber(Partner partner) throws AxelorException;

  void convertToIndividualPartner(Partner partner);

  /**
   * Check if the partner in view has a duplicate.
   *
   * @param partner a context partner object
   * @return if there is a duplicate partner
   */
  boolean isThereDuplicatePartner(Partner partner);

  /**
   * Check if the partner in view has an archived duplicate.
   *
   * @param partner a context partner object
   * @return a found archived partner or null.
   */
  Partner isThereDuplicatePartnerInArchive(Partner partner);

  /**
   * Search for the sale price list for the current date in the partner.
   *
   * @param partner
   * @return the sale price list for the partner null if no active price list has been found
   */
  PriceList getSalePriceList(Partner partner);

  /**
   * Get the partner language code. If null, return the default partner language.
   *
   * @param partner
   * @return
   */
  String getPartnerLanguageCode(Partner partner);

  /**
   * Normalize phone number.
   *
   * @param phoneNumber
   * @return
   */
  String normalizePhoneNumber(String phoneNumber);

  /**
   * Check phone number.
   *
   * @param phoneNumber
   * @return
   */
  boolean checkPhoneNumber(String phoneNumber);

  /**
   * Get phone number field name.
   *
   * @param actionName
   * @return
   */
  String getPhoneNumberFieldName(String actionName);

  void setCompanyStr(Partner partner);

  String computeCompanyStr(Partner partner);

  String getTaxNbrFromRegistrationCode(Partner partner);

  String getNicFromRegistrationCode(Partner partner);

  String getSirenFromRegistrationCode(Partner partner);

  boolean isRegistrationCodeValid(Partner partner);
}
