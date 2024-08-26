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
import java.text.SimpleDateFormat;
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
    Locale locale = LocaleService.computeLocaleByLocaleCode(localizationCode);
    String dateFormatString =
        ((SimpleDateFormat) DateFormat.getDateInstance(DateFormat.SHORT, locale)).toPattern();
    return convertToFourDigitYear(dateFormatString);
  }

  /**
   * For Java defined date format Strings, some have only one y, such as y-MM-dd. This method
   * converts it to yyyy-MM-dd, to make it more readable for a user.
   *
   * @param dateFormatString the date format String that may have only one 'y'
   */
  protected String convertToFourDigitYear(String dateFormatString) {
    if (dateFormatString.chars().filter(c -> c == 'y').count() == 1) {
      dateFormatString = dateFormatString.replace("y", "yyyy");
    }
    return dateFormatString;
  }
}
