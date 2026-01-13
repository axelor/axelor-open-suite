/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2025 Axelor (<http://axelor.com>).
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
package com.axelor.apps.base.service.user;

import com.axelor.auth.AuthUtils;
import com.axelor.meta.CallMethod;
import com.google.common.base.Strings;
import java.math.BigDecimal;
import java.util.Locale;

public class UserLocaleHelper {

  @CallMethod
  public static String formatNumberWithLocale(BigDecimal decimal) {
    String language = AuthUtils.getUser().getLanguage();
    if (!Strings.isNullOrEmpty(language)) {
      return java.text.NumberFormat.getInstance(Locale.forLanguageTag(language)).format(decimal);
    }
    return java.text.NumberFormat.getInstance(Locale.US).format(decimal);
  }
}
