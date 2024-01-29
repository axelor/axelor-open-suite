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

import com.axelor.app.AppSettings;
import com.axelor.app.AvailableAppSettings;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.exceptions.BaseExceptionMessage;
import com.axelor.common.ObjectUtils;
import com.axelor.common.StringUtils;
import com.axelor.i18n.I18n;
import java.time.chrono.IsoChronology;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.FormatStyle;
import java.util.Locale;
import java.util.Optional;

public class CompanyDateService {

  public DateTimeFormatter getDateFormat(Company company) throws AxelorException {
    Locale locale = checkLocale(company);
    return DateTimeFormatter.ofPattern(getPattern(null, locale));
  }

  public DateTimeFormatter getDateTimeFormat(Company company) throws AxelorException {
    Locale locale = checkLocale(company);
    return DateTimeFormatter.ofPattern(getPattern(FormatStyle.SHORT, locale));
  }

  protected Locale checkLocale(Company company) throws AxelorException {
    Locale locale = null;
    String localeStr = Optional.ofNullable(company).map(Company::getLocale).orElse(null);

    if (StringUtils.isEmpty(localeStr)) {
      localeStr = AppSettings.get().get(AvailableAppSettings.APPLICATION_LOCALE, null);
    }
    if (StringUtils.notEmpty(localeStr)) {
      locale = Locale.forLanguageTag(localeStr.replace("_", "-"));
    }
    if (ObjectUtils.isEmpty(locale)) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_NO_VALUE,
          I18n.get(BaseExceptionMessage.COMPANY_LOCALE_MISSING),
          company.getName());
    }
    return locale;
  }

  protected String getPattern(FormatStyle style, Locale locale) {
    return DateTimeFormatterBuilder.getLocalizedDateTimePattern(
            FormatStyle.SHORT, style, IsoChronology.INSTANCE, locale)
        .replaceAll("\\by+\\b", "yyyy")
        .replaceAll("\\bu+\\b", "uuuu");
  }
}
