/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2020 Axelor (<http://axelor.com>).
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
package com.axelor.apps.base.service;

import com.axelor.app.internal.AppFilter;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Language;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import java.util.Locale;
import java.util.Optional;

public class ReportingTool {

  /** Finds locale from user company. Defaults to user locale. */
  public static Locale getCompanyLocale() {
    // manage NPE using optional
    return Optional.of(AuthUtils.getUser())
        .map(User::getActiveCompany)
        .map(Company::getLanguage)
        .map(Language::getCode)
        .map(Locale::new)
        .orElseGet(AppFilter::getLocale);
  }
}
