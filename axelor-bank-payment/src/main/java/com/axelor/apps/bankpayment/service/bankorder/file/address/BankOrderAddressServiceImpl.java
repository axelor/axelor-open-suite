/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2026 Axelor (<http://axelor.com>).
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
package com.axelor.apps.bankpayment.service.bankorder.file.address;

import com.axelor.apps.bankpayment.db.BankOrder;
import com.axelor.apps.bankpayment.db.BankOrderFileFormat;
import com.axelor.apps.bankpayment.db.BankOrderLine;
import com.axelor.apps.bankpayment.db.repo.BankOrderFileFormatRepository;
import com.axelor.apps.bankpayment.exception.BankPaymentExceptionMessage;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Address;
import com.axelor.apps.base.db.City;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Country;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.Street;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.PartnerService;
import com.axelor.common.ObjectUtils;
import com.axelor.common.StringUtils;
import com.axelor.i18n.I18n;
import com.axelor.utils.helpers.StringHtmlListBuilder;
import jakarta.inject.Inject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class BankOrderAddressServiceImpl implements BankOrderAddressService {

  protected static final int MAX_LENGTH_SUB_DEPT = 70;
  protected static final int MAX_LENGTH_STRT_NM = 70;
  protected static final int MAX_LENGTH_BLDG_NB = 16;
  protected static final int MAX_LENGTH_BLDG_NM = 35;
  protected static final int MAX_LENGTH_FLR = 70;
  protected static final int MAX_LENGTH_PST_BX = 16;
  protected static final int MAX_LENGTH_ROOM = 70;
  protected static final int MAX_LENGTH_PST_CD = 16;
  protected static final int MAX_LENGTH_TWN_NM = 35;
  protected static final int MAX_LENGTH_TWN_LCTN_NM = 35;
  protected static final int MAX_LENGTH_DSTRCT_NM = 35;
  protected static final int MAX_LENGTH_CTRY_SUB_DVSN = 35;
  protected static final int MAX_LENGTH_CTRY = 2;
  protected static final int MAX_LENGTH_ADR_LINE = 70;

  protected PartnerService partnerService;

  @Inject
  public BankOrderAddressServiceImpl(PartnerService partnerService) {
    this.partnerService = partnerService;
  }

  @Override
  public StructuredPostalAddress createSenderPostalAddress(
      Company senderCompany, BankOrderFileFormat bankOrderFileFormat) throws AxelorException {

    if (senderCompany == null || !isAddressFormatEnabled(bankOrderFileFormat)) {
      return null;
    }

    return createPostalAddress(
        senderCompany.getAddress(), bankOrderFileFormat.getAddressFormatSelect());
  }

  @Override
  public StructuredPostalAddress createReceiverPostalAddress(
      BankOrderLine bankOrderLine, BankOrderFileFormat bankOrderFileFormat) throws AxelorException {

    if (bankOrderLine == null || !isAddressFormatEnabled(bankOrderFileFormat)) {
      return null;
    }

    return createPostalAddress(
        getReceiverAddress(bankOrderLine.getPartner()),
        bankOrderFileFormat.getAddressFormatSelect());
  }

  @Override
  public void checkAddresses(BankOrder bankOrder) throws AxelorException {

    BankOrderFileFormat bankOrderFileFormat = bankOrder.getBankOrderFileFormat();

    if (!isAddressFormatEnabled(bankOrderFileFormat)) {
      return;
    }

    String addressFormat = bankOrderFileFormat.getAddressFormatSelect();
    List<String> errorList = new ArrayList<>();

    errorList.addAll(checkSenderAddress(bankOrder.getSenderCompany(), addressFormat));

    for (BankOrderLine bankOrderLine : bankOrder.getBankOrderLineList()) {
      errorList.addAll(checkReceiverAddress(bankOrderLine, addressFormat));
    }

    if (ObjectUtils.notEmpty(errorList)) {
      throw new AxelorException(
          bankOrder,
          TraceBackRepository.CATEGORY_MISSING_FIELD,
          StringHtmlListBuilder.formatMessage(
              I18n.get(BankPaymentExceptionMessage.BANK_ORDER_ADDRESS_NOT_COMPLIANT), errorList));
    }
  }

  @Override
  public void checkAddressFormat(BankOrderFileFormat bankOrderFileFormat) throws AxelorException {

    BankOrderAddressFormatHelper.checkAddressFormat(bankOrderFileFormat);
  }

  protected List<String> checkSenderAddress(Company senderCompany, String addressFormat) {

    if (senderCompany == null) {
      return new ArrayList<>();
    }

    Address senderAddress = senderCompany.getAddress();

    if (senderAddress == null) {
      return List.of(
          String.format(
              I18n.get(BankPaymentExceptionMessage.BANK_ORDER_FILE_NO_SENDER_ADDRESS),
              senderCompany.getName()));
    }

    return checkAddress(senderAddress, senderCompany.getName(), addressFormat);
  }

  protected List<String> checkReceiverAddress(BankOrderLine bankOrderLine, String addressFormat) {

    Partner partner = bankOrderLine.getPartner();
    String partyName = getPartyName(bankOrderLine);
    Address receiverAddress = getReceiverAddress(partner);

    if (receiverAddress == null) {
      return List.of(
          String.format(
              I18n.get(BankPaymentExceptionMessage.BANK_ORDER_ADDRESS_NO_RECEIVER_ADDRESS),
              partyName));
    }

    return checkAddress(receiverAddress, partyName, addressFormat);
  }

  protected List<String> checkAddress(Address address, String partyName, String addressFormat) {

    List<String> errorList = new ArrayList<>();
    StructuredPostalAddress postalAddress = createPostalAddress(address, addressFormat);

    if (postalAddress == null) {
      return errorList;
    }

    if (postalAddress.getTwnNm() == null) {
      errorList.add(
          String.format(
              I18n.get(BankPaymentExceptionMessage.BANK_ORDER_ADDRESS_MISSING_TOWN_NAME),
              partyName));
    }

    if (postalAddress.getCtry() == null) {
      errorList.add(
          String.format(
              I18n.get(BankPaymentExceptionMessage.BANK_ORDER_ADDRESS_MISSING_COUNTRY), partyName));
    }

    return errorList;
  }

  protected StructuredPostalAddress createPostalAddress(Address address, String addressFormat) {

    if (address == null || !BankOrderAddressFormatHelper.isAddressFormatEnabled(addressFormat)) {
      return null;
    }

    return BankOrderFileFormatRepository.ADDRESS_FORMAT_HYBRID.equals(addressFormat)
        ? createHybridPostalAddress(address)
        : createStructuredPostalAddress(address);
  }

  protected StructuredPostalAddress createHybridPostalAddress(Address address) {

    StructuredPostalAddress postalAddress = new StructuredPostalAddress();

    postalAddress.setTwnNm(
        SepaCharacterHelper.transliterateAndTruncate(getTownName(address), MAX_LENGTH_TWN_NM));
    postalAddress.setCtry(
        SepaCharacterHelper.transliterateAndTruncate(getCountryCode(address), MAX_LENGTH_CTRY));
    postalAddress.setAdrLineList(createAddressLineList(address));

    return postalAddress;
  }

  protected StructuredPostalAddress createStructuredPostalAddress(Address address) {

    StructuredPostalAddress postalAddress = new StructuredPostalAddress();

    postalAddress.setSubDept(
        SepaCharacterHelper.transliterateAndTruncate(
            address.getSubDepartment(), MAX_LENGTH_SUB_DEPT));
    postalAddress.setStrtNm(
        SepaCharacterHelper.transliterateAndTruncate(getStreetName(address), MAX_LENGTH_STRT_NM));
    postalAddress.setBldgNb(
        SepaCharacterHelper.transliterateAndTruncate(
            address.getBuildingNumber(), MAX_LENGTH_BLDG_NB));
    postalAddress.setBldgNm(
        SepaCharacterHelper.transliterateAndTruncate(
            address.getBuildingName(), MAX_LENGTH_BLDG_NM));
    postalAddress.setFlr(
        SepaCharacterHelper.transliterateAndTruncate(address.getFloor(), MAX_LENGTH_FLR));
    postalAddress.setPstBx(
        SepaCharacterHelper.transliterateAndTruncate(address.getPostBox(), MAX_LENGTH_PST_BX));
    postalAddress.setRoom(
        SepaCharacterHelper.transliterateAndTruncate(address.getRoom(), MAX_LENGTH_ROOM));
    postalAddress.setPstCd(
        SepaCharacterHelper.transliterateAndTruncate(getZip(address), MAX_LENGTH_PST_CD));
    postalAddress.setTwnNm(
        SepaCharacterHelper.transliterateAndTruncate(getTownName(address), MAX_LENGTH_TWN_NM));
    postalAddress.setTwnLctnNm(
        SepaCharacterHelper.transliterateAndTruncate(
            address.getTownLocationName(), MAX_LENGTH_TWN_LCTN_NM));
    postalAddress.setDstrctNm(
        SepaCharacterHelper.transliterateAndTruncate(
            address.getDistrictName(), MAX_LENGTH_DSTRCT_NM));
    postalAddress.setCtrySubDvsn(
        SepaCharacterHelper.transliterateAndTruncate(
            address.getCountrySubDivision(), MAX_LENGTH_CTRY_SUB_DVSN));
    postalAddress.setCtry(
        SepaCharacterHelper.transliterateAndTruncate(getCountryCode(address), MAX_LENGTH_CTRY));

    return postalAddress;
  }

  protected List<String> createAddressLineList(Address address) {

    List<String> addressLineList = new ArrayList<>();

    for (String addressLine :
        Arrays.asList(
            SepaCharacterHelper.transliterateAndTruncate(
                getStreetName(address), MAX_LENGTH_ADR_LINE),
            createComplementaryAddressLine(address))) {

      if (addressLine != null) {
        addressLineList.add(addressLine);
      }
    }

    return addressLineList;
  }

  protected String createComplementaryAddressLine(Address address) {

    String complementaryAddressLine =
        Stream.of(
                address.getBuildingName(),
                address.getFloor(),
                address.getPostBox(),
                address.getTownLocationName())
            .filter(value -> !StringUtils.isBlank(value))
            .collect(Collectors.joining(" "));

    return SepaCharacterHelper.transliterateAndTruncate(
        complementaryAddressLine, MAX_LENGTH_ADR_LINE);
  }

  protected Address getReceiverAddress(Partner partner) {

    if (partner == null) {
      return null;
    }

    Address receiverAddress = partner.getMainAddress();

    return receiverAddress != null ? receiverAddress : partnerService.getDefaultAddress(partner);
  }

  protected String getPartyName(BankOrderLine bankOrderLine) {

    Partner partner = bankOrderLine.getPartner();
    String partnerName = partner != null ? partner.getFullName() : "";

    return StringUtils.isBlank(bankOrderLine.getSequence())
        ? partnerName
        : String.format("%s - %s", bankOrderLine.getSequence(), partnerName);
  }

  protected String getStreetName(Address address) {

    String streetName = address.getStreetName();
    Street street = address.getStreet();

    if (StringUtils.isBlank(streetName) && street != null) {
      streetName = street.getName();
    }

    return streetName;
  }

  protected String getZip(Address address) {

    String zip = address.getZip();
    City city = address.getCity();

    if (StringUtils.isBlank(zip) && city != null) {
      zip = city.getZip();
    }

    return zip;
  }

  protected String getTownName(Address address) {

    String townName = address.getTownName();
    City city = address.getCity();

    if (StringUtils.isBlank(townName) && city != null) {
      townName = city.getName();
    }

    return townName;
  }

  protected String getCountryCode(Address address) {

    Country country = address.getCountry();

    return country != null ? country.getAlpha2Code() : null;
  }

  protected boolean isAddressFormatEnabled(BankOrderFileFormat bankOrderFileFormat) {

    return BankOrderAddressFormatHelper.isAddressFormatEnabled(bankOrderFileFormat);
  }
}
