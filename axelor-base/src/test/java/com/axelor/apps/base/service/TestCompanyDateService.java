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

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Country;
import com.axelor.apps.base.db.Language;
import com.axelor.apps.base.db.Localization;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class TestCompanyDateService {

  private static final String DD_MM_YYYY_HH_MM = "dd/MM/yyyy HH:mm";
  private static final String M_D_YYYY_H_MM_A = "M/d/yyyy, h:mm a";
  private static final String DD_MM_YYYY = "dd/MM/yyyy";
  private static final String M_D_YYYY = "M/d/yyyy";

  protected LocalDateTime todayDateTime = LocalDateTime.of(2023, 3, 28, 0, 0, 0);
  protected LocalDate todayDate = LocalDate.of(2023, 3, 28);

  private static CompanyDateService companyDateService;
  private static Company usCompany;
  private static Company frCompany;

  @BeforeAll
  static void prepare() {
    companyDateService = new CompanyDateService();
    Country country_us = new Country("UNITED STATES OF AMERICA");
    country_us.setAlpha2Code("US");
    Country country_fr = new Country("FRANCE");
    country_fr.setAlpha2Code("FR");
    Language language_us = new Language("English", "en");
    Language language_fr = new Language("French", "fr");
    usCompany = prepareCompany(language_us, country_us);
    frCompany = prepareCompany(language_fr, country_fr);
  }

  @Test
  void testDateFormatterUs() throws AxelorException {
    Assertions.assertEquals(getDateTimeFormat(M_D_YYYY), getCompanyDateformat(usCompany));
  }

  @Test
  void testDateFormatterFr() throws AxelorException {
    Assertions.assertEquals(getDateTimeFormat(DD_MM_YYYY), getCompanyDateformat(frCompany));
  }

  @Test
  void testDateTimeFormatterUs() throws AxelorException {
    Assertions.assertEquals(
        getDateTimeFormat(M_D_YYYY_H_MM_A), getCompanyDateTimeformat(usCompany));
  }

  @Test
  void testDateTimeFormatterFr() throws AxelorException {
    Assertions.assertEquals(
        getDateTimeFormat(DD_MM_YYYY_HH_MM), getCompanyDateTimeformat(frCompany));
  }

  protected static Company prepareCompany(Language language, Country country) {
    Company company = new Company();
    Localization localization = new Localization();
    localization.setName(language.getName() + " (" + country.getName() + ")");
    localization.setLanguage(language);
    localization.setCountry(country);
    localization.setCode(language.getCode() + "_" + country.getAlpha2Code());
    company.setLocalization(localization);
    return company;
  }

  protected String getDateTimeFormat(String pattern) {
    return DateTimeFormatter.ofPattern(pattern).format(todayDateTime);
  }

  protected String getCompanyDateformat(Company company) throws AxelorException {
    return companyDateService.getDateFormat(company).format(todayDate);
  }

  protected String getCompanyDateTimeformat(Company company) throws AxelorException {
    return companyDateService.getDateTimeFormat(company).format(todayDateTime);
  }
}
