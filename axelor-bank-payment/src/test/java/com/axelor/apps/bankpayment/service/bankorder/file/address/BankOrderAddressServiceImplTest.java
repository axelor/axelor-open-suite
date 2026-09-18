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

import com.axelor.apps.bankpayment.db.BankOrderFileFormat;
import com.axelor.apps.bankpayment.db.repo.BankOrderFileFormatRepository;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Address;
import com.axelor.apps.base.db.City;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Country;
import com.axelor.apps.base.db.Street;
import com.axelor.apps.base.service.PartnerService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class BankOrderAddressServiceImplTest {

  private BankOrderAddressServiceImpl bankOrderAddressService;

  @BeforeEach
  void setUp() {
    bankOrderAddressService = new BankOrderAddressServiceImpl(Mockito.mock(PartnerService.class));
  }

  protected Country createCountry(String alpha2Code) {
    Country country = new Country();
    country.setAlpha2Code(alpha2Code);
    return country;
  }

  protected Address createAddress() {
    Address address = new Address();
    address.setStreetName("70 RUE PASTORELLI");
    address.setTownName("NICE");
    address.setZip("06000");
    address.setCountry(createCountry("FR"));
    address.setAddressL2("ALPES-MARITIMES");
    address.setAddressL3("BATIMENT B");
    address.setAddressL4("ENTREE 3");
    address.setAddressL5("BP 42");
    return address;
  }

  protected BankOrderFileFormat createFileFormat(String orderFileFormat, String addressFormat) {
    BankOrderFileFormat bankOrderFileFormat = new BankOrderFileFormat();
    bankOrderFileFormat.setOrderFileFormatSelect(orderFileFormat);
    bankOrderFileFormat.setAddressFormatSelect(addressFormat);
    return bankOrderFileFormat;
  }

  protected StructuredPostalAddress createSenderPostalAddress(Address address, String addressFormat)
      throws AxelorException {
    Company company = new Company();
    company.setAddress(address);
    return bankOrderAddressService.createSenderPostalAddress(
        company,
        createFileFormat(
            BankOrderFileFormatRepository.FILE_FORMAT_PAIN_001_001_09_SCT, addressFormat));
  }

  @Test
  void testNoneFormatProducesNoAddress() throws AxelorException {
    Assertions.assertNull(
        createSenderPostalAddress(
            createAddress(), BankOrderFileFormatRepository.ADDRESS_FORMAT_NONE));
  }

  @Test
  void testNullFormatProducesNoAddress() throws AxelorException {
    Assertions.assertNull(createSenderPostalAddress(createAddress(), null));
  }

  @Test
  void testStructuredMapsNamedElements() throws AxelorException {
    StructuredPostalAddress postalAddress =
        createSenderPostalAddress(
            createAddress(), BankOrderFileFormatRepository.ADDRESS_FORMAT_STRUCTURED);

    Assertions.assertEquals("70 RUE PASTORELLI", postalAddress.getStrtNm());
    Assertions.assertEquals("NICE", postalAddress.getTwnNm());
    Assertions.assertEquals("06000", postalAddress.getPstCd());
    Assertions.assertEquals("FR", postalAddress.getCtry());
  }

  @Test
  void testStructuredEmitsNoAddressLine() throws AxelorException {
    StructuredPostalAddress postalAddress =
        createSenderPostalAddress(
            createAddress(), BankOrderFileFormatRepository.ADDRESS_FORMAT_STRUCTURED);

    Assertions.assertTrue(postalAddress.getAdrLineList().isEmpty());
  }

  @Test
  void testHybridKeepsAtMostTwoAddressLines() throws AxelorException {
    StructuredPostalAddress postalAddress =
        createSenderPostalAddress(
            createAddress(), BankOrderFileFormatRepository.ADDRESS_FORMAT_HYBRID);

    Assertions.assertEquals(2, postalAddress.getAdrLineList().size());
    Assertions.assertEquals("ALPES-MARITIMES", postalAddress.getAdrLineList().get(0));
    Assertions.assertEquals("BATIMENT B", postalAddress.getAdrLineList().get(1));
  }

  @Test
  void testHybridSkipsLinesAlreadyMappedToNamedElements() throws AxelorException {
    Address address = createAddress();
    address.setAddressL2("70 RUE PASTORELLI");
    address.setAddressL3("NICE");
    address.setAddressL4("BATIMENT B");
    address.setAddressL5("ENTREE 3");

    StructuredPostalAddress postalAddress =
        createSenderPostalAddress(address, BankOrderFileFormatRepository.ADDRESS_FORMAT_HYBRID);

    Assertions.assertEquals(2, postalAddress.getAdrLineList().size());
    Assertions.assertEquals("BATIMENT B", postalAddress.getAdrLineList().get(0));
    Assertions.assertEquals("ENTREE 3", postalAddress.getAdrLineList().get(1));
  }

  @Test
  void testHybridWithoutRemainingLinesIsStructured() throws AxelorException {
    Address address = createAddress();
    address.setAddressL2(null);
    address.setAddressL3(null);
    address.setAddressL4(null);
    address.setAddressL5(null);

    StructuredPostalAddress postalAddress =
        createSenderPostalAddress(address, BankOrderFileFormatRepository.ADDRESS_FORMAT_HYBRID);

    Assertions.assertTrue(postalAddress.getAdrLineList().isEmpty());
    Assertions.assertEquals("NICE", postalAddress.getTwnNm());
  }

  @Test
  void testStreetNameFallsBackOnStreet() throws AxelorException {
    Address address = createAddress();
    address.setStreetName(null);
    Street street = new Street();
    street.setName("RUE DE LA PAIX");
    address.setStreet(street);

    Assertions.assertEquals(
        "RUE DE LA PAIX",
        createSenderPostalAddress(address, BankOrderFileFormatRepository.ADDRESS_FORMAT_STRUCTURED)
            .getStrtNm());
  }

  @Test
  void testTownNameAndZipFallBackOnCity() throws AxelorException {
    Address address = createAddress();
    address.setTownName(null);
    address.setZip(null);
    City city = new City();
    city.setName("PARIS");
    city.setZip("75001");
    address.setCity(city);

    StructuredPostalAddress postalAddress =
        createSenderPostalAddress(address, BankOrderFileFormatRepository.ADDRESS_FORMAT_STRUCTURED);

    Assertions.assertEquals("PARIS", postalAddress.getTwnNm());
    Assertions.assertEquals("75001", postalAddress.getPstCd());
  }

  @Test
  void testCountryCodeComesFromAlpha2CodeOnly() throws AxelorException {
    Address address = createAddress();
    Country country = new Country();
    country.setAlpha3Code("FRA");
    country.setName("FRANCE");
    address.setCountry(country);

    Assertions.assertNull(
        createSenderPostalAddress(address, BankOrderFileFormatRepository.ADDRESS_FORMAT_STRUCTURED)
            .getCtry());
  }

  @Test
  void testValuesAreTransliteratedAndTruncated() throws AxelorException {
    Address address = createAddress();
    address.setTownName("Münchenerstadtteilverwaltungsbezirksamtzentrum");

    Assertions.assertEquals(
        "Munchenerstadtteilverwaltungsbezirk",
        createSenderPostalAddress(address, BankOrderFileFormatRepository.ADDRESS_FORMAT_STRUCTURED)
            .getTwnNm());
  }

  @Test
  void testCheckAddressFormatAcceptsStructuredOnSupportedFormat() {
    Assertions.assertDoesNotThrow(
        () ->
            bankOrderAddressService.checkAddressFormat(
                createFileFormat(
                    BankOrderFileFormatRepository.FILE_FORMAT_PAIN_001_001_03_SCT,
                    BankOrderFileFormatRepository.ADDRESS_FORMAT_STRUCTURED)));
  }

  @Test
  void testCheckAddressFormatAcceptsNoneOnAnyFormat() {
    Assertions.assertDoesNotThrow(
        () ->
            bankOrderAddressService.checkAddressFormat(
                createFileFormat(
                    BankOrderFileFormatRepository.FILE_FORMAT_PAIN_001_001_02_SCT,
                    BankOrderFileFormatRepository.ADDRESS_FORMAT_NONE)));
  }

  @Test
  void testCheckAddressFormatRejectsHybridOnOldFormat() {
    Assertions.assertThrows(
        AxelorException.class,
        () ->
            bankOrderAddressService.checkAddressFormat(
                createFileFormat(
                    BankOrderFileFormatRepository.FILE_FORMAT_PAIN_001_001_03_SCT,
                    BankOrderFileFormatRepository.ADDRESS_FORMAT_HYBRID)));
  }

  @Test
  void testCheckAddressFormatRejectsStructuredOnUnsupportedFormat() {
    Assertions.assertThrows(
        AxelorException.class,
        () ->
            bankOrderAddressService.checkAddressFormat(
                createFileFormat(
                    BankOrderFileFormatRepository.FILE_FORMAT_PAIN_001_001_02_SCT,
                    BankOrderFileFormatRepository.ADDRESS_FORMAT_STRUCTURED)));
  }
}
