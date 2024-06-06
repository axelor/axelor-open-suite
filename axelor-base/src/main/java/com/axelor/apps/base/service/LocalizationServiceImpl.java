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
import com.axelor.apps.base.db.Localization;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.exceptions.BaseExceptionMessage;
import com.axelor.common.StringUtils;
import com.axelor.i18n.I18n;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.Locale;
import org.apache.commons.lang3.LocaleUtils;

public class LocalizationServiceImpl implements LocalizationService {
  @Override
  public void validateLocale(Localization localization) throws AxelorException {
    String localeStr = localization.getCode();
    if (StringUtils.isEmpty(localeStr)) {
      return;
    }
    String languageTag = localeStr.replace("_", "-");

    if (LocaleUtils.availableLocaleList().stream()
        .map(Locale::toLanguageTag)
        .noneMatch(languageTag::equals)) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(BaseExceptionMessage.COMPANY_INVALID_LOCALE),
          localeStr);
    }
  }

  @Override
  public String getNumberFormat(String localizationCode) {
    Locale locale = LocaleService.computeLocaleByLocaleCode(localizationCode);
    // Create a number formatter with the specified locale
    NumberFormat usNumberFormatter = NumberFormat.getNumberInstance(locale);
    // Get the pattern string for the number format
    return ((java.text.DecimalFormat) usNumberFormatter).toLocalizedPattern();
  }

  @Override
  public String getDateFormat(String localizationCode) {
    LocalDate date = LocalDate.of(2024, 4, 17);

    // Format for en_CA: yyyy-MM-dd
    String formattedDateCanada = formatDate(date, new Locale("en", "CA"));
    System.out.println("Formatted Date (en_CA): " + formattedDateCanada);
    System.out.println("DateFormat for CA :" + DateFormat.getDateInstance(DateFormat.SHORT, new Locale("en", "CA")));

    // Format for en_US: MM/dd/yyyy
    String formattedDateUS = formatDate(date, Locale.US);
    System.out.println("Formatted Date (en_US): " + formattedDateUS);
    System.out.println("DateFormat for US :" + DateFormat.getDateInstance(DateFormat.SHORT, Locale.US));

    // Format for fr_FR: dd/MM/yyyy
    String formattedDateFrance = formatDate(date, Locale.FRANCE);
    System.out.println("Formatted Date (fr_FR): " + formattedDateFrance);
    System.out.println("DateFormat for FR :" + DateFormat.getDateInstance(DateFormat.SHORT, Locale.FRANCE));
    return null;
  }

  public static String formatDate(LocalDate date, Locale locale) {
    // Create a date formatter with the default date style for the given locale
    DateFormat formatter = DateFormat.getDateInstance(DateFormat.SHORT, locale);
    // Format the date using the formatter
    return formatter.format(java.sql.Date.valueOf(date));
  }
}
