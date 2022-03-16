/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2022 Axelor (<http://axelor.com>).
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
package com.axelor.apps.base.service.wordreport.config;

import com.axelor.apps.base.db.Print;
import com.axelor.common.ObjectUtils;
import com.axelor.i18n.I18n;
import java.util.Locale;
import java.util.ResourceBundle;
import org.apache.commons.lang3.tuple.Pair;

public class WordReportTranslationServiceImpl implements WordReportTranslationService {

  private static final String TRANSLATION_FUNCTION_NORMAL_QUOTE = "_tr('";
  private static final String TRANSLATION_FUNCTION_OPEN_OFFICE_QUOTE = "_tr(‘";
  private static final String TRANSLATION_FUNCTION_VALUE = "_tr(value:";
  private static final String VALUE_PREFIX = "value:";

  @Override
  public ResourceBundle getResourceBundle(Print print) {

    ResourceBundle resourceBundle;
    String language =
        ObjectUtils.notEmpty(print.getLanguage()) ? print.getLanguage().getCode() : null;

    if (language == null) {
      resourceBundle = I18n.getBundle();
    } else if (language.equals("fr")) {
      resourceBundle = I18n.getBundle(Locale.FRANCE);
    } else {
      resourceBundle = I18n.getBundle(Locale.ENGLISH);
    }

    return resourceBundle;
  }

  @Override
  public Pair<Boolean, String> checkTranslationFunction(String value) {

    boolean translate = false;
    if (value.trim().startsWith(TRANSLATION_FUNCTION_VALUE)) {
      value =
          org.apache.commons.lang3.StringUtils.chop(
              value.trim().replace(TRANSLATION_FUNCTION_VALUE, ""));
      translate = true;
    } else if (value.trim().startsWith(TRANSLATION_FUNCTION_NORMAL_QUOTE)
        || value.trim().startsWith(TRANSLATION_FUNCTION_OPEN_OFFICE_QUOTE)) {
      value =
          value
              .trim()
              .replace(TRANSLATION_FUNCTION_NORMAL_QUOTE, "")
              .replace(TRANSLATION_FUNCTION_OPEN_OFFICE_QUOTE, "")
              .replace("')", "")
              .replace("’)", "");
      translate = true;
    }
    return Pair.of(translate, value);
  }

  @Override
  public String getValueTranslation(String value, ResourceBundle resourceBundle) {
    value = resourceBundle.getString(VALUE_PREFIX + value);
    value = value.startsWith(VALUE_PREFIX) ? value.replace(VALUE_PREFIX, "") : value;
    return value;
  }
}
