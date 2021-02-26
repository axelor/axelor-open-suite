/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2021 Axelor (<http://axelor.com>).
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
package com.axelor.apps.base.service.excelreport.utility;

import com.axelor.apps.base.db.PrintTemplate;
import com.axelor.apps.base.service.excelreport.config.ExcelReportHelperService;
import com.axelor.inject.Beans;
import java.util.ResourceBundle;

public class ExcelReportTranslationServiceImpl implements ExcelReportTranslationService {

  @Override
  public boolean isTranslationFunction(String value) {
    boolean isTranslation = false;
    if (value.trim().startsWith("_tr('")
        || value.trim().startsWith("_tr(‘")
        || value.trim().startsWith("_tr(value:")) {
      isTranslation = true;
    }
    return isTranslation;
  }

  @Override
  public String checkTranslationFunction(String value) {

    if (value.trim().startsWith("_tr(value:")
        && (value.trim().contains("hide") || value.trim().contains("show"))) {
      value =
          org.apache.commons.lang3.StringUtils.replaceOnce(
              value.trim().replace("_tr(value:", "").trim(), ")", "");

    } else if (value.trim().startsWith("_tr(value:")) {
      value = org.apache.commons.lang3.StringUtils.chop(value.trim().replace("_tr(value:", ""));
    } else if (value.trim().startsWith("_tr('") || value.trim().startsWith("_tr(‘")) {
      value =
          value
              .trim()
              .replace("_tr('", "")
              .replace("_tr(‘", "")
              .replace("')", "")
              .replace("’)", "");
    }
    return value;
  }

  @Override
  public Object getTranslatedValue(Object value, PrintTemplate printTemplate) {

    ResourceBundle resourceBundle =
        Beans.get(ExcelReportHelperService.class).getResourceBundle(printTemplate);

    if (value.toString().trim().startsWith("_tr(value:")) {
      value = resourceBundle.getString("value:" + value.toString());
      value =
          value.toString().startsWith("value:") ? value.toString().replace("value:", "") : value;
    } else if (value.toString().trim().startsWith("_tr('")
        || value.toString().trim().startsWith("_tr(‘")) {
      value =
          value
              .toString()
              .trim()
              .replace("_tr('", "")
              .replace("_tr(‘", "")
              .replace("')", "")
              .replace("’)", "");
      value = resourceBundle.getString(value.toString());
    } else {
      value = resourceBundle.getString("value:" + value.toString());
      value =
          value.toString().startsWith("value:") ? value.toString().replace("value:", "") : value;
    }
    return value;
  }
}
